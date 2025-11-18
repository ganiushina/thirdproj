package ru.alta.thirdproj.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import ru.alta.thirdproj.entites.birthday.Email;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderService {

    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendHtmlMessage(Email email) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        Context context = new Context();
        context.setVariables(email.getProperties());
        helper.setFrom(email.getFrom());

        List<InternetAddress> validAddresses = new ArrayList<>();
        List<String> invalidAddresses = new ArrayList<>();

        // Проверяем каждый адрес отдельно
        for (String recipient : email.getTo()) {
            try {
                InternetAddress address = new InternetAddress(recipient.trim());
                address.validate(); // Проверка формата адреса
                validAddresses.add(address);
            } catch (AddressException e) {
                log.error("Invalid email format: {} - {}", recipient, e.getMessage());
                invalidAddresses.add(recipient);
            }
        }

        // Если нет валидных адресов - выходим
        if (validAddresses.isEmpty()) {
            log.warn("No valid email addresses found for sending");
            return;
        }

        helper.setSubject(email.getSubject());
        String html = templateEngine.process(email.getTemplate(), context);
        helper.setText(html, true);

        for (String key : email.getProperties().keySet()) {
            if (key.startsWith("img_")) {
                byte[] imgBytes = (byte[]) email.getProperties().get(key);
                ByteArrayResource imageResource = new ByteArrayResource(imgBytes);
                helper.addInline(key, imageResource, "image/png");
            }
        }

        // Отправляем каждому адресату отдельно
        for (InternetAddress address : validAddresses) {
            try {
                MimeMessage individualMessage = new MimeMessage(message);
                individualMessage.setRecipient(Message.RecipientType.TO, address);

                log.info("Attempting to send email to: {}", address);
                emailSender.send(individualMessage);
                log.info("Successfully sent email to: {}", address);
            } catch (MailSendException e) {
                log.error("Failed to send email to {}: {}", address, e.getMessage());
            } catch (MessagingException e) {
                log.error("Messaging error for {}: {}", address, e.getMessage());
            }
        }
    }

}
