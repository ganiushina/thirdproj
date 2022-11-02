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







//    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();
//
//    static {
//        COLUMN_MAPPINGS.put("man_id", "manId");
//        COLUMN_MAPPINGS.put("man_fio", "manFIO");
//        COLUMN_MAPPINGS.put("userPosition", "userPosition");
//        COLUMN_MAPPINGS.put("userDepartment", "userDepartment");
//    }

}
