package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;


@Data
public class UserSum {

    private Double userSum;

    private List<Double> userSumByCandidate;

}
