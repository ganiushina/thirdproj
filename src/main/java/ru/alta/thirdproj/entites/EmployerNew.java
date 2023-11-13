package ru.alta.thirdproj.entites;


import lombok.Data;

import java.util.List;

@Data
public class EmployerNew {

    private int manId;
    private String manFIO;
    private String userDepartment;
    private List<Act> actList;
    private Double allBonus;
    private List<Integer> percent;
    private String allBonusRUB;

}
