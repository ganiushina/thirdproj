package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class UserBonusNew {

    private int userId;

    private String fio;

    private String position;

    private String department;

    private List<Double> moneyAll;

    private List<Double> moneyByCandidate;

    private List<Integer> percent;

    private List<Double> sumTotal;

    private List<Double> sumUser;

    private List<String> companyName;

    private List<String> candidateName;

    private List<Integer> month;

    private List<String> monthName;

    private List<String> monthMoneyName;

    private List<String> monthSummName;

    private List<Integer> year;

    private List<Act> actList;

    private List<String> extraBonusAct;

    private List<String> moneyAllRUB;
    private List<String> moneyByCandidateRUB;
    private List<String> sumTotalRUB;
    private List<String> sumUserRUB;

    private List<String> moneyAllRUBPerMonth;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserBonusNew that = (UserBonusNew) o;
        return userId == that.userId && Objects.equals(fio, that.fio) && Objects.equals(position, that.position) && Objects.equals(department, that.department) && Objects.equals(moneyAll, that.moneyAll) && Objects.equals(moneyByCandidate, that.moneyByCandidate) && Objects.equals(percent, that.percent) && Objects.equals(sumTotal, that.sumTotal) && Objects.equals(sumUser, that.sumUser) && Objects.equals(companyName, that.companyName) && Objects.equals(candidateName, that.candidateName) && Objects.equals(month, that.month) && Objects.equals(monthName, that.monthName) && Objects.equals(monthMoneyName, that.monthMoneyName) && Objects.equals(monthSummName, that.monthSummName) && Objects.equals(year, that.year) && Objects.equals(actList, that.actList) && Objects.equals(extraBonusAct, that.extraBonusAct) && Objects.equals(moneyAllRUB, that.moneyAllRUB) && Objects.equals(moneyByCandidateRUB, that.moneyByCandidateRUB) && Objects.equals(sumTotalRUB, that.sumTotalRUB) && Objects.equals(sumUserRUB, that.sumUserRUB) && Objects.equals(moneyAllRUBPerMonth, that.moneyAllRUBPerMonth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, fio, position, department, moneyAll, moneyByCandidate, percent, sumTotal, sumUser, companyName, candidateName, month, monthName, monthMoneyName, monthSummName, year, actList, extraBonusAct, moneyAllRUB, moneyByCandidateRUB, sumTotalRUB, sumUserRUB, moneyAllRUBPerMonth);
    }
}
