package ru.alta.thirdproj.entites;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExtraAct {
    Integer actId;
    Double totalSum;
    Double percent;
    Integer paid;
    String company;
    String candidate;
}
