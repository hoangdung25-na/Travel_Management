package com.travel.auth.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp);
}
