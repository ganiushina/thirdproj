package ru.alta.thirdproj.entites;

import lombok.Data;

@Data
public class UserSalary {

    private long userId;
    private String fio;
    private String userSalary;
    private String userBonus;
    private String userBonusKPI;
    private String userMoneyReal;
    private String userSalaryAll;
    private int salaryMonth;
    private int salaryYear;
    private int salaryQuarter;
    private String position;
    private String department;




}
