package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class UserSalaryDetail {
    private long userId;
    private String userFio;
    private String department;
    private String position;
    private List<Salary> salaryList;
}
