package ru.alta.thirdproj.entites;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public final class ActBuilder {
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
    private String projectName;
    private LocalDate dateAct;

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    DateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");

    private ActBuilder() {
    }

    public static ActBuilder anAct() {
        return new ActBuilder();
    }

    public ActBuilder withNum(String num) {
        this.num = num;
        return this;
    }

    public ActBuilder withId(int id) {
        this.id = id;
        return this;
    }

    public ActBuilder withDate(String date) {
//        this.date = formatter1.format(date);
        this.date = date;
        return this;
    }

    public ActBuilder withCandidate(String candidate) {
        this.candidate = candidate;
        return this;
    }

    public ActBuilder withCompanies(String companies) {
        this.companies = companies;
        return this;
    }

    public ActBuilder withProjects(Integer projects) {
        this.projects = projects;
        return this;
    }

    public ActBuilder withDatePayment(String datePayment) {
        this.datePayment = datePayment;
        return this;
    }

    public ActBuilder withBonus(Double bonus) {
        this.bonus = bonus;
        return this;
    }

    public ActBuilder withDateForPay(String dateForPay) {
        this.dateForPay = dateForPay;
        return this;
    }

    public ActBuilder withPaid(boolean paid) {
        this.paid = paid;
        return this;
    }

    public ActBuilder withPaymentRealDate(String paymentRealDate) {
        this.paymentRealDate = paymentRealDate;
        return this;
    }

    public ActBuilder withPaymentDate(String paymentRealDate) {
        this.paymentRealDate = paymentRealDate;
        return this;
    }

    public ActBuilder withPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
        return this;
    }

    public ActBuilder withEmployerPaid(String employerPaid) {
        this.employerPaid = employerPaid;
        return this;
    }

    public ActBuilder withProjectName(String employerPaid) {
        this.projectName = projectName;
        return this;
    }

    public ActBuilder withDateAct (LocalDate dateAct) {
        this.dateAct = dateAct;
        return this;
    }


    public Act build() {
        Act act = new Act();
        act.setNum(num);
        act.setId(id);
        act.setDate(date);
        act.setCandidate(candidate);
        act.setCompanies(companies);
        act.setProjects(projects);
        act.setDatePayment(datePayment);
        act.setBonus(bonus);
        act.setDateForPay(dateForPay);
        act.setPaid(paid);
        act.setPaymentRealDate(paymentRealDate);
        act.setPaymentDate(paymentDate);
        act.setEmployerPaid(employerPaid);
        act.setProjectName(projectName);
        act.setDateAct(dateAct);
        return act;
    }
}
