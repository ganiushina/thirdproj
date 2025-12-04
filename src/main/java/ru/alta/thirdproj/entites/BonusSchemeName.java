package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BonusSchemeName {

    private Integer id;
    private String schemeName;

    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("id", "id");
        COLUMN_MAPPINGS.put("scheme_name", "schemeName");
    }
}
