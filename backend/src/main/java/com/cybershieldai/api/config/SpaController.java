package com.cybershieldai.api.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Serves the React SPA when the built frontend is embedded inside resources/static/.
 * When deployed as a standalone API on Render, returns an API health/info payload.
 */
@Controller
public class SpaController {

    private final boolean hasEmbeddedFrontend = new ClassPathResource("static/index.html").exists();

    @GetMapping(value = {
            "/login",
            "/register",
            "/forgot-password",
            "/reset-password",
            "/app",
            "/app/**"
    })
    public Object forwardClientRoutes() {
        if (hasEmbeddedFrontend) {
            return "forward:/index.html";
        }
        return org.springframework.http.ResponseEntity.status(404).body(Map.of(
                "error", "Frontend is hosted separately. Please access the web application via its frontend URL."
        ));
    }

    @GetMapping("/")
    public Object root() {
        if (hasEmbeddedFrontend) {
            return "forward:/index.html";
        }
        return org.springframework.http.ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "CyberShield AI API",
                "version", "1.0.0",
                "docs", "/swagger-ui.html"
        ));
    }
}
