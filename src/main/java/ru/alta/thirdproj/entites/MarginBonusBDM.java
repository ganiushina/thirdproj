package ru.alta.thirdproj.entites;
import lombok.Data;

@Data
public class MarginBonusBDM {
    private String departmentName;
    private String divisionName;
    private Double allMoney;
    private Double marginDepartment;
    private Double marginDivision   ;
    private Double marginBonusBDM;
    private int marginYear;
    private int marginQuarter;
}
