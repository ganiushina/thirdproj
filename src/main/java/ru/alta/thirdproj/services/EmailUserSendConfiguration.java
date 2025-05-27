package ru.alta.thirdproj.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.alta.thirdproj.entites.UserEmail;
import ru.alta.thirdproj.entites.birthday.BirthdayMan;
import ru.alta.thirdproj.entites.birthday.Email;
import ru.alta.thirdproj.entites.birthday.UserBirthDay;
import ru.alta.thirdproj.repositories.EmailUserRepositoriesImpl;
import ru.alta.thirdproj.repositories.userbirthday.BirthDayRepositories;

import javax.mail.MessagingException;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
public class EmailUserSendConfiguration {

    protected static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private EmailUserRepositoriesImpl emailUserRepositories;
    @Autowired
    private EmailSenderService emailSenderService;

    public void sendAccessToAllUsers(LocalDate dateFrom, LocalDate dateTo, String period) throws Exception {
//        UserEmail userEmail1 = new UserEmail();
//        userEmail1.setUserEmail("stimul22@mail.ru");
//        UserEmail userEmail2 = new UserEmail();
//        userEmail2.setUserEmail("support@altapersonnel.ru");
//        List<UserEmail> userEmails = new ArrayList<>();
//        userEmails.add(userEmail1);
//        userEmails.add(userEmail2);
        List<UserEmail> userEmails = emailUserRepositories.getUserBirthday();

        if (!userEmails.isEmpty()) {
            emailSenderService.sendHtmlMessage(
                    getEmailUsers(userEmails, "Сверка выплат", period)
            );
        }
        log.info("Отправка писем о сверке выплат за {} выполнена {}", period, LocalDateTime.now());

        System.out.println(Thread.currentThread().getName()+" The payment verification has been performed "+ new Date());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Email getEmailUsers(List<UserEmail> userEmails, String subject, String period) {
        List<String> emails = userEmails.stream()
                .map(UserEmail::getUserEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Email email = new Email();
        email.setTo(emails);
        email.setFrom("it@altapersonnel.ru");
        email.setSubject(subject);
        email.setTemplate("user_payment_done");

        Map<String, Object> properties = new HashMap<>();
        properties.put("period", period);
        properties.put("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        email.setProperties(properties);
        return email;
    }

}
