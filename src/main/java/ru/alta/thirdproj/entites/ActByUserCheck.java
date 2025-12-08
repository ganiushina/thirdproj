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
    private Double resecherPercent;
    private Double candidatePercent;
    private Double summResponsibleUser;
    private String resecherDepartmentName;

    public String getResecherDisplay() {
        String name = resecherName != null ? resecherName.trim() : "";
        String dep  = resecherDepartmentName != null ? resecherDepartmentName.trim() : "";

        if (!name.isEmpty() && !dep.isEmpty()) {
            return name + " (" + dep + ")" ;
        } else if (!name.isEmpty()) {
            return name;
        } else {
            return dep; // если имени нет, но департамент есть
        }
    }
}
