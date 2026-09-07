package com.cybershieldai.api.config;

import com.cybershieldai.api.alert.AlertRepository;
import com.cybershieldai.api.alert.entity.AlertEntity;
import com.cybershieldai.api.asset.AssetRepository;
import com.cybershieldai.api.asset.entity.AssetEntity;
import com.cybershieldai.api.billing.BillingPlanRepository;
import com.cybershieldai.api.billing.InvoiceRepository;
import com.cybershieldai.api.billing.OrganizationSubscriptionRepository;
import com.cybershieldai.api.billing.PaymentMethodRepository;
import com.cybershieldai.api.billing.entity.BillingPlanEntity;
import com.cybershieldai.api.billing.entity.InvoiceEntity;
import com.cybershieldai.api.billing.entity.OrganizationSubscriptionEntity;
import com.cybershieldai.api.billing.entity.PaymentMethodEntity;
import com.cybershieldai.api.common.Role;
import com.cybershieldai.api.common.Severity;
import com.cybershieldai.api.investment.RemediationActionRepository;
import com.cybershieldai.api.investment.entity.RemediationActionEntity;
import com.cybershieldai.api.organization.OrganizationRepository;
import com.cybershieldai.api.organization.entity.OrganizationEntity;
import com.cybershieldai.api.risk.RiskComputationService;
import com.cybershieldai.api.risk.RiskSnapshotRepository;
import com.cybershieldai.api.risk.entity.RiskSnapshotEntity;
import com.cybershieldai.api.settings.NotificationPreferenceRepository;
import com.cybershieldai.api.settings.entity.NotificationPreferenceEntity;
import com.cybershieldai.api.threatintel.ThreatIntelRepository;
import com.cybershieldai.api.threatintel.entity.ThreatIntelEntity;
import com.cybershieldai.api.user.UserRepository;
import com.cybershieldai.api.user.entity.UserEntity;
import com.cybershieldai.api.vulnerability.VulnerabilityRepository;
import com.cybershieldai.api.vulnerability.entity.VulnerabilityEntity;
import com.cybershieldai.api.whatif.WhatIfScenarioRepository;
import com.cybershieldai.api.whatif.entity.WhatIfScenarioEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Component
public class DemoDataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private final AppProperties props;
    private final OrganizationRepository orgs;
    private final UserRepository users;
    private final AssetRepository assets;
    private final VulnerabilityRepository vulns;
    private final ThreatIntelRepository threats;
    private final RemediationActionRepository actions;
    private final WhatIfScenarioRepository scenarios;
    private final AlertRepository alerts;
    private final BillingPlanRepository plans;
    private final OrganizationSubscriptionRepository subscriptions;
    private final PaymentMethodRepository methods;
    private final InvoiceRepository invoices;
    private final NotificationPreferenceRepository prefs;
    private final RiskSnapshotRepository snapshots;
    private final RiskComputationService risk;
    private final org.springframework.security.crypto.password.PasswordEncoder encoder;

    public DemoDataSeeder(AppProperties props, OrganizationRepository orgs, UserRepository users, AssetRepository assets,
                          VulnerabilityRepository vulns, ThreatIntelRepository threats, RemediationActionRepository actions,
                          WhatIfScenarioRepository scenarios, AlertRepository alerts, BillingPlanRepository plans,
                          OrganizationSubscriptionRepository subscriptions, PaymentMethodRepository methods,
                          InvoiceRepository invoices, NotificationPreferenceRepository prefs,
                          RiskSnapshotRepository snapshots, RiskComputationService risk,
                          org.springframework.security.crypto.password.PasswordEncoder encoder) {
        this.props = props;
        this.orgs = orgs;
        this.users = users;
        this.assets = assets;
        this.vulns = vulns;
        this.threats = threats;
        this.actions = actions;
        this.scenarios = scenarios;
        this.alerts = alerts;
        this.plans = plans;
        this.subscriptions = subscriptions;
        this.methods = methods;
        this.invoices = invoices;
        this.prefs = prefs;
        this.snapshots = snapshots;
        this.risk = risk;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!props.seed()) {
            return;
        }
        seedPlans();
        if (users.existsByEmailIgnoreCase("admin@abc.university")) {
            log.info("Demo data already present");
            return;
        }
        OrganizationEntity abc = org("ABC University", new BigDecimal("5000000"), new BigDecimal("1850000"));
        OrganizationEntity other = org("Other College", new BigDecimal("1000000"), BigDecimal.ZERO);

        UserEntity admin = user(abc, "admin@abc.university", "Priya Sharma", Role.ADMIN, "Admin#2026Ai");
        user(abc, "analyst@abc.university", "Rahul Mehta", Role.ANALYST, "Analyst#2026Ai");
        user(abc, "viewer@abc.university", "Anika Rao", Role.VIEWER, "Viewer#2026Ai");
        user(other, "admin@other.edu", "Jordan Lee", Role.ADMIN, "Other#2026Ai");

        AssetEntity sis = asset(abc, "Student Information System", "Application", "Identity & Access", Severity.CRITICAL, "1.85E7", "Registrar");
        AssetEntity wifi = asset(abc, "Campus Wi-Fi Core", "Network", "Network", Severity.HIGH, "9.4E6", "IT Networking");
        AssetEntity hpc = asset(abc, "Research HPC Cluster", "Cloud", "Cloud Infrastructure", Severity.CRITICAL, "2.4E7", "Research Computing");
        AssetEntity erp = asset(abc, "Finance ERP", "Application", "Applications", Severity.HIGH, "1.2E7", "Finance");
        AssetEntity mail = asset(abc, "Faculty Email Gateway", "Endpoint", "Endpoints", Severity.MEDIUM, "4.1E6", "Messaging");
        asset(abc, "Learning Management System", "Application", "Applications", Severity.HIGH, "7.2E6", "Academic IT");
        asset(abc, "Library Catalog", "Application", "Applications", Severity.LOW, "1.1E6", "Library");
        AssetEntity hvac = asset(abc, "IoT HVAC Controllers", "IoT", "Endpoints", Severity.MEDIUM, "2.8E6", "Facilities");
        asset(other, "Admissions Portal", "Application", "Applications", Severity.HIGH, "3.0E6", "Admissions");

        VulnerabilityEntity v1 = vuln(abc, sis, "Unpatched SSO library", "CRITICAL", "CVE-2024-21762", "OPEN");
        vuln(abc, hpc, "Exposed Jupyter token", "HIGH", "CVE-2024-6387", "IN_PROGRESS");
        vuln(abc, wifi, "WPA2 enterprise misconfig", "HIGH", null, "OPEN");
        vuln(abc, erp, "Outdated Oracle CPU", "MEDIUM", "CVE-2023-21971", "OPEN");
        vuln(abc, mail, "Open relay test endpoint", "LOW", null, "ACCEPTED_RISK");
        vuln(abc, hvac, "Default vendor credentials", "HIGH", null, "OPEN");

        threat(abc, "APT targeting university SSO", "Scattered Lapsus$", "Nation-state-adjacent", Severity.CRITICAL,
                List.of(sis, mail));
        threat(abc, "Ransomware against research storage", "LockBit affiliate", "Ransomware", Severity.HIGH, List.of(hpc));
        threat(abc, "Credential stuffing on student portal", "Commodity botnet", "Credential stuffing", Severity.MEDIUM, List.of(sis));

        RemediationActionEntity mfa = action(abc, "Campus-wide MFA rollout", "Identity", "800000", "18");
        RemediationActionEntity edr = action(abc, "EDR coverage for all endpoints", "Endpoints", "1200000", "22");
        RemediationActionEntity patch = action(abc, "Emergency patch window for internet-facing systems", "Applications", "350000", "15");
        action(abc, "Network micro-segmentation", "Network", "2100000", "20");
        action(abc, "Immutable backup vault", "Cloud Infrastructure", "650000", "12");

        scenario(abc, "Delay MFA rollout by 45 days", mfa, 45);
        scenario(abc, "Postpone EDR purchase 30 days", edr, 30);
        scenario(abc, "Slip emergency patch window 14 days", patch, 14);

        alert(abc, "Critical SSO library on SIS", "Unpatched component on Student Information System", Severity.CRITICAL, sis, v1);
        alert(abc, "HPC token exposure", "Research cluster notebook token found in logs", Severity.HIGH, hpc, null);
        alert(abc, "Wi-Fi radius anomaly", "Repeated failed EAP authentications from unknown NAS", Severity.MEDIUM, wifi, null);

        BillingPlanEntity growth = plans.findByCode("GROWTH").orElseThrow();
        OrganizationSubscriptionEntity sub = new OrganizationSubscriptionEntity();
        sub.setOrganization(abc);
        sub.setPlan(growth);
        sub.setRenewalDate(LocalDate.now().plusMonths(8));
        subscriptions.save(sub);

        OrganizationSubscriptionEntity otherSub = new OrganizationSubscriptionEntity();
        otherSub.setOrganization(other);
        otherSub.setPlan(plans.findByCode("STARTER").orElse(growth));
        otherSub.setRenewalDate(LocalDate.now().plusMonths(6));
        subscriptions.save(otherSub);

        pay(abc, "Visa", "4242", "card", true);
        pay(abc, "UPI", "8891", "upi", false);

        invoice(abc, "INV-2026-001", "249000", "PAID", LocalDate.now().minusMonths(2));
        invoice(abc, "INV-2026-002", "249000", "PAID", LocalDate.now().minusMonths(1));
        invoice(abc, "INV-2026-003", "249000", "OPEN", LocalDate.now());

        risk.recalculateOrganization(abc.getId());
        var totals = risk.totals(abc.getId());
        for (int i = 30; i >= 1; i--) {
            RiskSnapshotEntity snap = new RiskSnapshotEntity();
            snap.setOrganization(abc);
            snap.setSnapshotDate(LocalDate.now().minusDays(i));
            snap.setOverallScore(totals.overall().subtract(BigDecimal.valueOf(i * 0.12))
                    .max(BigDecimal.valueOf(40)).setScale(2, java.math.RoundingMode.HALF_UP));
            snap.setFinancialExposure(totals.financialExposure().subtract(BigDecimal.valueOf(i * 18000L))
                    .max(BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP));
            snapshots.save(snap);
        }
        risk.persistSnapshot(abc.getId());
        risk.recalculateOrganization(other.getId());

        log.info("Seeded ABC University demo org. Admin login: {} / {}", admin.getEmail(), "Admin#2026Ai");
    }

    private void seedPlans() {
        if (plans.count() > 0) {
            return;
        }
        plan("STARTER", "Starter", "99000", "Up to 50 assets|Email alerts|Community support");
        plan("GROWTH", "Growth", "249000", "Up to 500 assets|Investment optimizer|What-if analysis|Priority support");
        plan("ENTERPRISE", "Enterprise", "799000", "Unlimited assets|SSO|Dedicated CSM|Custom reports");
    }

    private void plan(String code, String name, String price, String features) {
        BillingPlanEntity p = new BillingPlanEntity();
        p.setCode(code);
        p.setName(name);
        p.setPriceMonthly(new BigDecimal(price));
        p.setFeatures(features);
        plans.save(p);
    }

    private OrganizationEntity org(String name, BigDecimal available, BigDecimal allocated) {
        OrganizationEntity o = new OrganizationEntity();
        o.setName(name);
        o.setBudgetAvailable(available);
        o.setBudgetAllocated(allocated);
        return orgs.save(o);
    }

    private UserEntity user(OrganizationEntity org, String email, String name, Role role, String password) {
        UserEntity u = new UserEntity();
        u.setOrganization(org);
        u.setEmail(email);
        u.setFullName(name);
        u.setRole(role);
        u.setPasswordHash(encoder.encode(password));
        u = users.save(u);
        NotificationPreferenceEntity p = new NotificationPreferenceEntity();
        p.setUser(u);
        prefs.save(p);
        return u;
    }

    private AssetEntity asset(OrganizationEntity org, String name, String type, String category, Severity crit,
                              String exposure, String owner) {
        AssetEntity a = new AssetEntity();
        a.setOrganization(org);
        a.setName(name);
        a.setType(type);
        a.setCategory(category);
        a.setCriticality(crit);
        a.setFinancialExposure(new BigDecimal(exposure));
        a.setOwner(owner);
        a.setLastScannedAt(Instant.now().minus(3, ChronoUnit.DAYS));
        a.setStatus("ACTIVE");
        return assets.save(a);
    }

    private VulnerabilityEntity vuln(OrganizationEntity org, AssetEntity asset, String title, String severity,
                                     String cve, String status) {
        VulnerabilityEntity v = new VulnerabilityEntity();
        v.setOrganization(org);
        v.setAsset(asset);
        v.setTitle(title);
        v.setDescription(title + " affecting " + asset.getName());
        v.setSeverity(Severity.valueOf(severity));
        v.setCveId(cve);
        v.setStatus(VulnerabilityEntity.Status.valueOf(status));
        v.setDiscoveredDate(LocalDate.now().minusDays(12));
        v.setRemediationDeadline(LocalDate.now().plusDays(14));
        return vulns.save(v);
    }

    private void threat(OrganizationEntity org, String title, String actor, String type, Severity sev, List<AssetEntity> related) {
        ThreatIntelEntity t = new ThreatIntelEntity();
        t.setOrganization(org);
        t.setTitle(title);
        t.setThreatActor(actor);
        t.setThreatType(type);
        t.setSeverity(sev);
        t.setSource("CyberShield Intel Feed");
        t.setPublishedDate(LocalDate.now().minusDays(4));
        t.setDescription(title);
        t.setRelatedAssets(Set.copyOf(related));
        threats.save(t);
    }

    private RemediationActionEntity action(OrganizationEntity org, String name, String category, String cost, String pct) {
        RemediationActionEntity a = new RemediationActionEntity();
        a.setOrganization(org);
        a.setName(name);
        a.setDescription(name);
        a.setCategory(category);
        a.setCost(new BigDecimal(cost));
        a.setRiskReductionPercent(new BigDecimal(pct));
        a.setActive(true);
        return actions.save(a);
    }

    private void scenario(OrganizationEntity org, String name, RemediationActionEntity control, int days) {
        WhatIfScenarioEntity s = new WhatIfScenarioEntity();
        s.setOrganization(org);
        s.setName(name);
        s.setDescription(name);
        s.setControl(control);
        s.setDelayDays(days);
        scenarios.save(s);
    }

    private void alert(OrganizationEntity org, String title, String message, Severity sev, AssetEntity asset,
                       VulnerabilityEntity vuln) {
        AlertEntity a = new AlertEntity();
        a.setOrganization(org);
        a.setTitle(title);
        a.setMessage(message);
        a.setSeverity(sev);
        a.setAsset(asset);
        a.setVulnerability(vuln);
        a.setRead(false);
        alerts.save(a);
    }

    private void pay(OrganizationEntity org, String brand, String last4, String type, boolean def) {
        PaymentMethodEntity m = new PaymentMethodEntity();
        m.setOrganization(org);
        m.setBrand(brand);
        m.setLast4(last4);
        m.setMethodType(type);
        m.setGatewayToken("tok_seed_" + last4);
        m.setDefault(def);
        methods.save(m);
    }

    private void invoice(OrganizationEntity org, String number, String amount, String status, LocalDate issued) {
        InvoiceEntity i = new InvoiceEntity();
        i.setOrganization(org);
        i.setInvoiceNumber(number);
        i.setAmount(new BigDecimal(amount));
        i.setStatus(status);
        i.setIssuedAt(issued);
        invoices.save(i);
    }
}
