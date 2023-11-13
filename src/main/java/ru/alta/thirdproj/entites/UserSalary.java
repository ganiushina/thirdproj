package ru.alta.thirdproj.entites;

import lombok.Data;

@Data
public class UserSalary {

    private long userId;
    private String fio;
    private Double userSalary;
    private Double userBonus;
    private Double userMoneyReal;
    private int salaryMonth;
    private int salaryYear;
    private int salaryQuarter;



}
