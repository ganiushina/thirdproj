package ru.alta.thirdproj.entites;


import lombok.Data;

import java.util.List;

@Data
public class UserAct {

    private int manId;
    private String manFIO;
    private String userDepartment;
    private List<ProjectBuhAct> actList;

}
