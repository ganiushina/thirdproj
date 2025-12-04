package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BonusPosition {

    private Integer id;
    private String posName;

    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("id", "id");
        COLUMN_MAPPINGS.put("pos_name", "posName");
    }
}
