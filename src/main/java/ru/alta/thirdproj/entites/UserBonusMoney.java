package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;


@Data
public class UserBonusMoney {

    private Double userBonus;

    private List<Double> userBonusByCandidate;

    private String company;

    private String candidate;

    private Double percent;

}
