package com.algonest.AlgoNest_Backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String to, String otp) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("🔐 Verify your AlgoNest account");

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
                            background:linear-gradient(135deg,#2563eb,#7c3aed);
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
                            background:#f3f4f6;
                            border:2px dashed #2563eb;
                            border-radius:12px;
                            padding:18px;
                            text-align:center;
                            font-size:34px;
                            font-weight:bold;
                            letter-spacing:8px;
                            color:#2563eb;
                        }

                        .warning{
                            background:#fff7ed;
                            border-left:4px solid #f97316;
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

                        a{
                            color:#2563eb;
                            text-decoration:none;
                        }
                    </style>
                </head>

                <body>

                <div class="container">

                    <div class="header">
                        <h1>AlgoNest</h1>
                    </div>

                    <div class="content">

                        <h2>Hello!</h2>

                        <p>
                            Thanks for signing up for <b>AlgoNest</b>.
                            Use the verification code below to complete your registration.
                        </p>

                        <div class="otp">%s</div>

                        <div class="warning">
                            ⏳ This OTP is valid for <b>5 minutes</b>.
                            Never share this code with anyone.
                        </div>

                        <p style="margin-top:30px;">
                            If you didn't request this email,
                            you can safely ignore it.
                        </p>
                        <p>
                            Thank You, 
                        </p>
                        <h2>Team AlgoNest</h2>

                    </div>

                    <div class="footer">
                        © 2026 AlgoNest<br>
                        Track • Analyze • Improve
                    </div>

                </div>

                </body>
                </html>
                """.formatted(otp);

        helper.setText(html, true);

        mailSender.send(message);
    }
}