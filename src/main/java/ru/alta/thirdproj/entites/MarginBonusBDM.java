package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;

@Data
public class MarginBonusBDM {
    private String departmentName;
    private String divisionName;
    private List<String> earnedMoneyDepartment;
    private List<String> earnedMoneyDivision;
    private List<String> paidMoney;
    private List<String> marginDepartment;
    private List<String> marginDivision   ;
    private List<String> marginBonusBDM;
    private int marginYear;
    private List<Integer> marginQuarter;
}
