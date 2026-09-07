package com.cybershieldai.api;

import com.cybershieldai.api.auth.dto.AuthDtos.LoginRequest;
import com.cybershieldai.api.auth.dto.AuthDtos.TokenResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class IsolationAndRolesIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cybershield")
            .withUsername("cybershield")
            .withPassword("cybershield");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void orgBCannotSeeOrgAAssets() throws Exception {
        String abc = login("admin@abc.university", "Admin#2026Ai");
        String other = login("admin@other.edu", "Other#2026Ai");
        String abcBody = mvc.perform(get("/api/assets").header("Authorization", "Bearer " + abc))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String otherBody = mvc.perform(get("/api/assets").header("Authorization", "Bearer " + other))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode abcAssets = mapper.readTree(abcBody).get("content");
        JsonNode otherAssets = mapper.readTree(otherBody).get("content");
        assertThat(abcAssets).isNotEmpty();
        assertThat(otherAssets.toString()).doesNotContain("Student Information System");
        assertThat(abcAssets.toString()).doesNotContain("Admissions Portal");
    }

    @Test
    void viewerCannotCreateAsset() throws Exception {
        String token = login("viewer@abc.university", "Viewer#2026Ai");
        mvc.perform(post("/api/assets").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"x","type":"Server","category":"Network","criticality":"LOW","financialExposure":1}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCannotCreateVulnerability() throws Exception {
        String token = login("viewer@abc.university", "Viewer#2026Ai");
        mvc.perform(post("/api/vulnerabilities").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"x","severity":"LOW","assetId":1,"discoveredDate":"2026-01-01"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCannotApplyInvestment() throws Exception {
        String token = login("viewer@abc.university", "Viewer#2026Ai");
        mvc.perform(post("/api/investment/apply").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCannotAddPaymentMethod() throws Exception {
        String token = login("viewer@abc.university", "Viewer#2026Ai");
        mvc.perform(post("/api/billing/payment-methods").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"brand":"Visa","last4":"1111","methodType":"card"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void analystCannotInviteUser() throws Exception {
        String token = login("analyst@abc.university", "Analyst#2026Ai");
        mvc.perform(post("/api/users/invite").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"x@abc.university","role":"VIEWER"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void analystCanCreateVulnerability() throws Exception {
        String token = login("analyst@abc.university", "Analyst#2026Ai");
        String assets = mvc.perform(get("/api/assets").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long assetId = mapper.readTree(assets).get("content").get(0).get("id").asLong();
        mvc.perform(post("/api/vulnerabilities").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Lab finding","severity":"MEDIUM","assetId":%d,"discoveredDate":"2026-01-01","status":"OPEN"}
                                """.formatted(assetId)))
                .andExpect(status().isCreated());
    }

    @Test
    void analystCannotSwitchPlan() throws Exception {
        String token = login("analyst@abc.university", "Analyst#2026Ai");
        mvc.perform(post("/api/billing/plans/1/switch").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanSwitchPlan() throws Exception {
        String token = login("admin@abc.university", "Admin#2026Ai");
        mvc.perform(post("/api/billing/plans/1/switch").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void analystCanCreateAsset() throws Exception {
        String token = login("analyst@abc.university", "Analyst#2026Ai");
        mvc.perform(post("/api/assets").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lab NAS","type":"Storage","category":"Cloud Infrastructure","criticality":"MEDIUM","financialExposure":50000,"owner":"Labs"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void adminCanInviteUser() throws Exception {
        String token = login("admin@abc.university", "Admin#2026Ai");
        mvc.perform(post("/api/users/invite").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"newhire@abc.university","role":"VIEWER"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void viewerCannotInviteUser() throws Exception {
        String token = login("viewer@abc.university", "Viewer#2026Ai");
        mvc.perform(post("/api/users/invite").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"x@abc.university","role":"VIEWER"}
                                """))
                .andExpect(status().isForbidden());
    }

    private String login(String email, String password) throws Exception {
        String json = mapper.writeValueAsString(new LoginRequest(email, password));
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readValue(body, TokenResponse.class).accessToken();
    }
}
