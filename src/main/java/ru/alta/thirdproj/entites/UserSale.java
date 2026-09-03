package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;

@Data
public class UserSale {
    private List<String> projectName;
    private List<Integer> responsibleUserId;
    private List<String> responsibleUserName;
    private List<String> summResponsibleUser;
    private List<String> summResearcher;
    private List<Integer> resecherId;
    private List<String> resecherName;
    private List<String> dateAct;
    private List<String> candidate;
    private List<String> actNum;
    private List<String> companyName;
    private Integer departmentId;
    private String  department;
}
