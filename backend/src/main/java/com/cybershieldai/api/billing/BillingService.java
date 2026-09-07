package com.cybershieldai.api.billing;

import com.cybershieldai.api.billing.entity.BillingPlanEntity;
import com.cybershieldai.api.billing.entity.InvoiceEntity;
import com.cybershieldai.api.billing.entity.OrganizationSubscriptionEntity;
import com.cybershieldai.api.billing.entity.PaymentMethodEntity;
import com.cybershieldai.api.common.ApiException;
import com.cybershieldai.api.common.OrgGuard;
import com.cybershieldai.api.common.PageResponse;
import com.cybershieldai.api.organization.OrganizationRepository;
import com.cybershieldai.api.organization.entity.OrganizationEntity;
import com.cybershieldai.api.config.AppProperties;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class BillingService {
    private final BillingPlanRepository plans;
    private final OrganizationSubscriptionRepository subscriptions;
    private final PaymentMethodRepository methods;
    private final InvoiceRepository invoices;
    private final OrganizationRepository orgs;
    private final OrgGuard orgGuard;
    private final PaymentGateway gateway;
    private final AppProperties props;

    public BillingService(BillingPlanRepository plans, OrganizationSubscriptionRepository subscriptions,
                          PaymentMethodRepository methods, InvoiceRepository invoices, OrganizationRepository orgs,
                          OrgGuard orgGuard, PaymentGateway gateway, AppProperties props) {
        this.plans = plans;
        this.subscriptions = subscriptions;
        this.methods = methods;
        this.invoices = invoices;
        this.orgs = orgs;
        this.orgGuard = orgGuard;
        this.gateway = gateway;
        this.props = props;
    }

    public GatewayConfig gatewayConfig() {
        String key = props.razorpay().keyId();
        boolean mock = key == null || key.isBlank();
        return new GatewayConfig("razorpay", key == null ? "" : key, mock);
    }

    public record GatewayConfig(String provider, String keyId, boolean mockMode) {}

    public List<PlanDto> listPlans() {
        return plans.findAll().stream().map(this::toPlan).toList();
    }

    @Transactional(readOnly = true)
    public CurrentPlanDto currentPlan() {
        Long orgId = orgGuard.requireOrg();
        OrganizationSubscriptionEntity sub = subscriptions.findByOrganizationId(orgId)
                .orElseThrow(() -> ApiException.notFound("No subscription"));
        return new CurrentPlanDto(toPlan(sub.getPlan()), sub.getRenewalDate());
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CurrentPlanDto switchPlan(Long planId) {
        orgGuard.requireAdmin();
        Long orgId = orgGuard.requireOrg();
        BillingPlanEntity plan = plans.findById(planId).orElseThrow(() -> ApiException.notFound("Plan not found"));
        OrganizationEntity org = orgs.findById(orgId).orElseThrow();
        OrganizationSubscriptionEntity sub = subscriptions.findByOrganizationId(orgId).orElseGet(() -> {
            OrganizationSubscriptionEntity s = new OrganizationSubscriptionEntity();
            s.setOrganization(org);
            return s;
        });
        sub.setPlan(plan);
        sub.setRenewalDate(LocalDate.now().plusYears(1));
        subscriptions.save(sub);
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setOrganization(org);
        invoice.setInvoiceNumber("INV-" + Instant.now().toEpochMilli());
        invoice.setAmount(plan.getPriceMonthly());
        invoice.setStatus("PAID");
        invoice.setIssuedAt(LocalDate.now());
        invoices.save(invoice);
        return new CurrentPlanDto(toPlan(plan), sub.getRenewalDate());
    }

    public List<PaymentMethodDto> paymentMethods() {
        return methods.findByOrganizationId(orgGuard.requireOrg()).stream().map(this::toMethod).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentMethodDto addMethod(@Valid AddMethodRequest req) {
        orgGuard.requireAdmin();
        Long orgId = orgGuard.requireOrg();
        var tokenized = gateway.tokenize(new PaymentGateway.AddPaymentRequest(
                req.brand(), req.last4(), req.methodType(), req.gatewayToken()));
        PaymentMethodEntity e = new PaymentMethodEntity();
        e.setOrganization(orgs.getReferenceById(orgId));
        e.setBrand(tokenized.brand());
        e.setLast4(tokenized.last4());
        e.setMethodType(tokenized.methodType());
        e.setGatewayToken(tokenized.gatewayToken());
        boolean first = methods.findByOrganizationId(orgId).isEmpty();
        e.setDefault(first);
        return toMethod(methods.save(e));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteMethod(Long id) {
        orgGuard.requireAdmin();
        methods.delete(loadMethod(id));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentMethodDto selectDefault(Long id) {
        orgGuard.requireAdmin();
        Long orgId = orgGuard.requireOrg();
        PaymentMethodEntity selected = loadMethod(id);
        for (PaymentMethodEntity m : methods.findByOrganizationId(orgId)) {
            m.setDefault(m.getId().equals(selected.getId()));
        }
        return toMethod(selected);
    }

    public PageResponse<InvoiceDto> invoices(int page, int size) {
        return PageResponse.of(invoices.findByOrganizationId(orgGuard.requireOrg(),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "issuedAt")))
                .map(this::toInvoice));
    }

    public byte[] invoicePdf(Long id) {
        InvoiceEntity invoice = invoices.findByIdAndOrganizationId(id, orgGuard.requireOrg())
                .orElseThrow(() -> ApiException.notFound("Invoice not found"));
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("CyberShield AI"));
            doc.add(new Paragraph("Invoice " + invoice.getInvoiceNumber()));
            doc.add(new Paragraph("Issued: " + invoice.getIssuedAt()));
            doc.add(new Paragraph("Amount: INR " + invoice.getAmount().toPlainString()));
            doc.add(new Paragraph("Status: " + invoice.getStatus()));
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate invoice PDF", e);
        }
    }

    private PaymentMethodEntity loadMethod(Long id) {
        return methods.findByIdAndOrganizationId(id, orgGuard.requireOrg())
                .orElseThrow(() -> ApiException.notFound("Payment method not found"));
    }

    private PlanDto toPlan(BillingPlanEntity p) {
        List<String> features = Arrays.stream(p.getFeatures().split("\\|")).map(String::trim).filter(s -> !s.isBlank()).toList();
        return new PlanDto(p.getId(), p.getCode(), p.getName(), p.getPriceMonthly(), features);
    }

    private PaymentMethodDto toMethod(PaymentMethodEntity e) {
        return new PaymentMethodDto(e.getId(), e.getBrand(), e.getLast4(), e.getMethodType(), e.isDefault(), e.getCreatedAt());
    }

    private InvoiceDto toInvoice(InvoiceEntity e) {
        return new InvoiceDto(e.getId(), e.getInvoiceNumber(), e.getAmount(), e.getStatus(), e.getIssuedAt());
    }

    public record PlanDto(Long id, String code, String name, BigDecimal priceMonthly, List<String> features) {}

    public record CurrentPlanDto(PlanDto plan, LocalDate renewalDate) {}

    public record PaymentMethodDto(Long id, String brand, String last4, String methodType, boolean isDefault,
                                   Instant createdAt) {}

    public record AddMethodRequest(@NotBlank String brand,
                                   @NotBlank @Pattern(regexp = "\\d{4}") String last4,
                                   @NotBlank String methodType,
                                   String gatewayToken) {}

    public record InvoiceDto(Long id, String invoiceNumber, BigDecimal amount, String status, LocalDate issuedAt) {}
}
