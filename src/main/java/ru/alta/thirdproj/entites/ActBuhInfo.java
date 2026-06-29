package ru.alta.thirdproj.entites;

import lombok.Data;

import java.time.LocalDate;

/**
 * Лёгкое DTO для списка актов (act_buh) на странице распределения.
 * Включает флаг — есть ли уже строки в project_buh.
 */
@Data
public class ActBuhInfo {

    private int id;
    private String actNum;
    private LocalDate dateAct;
    private String companyName;
    private Double totalNoNds;
    private String candidate;
    private String projectName;

    /** Количество строк project_buh для этого акта (0 = не распределён) */
    private int distributionRowsCount;

    public boolean isDistributed() {
        return distributionRowsCount > 0;
    }
}
