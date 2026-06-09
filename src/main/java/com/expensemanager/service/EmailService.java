package com.expensemanager.service;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private static String fromEmail;
    private static String fromPassword;
    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";

    static {
        try (InputStream input = EmailService.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            Properties props = new Properties();
            props.load(input);
            fromEmail = props.getProperty("email.from");
            fromPassword = props.getProperty("email.password");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendNewPassword(String toEmail, String newPassword) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, fromPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Money Tracker - Mật khẩu mới của bạn");
            message.setText("Xin chào,\n\n"
                    + "Bạn vừa yêu cầu cấp lại mật khẩu.\n"
                    + "Mật khẩu mới của bạn là: " + newPassword + "\n\n"
                    + "Vui lòng đăng nhập và đổi lại mật khẩu.\n\n"
                    + "Trân trọng,\nAnh Nhân đẹp zai");

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }
}