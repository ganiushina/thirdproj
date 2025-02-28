package ru.alta.thirdproj.entites.birthday;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BirthdayMan implements Comparable<BirthdayMan> {
    int manId;
    String manFIO;
    LocalDate birthdayDate;
    String projectName;
    String manStatus;
    String companyName;
    String position;
    String city;
    byte[] imageBytes;
    String birthdayDateStr;

    @Override
    public int compareTo(BirthdayMan o) {
        int comparebirthday
                = (o).getBirthdayDate().getDayOfMonth();

        //  For Ascending order
        return this.birthdayDate.getDayOfMonth() -  comparebirthday;
       // return 0;
    }
}
