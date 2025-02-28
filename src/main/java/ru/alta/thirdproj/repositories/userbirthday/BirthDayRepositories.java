package ru.alta.thirdproj.repositories.userbirthday;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import ru.alta.thirdproj.entites.birthday.BirthdayMan;
import ru.alta.thirdproj.entites.birthday.UserBirthDay;

import java.awt.*;
import java.io.*;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BirthDayRepositories {
    private final Sql2o sql2o;

    public BirthDayRepositories(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    private static final String SELECT_BIRTHDAY_QUERY = "SELECT * FROM [dbo].[GetUserBirDay] (:day)";
    private static final String SELECT_DAY_GRABBER_STATISTIC = "select count(*) from [GrabberBirthDayStatistic]\n" +
                                                                "where convert(date, date_start) = :date_start and type_start = :type_start";
    private static final String SET_DAY_GRABBER_STATISTIC = "insert [GrabberBirthDayStatistic] values (:date_start, :type_start)";
    private static final String SELECT_USER_PHOTO__QUERY = "select top 1 photo from man_photo mp where mp.man_id = :man_id order by id asc";


    public List<UserBirthDay> getUserBirthday(int day) {
        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_BIRTHDAY_QUERY, false)
                    .addParameter("day", day);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();
            List<UserBirthDay> userBirthDayList = new ArrayList<>();
            boolean isNotNew = false;
            boolean isPhoto = false;
            SimpleDateFormat simpleDateFormatbdate = new SimpleDateFormat("dd MMMM");


            for (Map<String, Object> n : list) {
                UserBirthDay userBirthDay = new UserBirthDay(); // пользователь, кому отправить напоминание о др
                BirthdayMan birthdayMan = new BirthdayMan(); // тот, у кого др
                List<BirthdayMan> birthdayManList = new ArrayList<>(); // список тех, у кого др для конкретного сотрудника
                int userId = 0;
                int manId = 0; // сотрудник, у кого др
                isNotNew = false;
                isPhoto = false;
                for (var entry : n.entrySet()) {
                    if (entry.getKey().equals("login_user_id")) {
                        for (int i = 0; i < userBirthDayList.size(); i++) {
                            if (userBirthDayList.get(i).getUserId().equals(entry.getValue())) {
                                userId = (Integer) entry.getValue();
                                isNotNew = true;
                            }
                        }
                        userBirthDay.setUserId((Integer) entry.getValue());
                    }


                    if (entry.getKey().equals("man_email_man_email")) {
                        userBirthDay.setUserEmail((String) entry.getValue());
                    }
                    if (entry.getKey().equals("login_user_name")) {
                        userBirthDay.setUserName((String) entry.getValue());
                    }

                    if (entry.getKey().equals("man_id")) {
                        manId = (Integer) entry.getValue();
                        birthdayMan.setManId((Integer) entry.getValue());
                    }

                    if (entry.getKey().equals("chphoto")) {
                        if ((Integer) entry.getValue() == 1) {
                            isPhoto = true;
                        }
                    }

                    if (entry.getKey().equals("man_fio")) {
                        birthdayMan.setManFIO((String) entry.getValue());
                    }

                    if (entry.getKey().equals("bdate")) {
                        birthdayMan.setBirthdayDate(convertToLocalDateViaSqlDate((Date) entry.getValue()));
                        Date bdate = Date.from(convertToLocalDateViaSqlDate((Date) entry.getValue()).atStartOfDay(ZoneId.systemDefault()).toInstant());
                        birthdayMan.setBirthdayDateStr(simpleDateFormatbdate.format(bdate));

                    }

                    if (entry.getKey().equals("name_p_c")) {
                        birthdayMan.setProjectName((String) entry.getValue());
                    }
                    if (entry.getKey().equals("stst")) {
                        birthdayMan.setManStatus((String) entry.getValue());
                    }

                    if (entry.getKey().equals("dep_name")) {
                        birthdayMan.setCompanyName((String) entry.getValue());
                    }

                    if (entry.getKey().equals("pos_name")) {
                        birthdayMan.setPosition((String) entry.getValue());
                    }
                    if (entry.getKey().equals("city")) {
                        birthdayMan.setCity((String) entry.getValue());
                    }

                }
                if (isPhoto){
                    birthdayMan.setImageBytes(getPhoto(manId));
                }


                birthdayManList.add(birthdayMan);
                userBirthDay.setBirthdayManList(birthdayManList);

                if (isNotNew) {
                    int finalUserId = userId;
                    List<UserBirthDay> result = userBirthDayList.stream()
                            .filter(a -> Objects.equals(a.getUserId(), finalUserId))
                            .collect(Collectors.toList());

                    if (!result.isEmpty()) {
                            result.get(0).getBirthdayManList().add(birthdayManList.get(0));
                   }


                }  else {
                    userBirthDayList.add(userBirthDay);
                }



            }

            return userBirthDayList; // лист со всеми пользователями и их емайлами
        }
    }

    public boolean getDayGrabberStatistic(LocalDate date, int typeStart){
        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_DAY_GRABBER_STATISTIC, false)
                    .addParameter("date_start", date)
                    .addParameter("type_start", typeStart);
           return query.executeScalar(Integer.class) >= 1;
        }

    }

    public void setDayGrabberStatistic(LocalDate date, int typeStart){
        try (Connection connection = sql2o.open()) {
             connection.createQuery(SET_DAY_GRABBER_STATISTIC, false)
                    .addParameter("date_start", date)
                    .addParameter("type_start", typeStart)
                    .executeUpdate();
             connection.commit();
        }

    }

    public LocalDate convertToLocalDateViaSqlDate(Date dateToConvert) {
        return new java.sql.Date(dateToConvert.getTime()).toLocalDate();
    }

    public byte[] getPhoto(int userId){
        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_USER_PHOTO__QUERY, false)
                    .addParameter("man_id", userId);
//            Table table = query.executeAndFetchTable();
//            List<Map<String, Object>> list = table.asList();

//            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
//            Message message = (Message) in.readObject();
//            in.close();
//            ResultSet resultSet = (ResultSet) query.executeScalar();
//
//            Blob blob = resultSet.getBlob(1);
//
//            int blobLength = (int) blob.length();
//            byte[] blobAsBytes = blob.getBytes(1, blobLength);

//release the blob and free up memory. (since JDBC 4.0)
//          blob.free();


            byte[] photo = (byte[]) query.executeScalar();
            return photo;
////            byte[] photo = query.executeScalar(ByteArrayOutputStream.class).toByteArray();
//            return blobAsBytes;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
        }
    }


}
