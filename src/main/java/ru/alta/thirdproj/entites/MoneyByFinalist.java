package ru.alta.thirdproj.entites;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MoneyByFinalist {
    private int projectId;
    private String companyName;
    private String manFio;
    private Integer manId;
    private BigDecimal projectFee;
    private String projectName;
    private LocalDate recruitingToWork;
    private String recruitingToWorkString;
    private Integer projectDelayPay;
    private String projectFeeRUB;
    private LocalDate recruitingMoneyToWork;
    private String recruitingMoneyToWorkString;

}
