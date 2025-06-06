package ru.alta.thirdproj.entites;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserBonusKPIDetail {
    private Double bonus;
    private Double bestBonus;
    private Double bestBonusKPIMarketing;
    private Double AllBonus;
    private int monthNum;
    private String monthName;
//    private LocalDate dateKPI;
}
