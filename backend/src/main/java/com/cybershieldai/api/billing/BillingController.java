package com.cybershieldai.api.billing;

import com.cybershieldai.api.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
public class BillingController {
    private final BillingService billing;

    public BillingController(BillingService billing) {
        this.billing = billing;
    }

    @GetMapping("/gateway-config")
    public BillingService.GatewayConfig gateway() {
        return billing.gatewayConfig();
    }

    @GetMapping("/plans")
    public List<BillingService.PlanDto> plans() {
        return billing.listPlans();
    }

    @GetMapping("/plan")
    public BillingService.CurrentPlanDto plan() {
        return billing.currentPlan();
    }

    @PostMapping("/plans/{planId}/switch")
    public BillingService.CurrentPlanDto switchPlan(@PathVariable Long planId) {
        return billing.switchPlan(planId);
    }

    @GetMapping("/payment-methods")
    public List<BillingService.PaymentMethodDto> methods() {
        return billing.paymentMethods();
    }

    @PostMapping("/payment-methods")
    @ResponseStatus(HttpStatus.CREATED)
    public BillingService.PaymentMethodDto add(@Valid @RequestBody BillingService.AddMethodRequest req) {
        return billing.addMethod(req);
    }

    @DeleteMapping("/payment-methods/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        billing.deleteMethod(id);
    }

    @PostMapping("/payment-methods/{id}/select")
    public BillingService.PaymentMethodDto select(@PathVariable Long id) {
        return billing.selectDefault(id);
    }

    @GetMapping("/invoices")
    public PageResponse<BillingService.InvoiceDto> invoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return billing.invoices(page, size);
    }

    @GetMapping("/invoices/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        byte[] bytes = billing.invoicePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
