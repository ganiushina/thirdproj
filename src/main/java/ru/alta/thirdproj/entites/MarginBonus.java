package ru.alta.thirdproj.entites;

import lombok.Data;

@Data
public class MarginBonus {
    String depName;
    Double amountEarned;
    Double amountPaid;
    Double margin;
    String salaryMonth;
    Integer salaryYear;
    Double amountEarnedAllPeriod;
    Double amountPaidAllPeriod;
    Double marginAllPeriod;

    private String amountEarnedRUB;
    private String amountPaidRUB;
    private String marginRUB;
    private String amountEarnedAllPeriodRUB;
    private String amountPaidAllPeriodRUB;
    private String marginAllPeriodRUB;
}
