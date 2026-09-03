package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;

@Data
public class UserBonusKPI {

    private int userId;
    private String fio;
    private String position;
    List<Double> bonus;
    List<Double> bonusBest;
    List<Double> bonusBestMarketing;
    List<Double> bonusAll;
    List<String> month;

    List<String> bonusRUB;
    List<String> bonusBestRUB;
    List<String> bonusAllRUB;
    List<String> bonusBestMarketingRUB;

}
