package ru.alta.thirdproj.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
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
        String[] recipientList = email.getTo().toArray(new String[0]);
        InternetAddress[] recipientAddress = new InternetAddress[recipientList.length];
        int counter = 0;
        for (String recipient : recipientList) {
            try {
                recipientAddress[counter] = new InternetAddress(recipient.trim());
            } catch (AddressException e) {
                throw new RuntimeException(e);
            }
            counter++;
        }
        helper.setTo(recipientAddress);
        helper.setSubject(email.getSubject());
        String html = templateEngine.process(email.getTemplate(), context);
        helper.setText(html, true);

        if (email.getProperties().containsKey("img")) {
            byte[] imgBytes = (byte[]) email.getProperties().get("img");
            ByteArrayResource imageResource = new ByteArrayResource(imgBytes);
            helper.addInline("logo", imageResource, "image/png");
        }


        log.info("Sending email: {}", email.getTo() + " " + email.getSubject());
        emailSender.send(message);
    }
}
