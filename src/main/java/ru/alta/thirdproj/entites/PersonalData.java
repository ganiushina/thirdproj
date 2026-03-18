package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PersonalData {

    private int userId;
    private int cntTotal;
    private int cntConfirmed;
    private int cntNotConfirmed;

    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("user_id", "userId");
        COLUMN_MAPPINGS.put("cnt_total", "cntTotal");
        COLUMN_MAPPINGS.put("cnt_confirmed", "cntConfirmed");
        COLUMN_MAPPINGS.put("cnt_not_confirmed", "cntNotConfirmed");
    }
}
