package ru.alta.thirdproj.entites.birthday;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserBirthDay {
    Integer userId;
    String userEmail;
    String userName;
    List<BirthdayMan> birthdayManList;
}
