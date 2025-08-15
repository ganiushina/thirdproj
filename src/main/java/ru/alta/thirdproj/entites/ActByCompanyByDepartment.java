package ru.alta.thirdproj.entites;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ActByCompanyByDepartment {
    private String num;
    private int actId;
    private String candidate;
    private String company;
    private String projectName;
    private LocalDate dateAct;
    private String organization;
    private Double percentByDepartment;
    private Double sumActNoNDS;
    private Integer departmentId;
    private String departmentName;
    private Double sumByDepartment;
    private Double sumByCompany;
}
