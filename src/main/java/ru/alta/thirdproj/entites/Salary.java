package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;

@Data
public class Salary {
    private Double userSalaryAll;
    private String userSalaryAllRUB;
    private Double userBonus;
    private String userBonusRUB;
    private Double userBonusBDM;
    private String userBonusBDMRUB;
    private String userBonusBDMKPI;
    private String userBonusProjectBDM;
    private Double userSalaryNDFL;
    private String userSalaryNDFLRUB;
    private Double userSalarySingle;
    private String userSalarySingleRUB;
    private Double userSalary;
    private String userSalaryRUB;
    private Integer salaryMonth;
    private Integer salaryQuarter;
    private String salaryMonthStr;
    private Integer salaryYear;
    private String salarySickDaysPayRUB;
    private String salaryVacationPayRUB;
}
