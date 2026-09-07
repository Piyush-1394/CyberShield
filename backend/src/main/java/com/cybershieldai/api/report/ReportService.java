package com.cybershieldai.api.report;

import com.cybershieldai.api.asset.AssetRepository;
import com.cybershieldai.api.asset.entity.AssetEntity;
import com.cybershieldai.api.common.ApiException;
import com.cybershieldai.api.common.AuthUser;
import com.cybershieldai.api.common.OrgGuard;
import com.cybershieldai.api.investment.InvestmentService;
import com.cybershieldai.api.organization.OrganizationRepository;
import com.cybershieldai.api.report.entity.ReportEntity;
import com.cybershieldai.api.risk.RiskComputationService;
import com.cybershieldai.api.user.UserRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ReportService {
    private final ReportRepository reports;
    private final FileStorage storage;
    private final OrgGuard orgGuard;
    private final OrganizationRepository orgs;
    private final UserRepository users;
    private final AssetRepository assets;
    private final RiskComputationService risk;
    private final InvestmentService investment;

    public ReportService(ReportRepository reports, FileStorage storage, OrgGuard orgGuard, OrganizationRepository orgs,
                         UserRepository users, AssetRepository assets, RiskComputationService risk,
                         InvestmentService investment) {
        this.reports = reports;
        this.storage = storage;
        this.orgGuard = orgGuard;
        this.orgs = orgs;
        this.users = users;
        this.assets = assets;
        this.risk = risk;
        this.investment = investment;
    }

    public List<ReportDto> list() {
        return reports.findByOrganizationIdOrderByCreatedAtDesc(orgGuard.requireOrg()).stream()
                .map(this::toDto).toList();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ReportDto generate(@Valid GenerateRequest req) {
        orgGuard.requireWrite();
        Long orgId = orgGuard.requireOrg();
        String type = req.reportType().toUpperCase(Locale.ROOT);
        String format = req.format() == null ? "PDF" : req.format().toUpperCase(Locale.ROOT);
        byte[] body = switch (type) {
            case "ASSET_INVENTORY" -> assetInventory(orgId, format);
            case "INVESTMENT_PLAN" -> investmentPlan(orgId, format);
            default -> riskSummary(orgId, format);
        };
        String filename = orgId + "/" + UUID.randomUUID() + "." + format.toLowerCase(Locale.ROOT);
        try {
            storage.save(filename, body);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to store report", e);
        }
        ReportEntity entity = new ReportEntity();
        entity.setOrganization(orgs.getReferenceById(orgId));
        entity.setCreatedBy(users.getReferenceById(AuthUser.current().userId()));
        entity.setReportType(type);
        entity.setFormat(format);
        entity.setFilePath(filename);
        return toDto(reports.save(entity));
    }

    public byte[] download(Long id) {
        ReportEntity entity = reports.findByIdAndOrganizationId(id, orgGuard.requireOrg())
                .orElseThrow(() -> ApiException.notFound("Report not found"));
        try {
            return storage.read(entity.getFilePath());
        } catch (Exception e) {
            throw ApiException.notFound("Report file missing");
        }
    }

    public String contentType(Long id) {
        ReportEntity entity = reports.findByIdAndOrganizationId(id, orgGuard.requireOrg())
                .orElseThrow(() -> ApiException.notFound("Report not found"));
        return "CSV".equalsIgnoreCase(entity.getFormat()) ? "text/csv" : "application/pdf";
    }

    public String filename(Long id) {
        ReportEntity entity = reports.findByIdAndOrganizationId(id, orgGuard.requireOrg())
                .orElseThrow(() -> ApiException.notFound("Report not found"));
        return entity.getReportType().toLowerCase(Locale.ROOT) + "-" + entity.getId() + "."
                + entity.getFormat().toLowerCase(Locale.ROOT);
    }

    private byte[] riskSummary(Long orgId, String format) {
        var totals = risk.totals(orgId);
        String text = "CyberShield AI Risk Summary\nOverall: " + totals.overall()
                + "\nExposure: INR " + totals.financialExposure()
                + "\nCritical assets at risk: " + totals.criticalAssetsAtRisk()
                + "\nTop threat: " + totals.topThreatLevel();
        return render(text, format);
    }

    private byte[] assetInventory(Long orgId, String format) {
        StringBuilder sb = new StringBuilder("Asset,Type,Category,Criticality,Risk,Exposure\n");
        for (AssetEntity a : assets.findByOrganizationId(orgId)) {
            sb.append(a.getName()).append(',').append(a.getType()).append(',').append(a.getCategory()).append(',')
                    .append(a.getCriticality()).append(',').append(a.getRiskScore()).append(',')
                    .append(a.getFinancialExposure()).append('\n');
        }
        return render(sb.toString(), format);
    }

    private byte[] investmentPlan(Long orgId, String format) {
        var rec = investment.recommendations();
        StringBuilder sb = new StringBuilder("Investment recommendations (budget INR ")
                .append(rec.budgetAvailable()).append(")\n");
        rec.actions().forEach(a -> sb.append(a.name()).append(" | INR ").append(a.cost())
                .append(" | ").append(a.riskReductionPercent()).append("% | selected=")
                .append(a.selected()).append('\n'));
        return render(sb.toString(), format);
    }

    private byte[] render(String text, String format) {
        if ("CSV".equalsIgnoreCase(format)) {
            return text.getBytes(StandardCharsets.UTF_8);
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();
            for (String line : text.split("\n")) {
                doc.add(new Paragraph(line));
            }
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("PDF generation failed", e);
        }
    }

    private ReportDto toDto(ReportEntity e) {
        return new ReportDto(e.getId(), e.getReportType(), e.getFormat(), e.getCreatedAt(),
                "/api/reports/" + e.getId() + "/download");
    }

    public record GenerateRequest(@NotBlank String reportType, String format) {}

    public record ReportDto(Long id, String reportType, String format, Instant createdAt, String downloadUrl) {}
}
