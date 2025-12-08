package ru.alta.thirdproj.entites;

import lombok.Data;

@Data
public class FailedProbationActUpdate {
    private Integer actId;
    private Double totalNoNds;
    private String candidate;
    private String departmentName;
    private String responsibleUserName;
    private String resecherName;
    private Double summResecher;
    private Double candidatePercent;
    private Double summResponsibleUser;
}
