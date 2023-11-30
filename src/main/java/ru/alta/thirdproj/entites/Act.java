package ru.alta.thirdproj.entites;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class Act {
    private String num;
    private int id;
    private String date;
    private String candidate;
    private String companies;
    private Integer projects;
    private String datePayment;
    private Double bonus;
    private String dateForPay;
    private boolean paid;
    private String paymentRealDate;
    private LocalDate paymentDate;
    private String employerPaid;
    private Double percent;
    private String projectName;
    private LocalDate dateAct;
    private String dateClientPay;
    private String bonusRUB;
    private String organization;

}
