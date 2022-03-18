package ru.alta.thirdproj.entites;


import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Employer {

    private Long manId;

    private String manFIO;

    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("man_id", "manId");
        COLUMN_MAPPINGS.put("man_fio", "manFIO");
    }

}
