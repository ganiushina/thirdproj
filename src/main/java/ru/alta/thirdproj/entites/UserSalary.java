package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;

@Data
public class UserSalary {
    private long userId;
    private String userFio;
    private String userBonusKPI;
    private String userBonus;
    private String userSalary;
    private String userSalaryAll;
    private String userSalaryAllWithOutCoef;
    private String department;
    private String position;
    private int salaryMonth;
    private int salaryQuarter;
    private String salaryMonthStr;
    private int salaryYear;
}
