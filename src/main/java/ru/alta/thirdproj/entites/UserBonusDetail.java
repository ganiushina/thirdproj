package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class UserBonusDetail {
    private Double moneyByCandidate;
    private Integer percent;
    private Double sumUser;
    private String companyName;
    private String candidateName;
    private Integer month;
    private String monthName;
    private String monthMoneyName;
    private String monthSummName;
    private Integer year;
    private Integer actId;
    private String extraBonusAct;
    private String moneyByCandidateRUB;
    private String sumUserRUB;
    private String moneyAllRUBPerMonth;

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        UserBonusDetail that = (UserBonusDetail) o;
//        return userId == that.userId && Objects.equals(fio, that.fio) && Objects.equals(position, that.position) && Objects.equals(department, that.department) && Objects.equals(moneyAll, that.moneyAll) && Objects.equals(moneyByCandidate, that.moneyByCandidate) && Objects.equals(percent, that.percent) && Objects.equals(sumTotal, that.sumTotal) && Objects.equals(sumUser, that.sumUser) && Objects.equals(companyName, that.companyName) && Objects.equals(candidateName, that.candidateName) && Objects.equals(month, that.month) && Objects.equals(monthName, that.monthName) && Objects.equals(monthMoneyName, that.monthMoneyName) && Objects.equals(monthSummName, that.monthSummName) && Objects.equals(year, that.year) && Objects.equals(actList, that.actList) && Objects.equals(extraBonusAct, that.extraBonusAct) && Objects.equals(moneyAllRUB, that.moneyAllRUB) && Objects.equals(moneyByCandidateRUB, that.moneyByCandidateRUB) && Objects.equals(sumTotalRUB, that.sumTotalRUB) && Objects.equals(sumUserRUB, that.sumUserRUB) && Objects.equals(moneyAllRUBPerMonth, that.moneyAllRUBPerMonth);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(userId, fio, position, department, moneyAll, moneyByCandidate, percent, sumTotal, sumUser, companyName, candidateName, month, monthName, monthMoneyName, monthSummName, year, actList, extraBonusAct, moneyAllRUB, moneyByCandidateRUB, sumTotalRUB, sumUserRUB, moneyAllRUBPerMonth);
//    }
}
