package ru.alta.thirdproj.entites;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectBuhAct {
    private String actNum;
    private String candidate;
    private String companies;
    private Double summUser;
    private int userId;
    private String UserName;
    private String projectName;
    private String dateAct;

}
