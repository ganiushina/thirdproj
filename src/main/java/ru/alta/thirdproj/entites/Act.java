package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.Date;

@Data
public class Act {

    private String num;
    private int id;
    private Date date;

    private String candidate;
    private String companies;
    private Integer projects;
    private Date datePayment;
    private Double bonus;
    private Date dateForPay;
    private boolean paid;
    private Date paymentRealDate;
    private String employerPaid;

}
