package ru.alta.thirdproj.entites;

import lombok.Data;

/**
 * DTO — одна строка таблицы project_buh.
 * Один акт может содержать несколько таких строк (несколько консультантов).
 */
@Data
public class ActDistributionRow {

    private int id;
    private int actId;

    // Консультант (responsible_user)
    private String consultantName;
    private Integer consultantId;
    private Double consultantPercent;          // percent_responsible_user_complicity
    private Double consultantSum;              // summ_responsible_user
    private Double consultantPercentOfTotal;   // percent_responsible_user_by_candidate_percent

    // Ресечер
    private String researcherName;
    private Integer researcherId;
    private Double researcherPercent;          // percent_resecher_complicity
    private Double researcherSum;              // summ_resecher
    private Double researcherPercentOfTotal;   // percent_reseacher_by_candidate_percent

    // Отдел консультанта
    private String department;
    private Integer departmentId;

    // Отдел ресечера
    private String departmentResearcher;
    private Integer departmentResearcherId;

    // Тимлид
    private String teamLeaderName;
    private Integer teamLeaderId;

    // Город консультанта
    private String city;
    private Integer cityId;
}
