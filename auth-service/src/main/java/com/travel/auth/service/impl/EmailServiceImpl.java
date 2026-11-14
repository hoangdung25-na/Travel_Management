package com.travel.auth.service.impl;

import com.travel.auth.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("====================================================================");
            log.warn("CHƯA CẤU HÌNH MAIL_USERNAME & MAIL_PASSWORD TRONG FILE .env!");
            log.warn(">> MA OTP XAC THUC CHO {}: {} <<", toEmail, otp);
            log.warn("====================================================================");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Travel Management Platform");
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực OTP đặt lại mật khẩu - Travel Platform");

            String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;">
                    <h2 style="color: #0f766e; text-align: center; margin-bottom: 24px;">Xác Thực Đặt Lại Mật Khẩu</h2>
                    <p style="color: #334155; font-size: 15px; line-height: 1.6;">Xin chào,</p>
                    <p style="color: #334155; font-size: 15px; line-height: 1.6;">Bạn vừa gửi yêu cầu đặt lại mật khẩu cho tài khoản Travel Platform với email: <b>%s</b>.</p>
                    <div style="background-color: #f0fdf4; border: 2px dashed #059669; border-radius: 8px; padding: 16px; text-align: center; margin: 24px 0;">
                        <span style="font-size: 14px; color: #047857; display: block; margin-bottom: 8px;">Mã OTP xác thực của bạn (hiệu lực trong 5 phút):</span>
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #0f766e;">%s</span>
                    </div>
                    <p style="color: #64748b; font-size: 13px; line-height: 1.5;">Nếu bạn không yêu cầu hành động này, vui lòng bỏ qua email này để bảo đảm an toàn cho tài khoản của bạn.</p>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;" />
                    <p style="color: #94a3b8; font-size: 12px; text-align: center;">Travel Microservices Platform &copy; 2026. All rights reserved.</p>
                </div>
            """.formatted(toEmail, otp);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Đã gửi email OTP thực tế thành công đến hòm thư: {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email qua SMTP đến {}: {}", toEmail, e.getMessage());
            log.warn(">> FALLBACK MA OTP XAC THUC CHO {}: {} <<", toEmail, otp);
        }
    }
}
