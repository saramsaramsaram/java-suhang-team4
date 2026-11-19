package com.dgsw.javasuhangteam4.service;

import com.dgsw.javasuhangteam4.enitity.Sub;
import java.time.LocalDate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationMailService {

    private final ObjectProvider<JavaMailSender> senderProvider;
    private final String fromAddress;
    private final String testRecipient;

    public NotificationMailService(ObjectProvider<JavaMailSender> senderProvider, Environment env) {
        this.senderProvider = senderProvider;

        String smtpUsername = env.getProperty("spring.mail.username", "");
        fromAddress = env.getProperty("app.notifications.from", smtpUsername);
        testRecipient = env.getProperty("app.notifications.test-recipient", smtpUsername);
    }


    public void sendTestMail() {
        sendMail(testRecipient, "테스트 메일", "알림 테스트");
    }

    public void sendBillingReminder(Sub sub, LocalDate billingDate) {
        String price = sub.getMonthlyPrice().stripTrailingZeros().toPlainString();
        String body = "%s\n결제일 %s\n금액 %s원".formatted(sub.getServiceName(), billingDate, price);

        sendMail(sub.getMember().getEmail(), "결제일 알림", body);
    }


    private void sendMail(String recipient, String subject, String body) {
        JavaMailSender sender = senderProvider.getIfAvailable();
        if (sender == null || fromAddress.isBlank() || recipient.isBlank()) {
            throw new MailSendException("메일 설정 필요");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);

        sender.send(message);
    }
}
