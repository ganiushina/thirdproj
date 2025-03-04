package ru.alta.thirdproj.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.alta.thirdproj.entites.birthday.Email;
import ru.alta.thirdproj.entites.birthday.BirthdayMan;
import ru.alta.thirdproj.entites.birthday.UserBirthDay;
import ru.alta.thirdproj.repositories.userbirthday.BirthDayRepositories;

import javax.mail.MessagingException;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Configuration
@EnableScheduling
public class ScheduledConfiguration {

    protected static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

//    BirthdayGrabberService birthdayGrabber = new BirthdayGrabberService();
    public static final String MAIL_PASS = "P@SSw0rd";
    public static final String MAIL_USER = "it@altapersonnel.ru";
    public static final String MAIL_SMTP_HOST = "office.altapersonnel.ru";


    @Autowired
    private BirthDayRepositories birthDayRepositories;
    @Autowired
    private EmailSenderService emailSenderService;


//    @Autowired
//    public void BirthdayGrabberService(BirthDayRepositories birthDayRepositories) {
//        this.birthDayRepositories = birthDayRepositories;
//    }

    //       List<String> emails = new ArrayList<>();
//       emails.add("support@altapersonnel.ru");
//       emails.add("d.novozhenina@gmail.com");
//       emails.add("sadykov@altapersonnel.ru");


//    @Scheduled(cron = "0 0 7 * * *")
 //   @Scheduled(cron = "*/60 * * * * *")
    @Scheduled(cron =  "0 0 7 * * ?", zone="Europe/Samara")
    public void executeTaskUserTomorrow() throws Exception {
        List<UserBirthDay> userBirthDayList = birthDayRepositories.getUserBirthday(1);
        if (userBirthDayList.size()>0) {

            emailSenderService.sendHtmlMessage(getEmailUsersDaily(userBirthDayList, "Завтра День рождения коллеги!"));
        }

        System.out.println(Thread.currentThread().getName()+" The Task User Tomorrow executed at "+ new Date());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    @Scheduled(cron = "*/60 * * * * *")
  //  @Scheduled(cron = "0 0 8 * * *")
    @Scheduled(cron =  "0 0 8 * * ?", zone="Europe/Samara")
    public void executeTaskClient() throws MessagingException {
        List<UserBirthDay> userBirthDayList = birthDayRepositories.getUserBirthday(0);
        if (userBirthDayList.size()>0) {
            for (int i = 0; i < userBirthDayList.size() ; i++) {
                emailSenderService.sendHtmlMessage(getEmailClient(userBirthDayList.get(i).getBirthdayManList(),
                        "День рождения клиентов\\кандидатов", userBirthDayList.get(i).getUserName(),  userBirthDayList.get(i).getUserEmail()));
            }
        }
        System.out.println(Thread.currentThread().getName()+" The Task Client executed at "+ new Date());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    @Scheduled(cron = "0 0 9 1 * *")
//    @Scheduled(cron = "*/60 * * * * *")

 //   @Scheduled(cron = "0 9 1 * ?")

    @Scheduled(cron="0 0 6 1 * *", zone="Europe/Samara")
    public void executeTaskUserMonth() throws MessagingException {
        List<UserBirthDay> userBirthDayList = birthDayRepositories.getUserBirthday(100);
        if (userBirthDayList.size()>0) {
        for (int i = 0; i < userBirthDayList.size() ; i++) {                        ;
                emailSenderService.sendHtmlMessage(getEmailUsersMonthly(userBirthDayList.get(i).getBirthdayManList(),
                        "День рождения коллег в этом месяце", userBirthDayList.get(i).getUserName(), userBirthDayList.get(i).getUserEmail()));
            }
        }

        System.out.println(Thread.currentThread().getName()+" The Task User Month executed at "+ new Date());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Email getEmailClient(List<BirthdayMan> manBirthDayList, String subject, String userName, String emailStr ){
        List<String> emails = new ArrayList<>();
        Email email = new Email();
        LocalDate currentDate = LocalDate.now();
        emails.add(emailStr);
        email.setTo(emails);
        email.setFrom("it@altapersonnel.ru");
        email.setSubject(subject);
        email.setTemplate("client-email.html");
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", userName);
        properties.put("userBirthday", manBirthDayList);
        properties.put("date", currentDate);
        email.setProperties(properties);
        return email;

    }
    private Email getEmailUsersDaily(List<UserBirthDay> userBirthDayList, String subject){
        List<String> emails = new ArrayList<>();
        Email email = new Email();

        for (int i = 0; i < userBirthDayList.size(); i++) {
            emails.add(userBirthDayList.get(i).getUserEmail());
        }
        email.setTo(emails);
        email.setFrom("it@altapersonnel.ru");
        email.setSubject(subject);
        email.setTemplate("user-emailDaily.html");
        Map<String, Object> properties = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE dd MMMM");
        Date date = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        String currentDateTime = simpleDateFormat.format(date);
        properties.put("userBirthday", userBirthDayList.get(0).getBirthdayManList());
        properties.put("date", currentDateTime); //LocalDate.now().plusDays(1));
        properties.put("welcomestr", "День Рождения отмечает");
        properties.put("img", userBirthDayList.get(0).getBirthdayManList().get(0).getImageBytes());
        email.setProperties(properties);
        return email;

    }

    private Email getEmailUsersMonthly(List<BirthdayMan> manBirthDayList, String subject, String userName, String emailAdr){
        List<String> emails = new ArrayList<>();
        Map<String, Object> properties = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM");
        Date date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        String currentDateTime = simpleDateFormat.format(date);
        Email email = new Email();
        emails.add(emailAdr);
        email.setTo(emails);
        email.setFrom("it@altapersonnel.ru");
        email.setSubject(subject);
        email.setTemplate("user-emailMonthly.html");
        properties.put("name", userName);
        properties.put("userBirthday", manBirthDayList);
        properties.put("date", currentDateTime);
        email.setProperties(properties);
        return email;

    }






}
