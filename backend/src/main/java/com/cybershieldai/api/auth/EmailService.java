package com.cybershieldai.api.auth;

public interface EmailService {
    void send(String to, String subject, String body);
}
