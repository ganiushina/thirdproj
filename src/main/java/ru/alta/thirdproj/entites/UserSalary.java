package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;

@Data
public class UserSalary {

    private long userId;
    private String fio;
    private List<String> userSalary;
    private List<String> userBonus;
    private List<String> userBonusKPI;
    private List<String> userMoneyReal;
    private List<String> userSalaryAll;
    private int salaryMonth;
    private List<String> salaryMonthStr;
    private int salaryYear;
   // private int salaryQuarter;
    private String position;
    private String department;




}
