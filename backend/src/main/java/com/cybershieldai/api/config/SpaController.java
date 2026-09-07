package com.cybershieldai.api.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the React SPA for any non-API, non-docs path so that direct
 * navigation / refresh on client-side routes (e.g. /app/overview, /login)
 * returns index.html instead of a 404. The actual static JS/CSS files are
 * served automatically by Spring Boot from src/main/resources/static.
 *
 * Only active when the built frontend is embedded in resources/static/.
 * In local dev, Vite serves the frontend directly on port 5173.
 */
@Controller
public class SpaController {

    @GetMapping(value = {
            "/",
            "/login",
            "/register",
            "/forgot-password",
            "/reset-password",
            "/app",
            "/app/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
