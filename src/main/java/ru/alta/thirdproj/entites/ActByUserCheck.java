package ru.alta.thirdproj.entites;

import lombok.Data;

import java.time.LocalDate;
@Data
public class ActByUserCheck {
    private int actId;
    private LocalDate dateAct;
    private String actNum;
    private String companyName;
    private Double totalNoNds;
    private String projectName;
    private String candidate;
    private String organization;
    private String departmentName;
    private String responsibleUserName;
    private String resecherName;
    private Double summResecher;
    private Double candidatePercent;
    private Double summResponsibleUser;
}
