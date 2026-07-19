package com.algonest.AlgoNest_Backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class ResetEmailService {

    private final JavaMailSender mailSender;

    public ResetEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String to, String otp) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("🔑 Reset your AlgoNest password");

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>

                        body{
                            margin:0;
                            padding:0;
                            background:#f5f7fb;
                            font-family:Arial,Helvetica,sans-serif;
                        }

                        .container{
                            max-width:600px;
                            margin:40px auto;
                            background:#ffffff;
                            border-radius:18px;
                            overflow:hidden;
                            box-shadow:0 8px 25px rgba(0,0,0,.08);
                        }

                        .header{
                            background:linear-gradient(135deg,#ef4444,#f97316);
                            color:white;
                            text-align:center;
                            padding:35px;
                        }

                        .header h1{
                            margin:0;
                            font-size:28px;
                        }

                        .content{
                            padding:35px;
                            color:#374151;
                            line-height:1.8;
                        }

                        .otp{
                            margin:30px auto;
                            width:220px;
                            background:#fff7ed;
                            border:2px dashed #ef4444;
                            border-radius:12px;
                            padding:18px;
                            text-align:center;
                            font-size:34px;
                            font-weight:bold;
                            letter-spacing:8px;
                            color:#ef4444;
                        }

                        .warning{
                            background:#fef2f2;
                            border-left:4px solid #ef4444;
                            padding:15px;
                            border-radius:8px;
                            margin-top:25px;
                        }

                        .footer{
                            background:#f9fafb;
                            text-align:center;
                            color:#6b7280;
                            font-size:13px;
                            padding:20px;
                        }

                    </style>
                </head>

                <body>

                <div class="container">

                    <div class="header">
                        <h1>AlgoNest</h1>
                    </div>

                    <div class="content">

                        <h2>Password Reset Request</h2>

                        <p>
                            We received a request to reset the password for your
                            <b>AlgoNest</b> account.
                        </p>

                        <p>
                            Enter the OTP below to continue resetting your password.
                        </p>

                        <div class="otp">%s</div>

                        <div class="warning">
                            🔒 This OTP is valid for <b>5 minutes</b>.<br><br>
                            If you did not request a password reset,
                            please ignore this email. Your account will remain secure.
                        </div>

                        <p style="margin-top:30px;">
                            For your security, never share this OTP with anyone.
                            AlgoNest will never ask for your verification code.
                        </p>

                        <p>
                            Stay secure,<br>
                            <b>Team AlgoNest</b>
                        </p>

                    </div>

                    <div class="footer">
                        © 2026 AlgoNest<br>
                        Secure • Track • Improve
                    </div>

                </div>

                </body>
                </html>
                """.formatted(otp);

        helper.setText(html, true);

        mailSender.send(message);
    }
}