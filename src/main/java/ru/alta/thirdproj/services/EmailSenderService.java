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

        // Проверяем каждый адрес отдельно
        for (String recipient : email.getTo()) {
            try {
                InternetAddress address = new InternetAddress(recipient.trim());
                address.validate(); // Проверка формата адреса
                validAddresses.add(address);
            } catch (AddressException e) {
                log.error("Invalid email address: {} - {}", recipient, e.getMessage());
            }
        }

        // Если нет валидных адресов - выходим
        if (validAddresses.isEmpty()) {
            log.warn("No valid email addresses found for sending");
            return;
        }

        // Устанавливаем получателей
        helper.setTo(validAddresses.toArray(new InternetAddress[0]));
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

        log.info("Sending email to valid addresses: {}", validAddresses);

        try {
            emailSender.send(message);
        } catch (MailSendException e) {
            // Логируем ошибки отправки, но не прерываем выполнение
            log.error("Error sending email to some addresses: {}", e.getFailedMessages());
        }
    }

}
