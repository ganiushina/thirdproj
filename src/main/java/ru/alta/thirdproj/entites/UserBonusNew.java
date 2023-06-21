package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

}
