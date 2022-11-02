package ru.alta.thirdproj.entites;

import lombok.Data;

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
    private String employerPaid;
    private Double percent;
    private String projectName;

    private String dateAct;

}
