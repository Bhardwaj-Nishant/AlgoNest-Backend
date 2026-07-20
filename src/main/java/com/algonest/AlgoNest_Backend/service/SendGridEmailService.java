package com.algonest.AlgoNest_Backend.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendGridEmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    public void sendOtp(String to, String otp, String type) {
        // type: "signup" or "reset"
        String subject = type.equals("reset")
                ? "🔑 Reset your AlgoNest password"
                : "🔐 Verify your AlgoNest account";

        String htmlContent = buildHtml(otp, type);

        Email from = new Email(fromEmail);
        Email toEmail = new Email(to);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        try {
            request.setBody(mail.build());
            Response response = sg.api(request);
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ OTP sent to " + to);
            } else {
                System.err.println("❌ SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
                throw new RuntimeException("SendGrid error: " + response.getBody());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to send OTP", e);
        }
    }

    private String buildHtml(String otp, String type) {
        String headerColor = type.equals("reset")
                ? "linear-gradient(135deg,#ef4444,#f97316)"
                : "linear-gradient(135deg,#2563eb,#7c3aed)";

        String heading = type.equals("reset")
                ? "Password Reset Request"
                : "Verify your AlgoNest account";

        String intro = type.equals("reset")
                ? "We received a request to reset the password for your <b>AlgoNest</b> account. Enter the OTP below to continue."
                : "Thanks for signing up for <b>AlgoNest</b>. Use the verification code below to complete your registration.";

        String warning = type.equals("reset")
                ? "🔒 This OTP is valid for 5 minutes. If you didn't request a password reset, ignore this email."
                : "⏳ This OTP is valid for 5 minutes. Never share this code with anyone.";

        return """
        <!DOCTYPE html>
        <html>
        <head><style>
            body { margin:0; padding:0; background:#f5f7fb; font-family:Arial,Helvetica,sans-serif; }
            .container { max-width:600px; margin:40px auto; background:#ffffff; border-radius:18px; overflow:hidden; box-shadow:0 8px 25px rgba(0,0,0,.08); }
            .header { background:%s; color:white; text-align:center; padding:35px; }
            .header h1 { margin:0; font-size:28px; }
            .content { padding:35px; color:#374151; line-height:1.8; }
            .otp { margin:30px auto; width:220px; background:#f3f4f6; border:2px dashed #2563eb; border-radius:12px; padding:18px; text-align:center; font-size:34px; font-weight:bold; letter-spacing:8px; color:#2563eb; }
            .warning { background:#fff7ed; border-left:4px solid #f97316; padding:15px; border-radius:8px; margin-top:25px; }
            .footer { background:#f9fafb; text-align:center; color:#6b7280; font-size:13px; padding:20px; }
        </style></head>
        <body>
        <div class="container">
            <div class="header"><h1>AlgoNest</h1></div>
            <div class="content">
                <h2>%s</h2>
                <p>%s</p>
                <div class="otp">%s</div>
                <div class="warning">%s</div>
                <p style="margin-top:30px;">If you didn't request this, you can safely ignore it.</p>
                <p>Thank You,<br><b>Team AlgoNest</b></p>
            </div>
            <div class="footer">© 2026 AlgoNest • Track • Analyze • Improve</div>
        </div>
        </body>
        </html>
        """.formatted(headerColor, heading, intro, otp, warning);
    }
}