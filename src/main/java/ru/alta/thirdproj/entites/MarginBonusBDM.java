package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MarginBonusBDM {
    private String departmentName;
    private String divisionName;
    private int marginYear;
    // Кварталы направления, по строке на квартал
    private List<MarginQuarterRow> quarterRows = new ArrayList<>();
}
