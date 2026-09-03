package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;

@Data
public class DepartmentUserSales {
    private Integer departmentId;
    private String  department;
    private List<EmployerNew> userSaleList;
}
