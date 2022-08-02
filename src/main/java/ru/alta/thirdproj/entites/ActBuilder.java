package ru.alta.thirdproj.entites;

import java.util.Date;

public final class ActBuilder {
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

    public ActBuilder withDate(Date date) {
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

    public ActBuilder withDatePayment(Date datePayment) {
        this.datePayment = datePayment;
        return this;
    }

    public ActBuilder withBonus(Double bonus) {
        this.bonus = bonus;
        return this;
    }

    public ActBuilder withDateForPay(Date dateForPay) {
        this.dateForPay = dateForPay;
        return this;
    }

    public ActBuilder withPaid(boolean paid) {
        this.paid = paid;
        return this;
    }

    public ActBuilder withPaymentRealDate(Date paymentRealDate) {
        this.paymentRealDate = paymentRealDate;
        return this;
    }

    public ActBuilder withEmployerPaid(String employerPaid) {
        this.employerPaid = employerPaid;
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
        act.setEmployerPaid(employerPaid);
        return act;
    }
}
