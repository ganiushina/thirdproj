package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UserEmail {
    private int userId;
    private String userFio;
    private String userEmail;
    private boolean access;

    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("man_id", "userId");
        COLUMN_MAPPINGS.put("man_fio", "userFIO");
        COLUMN_MAPPINGS.put("userEmail", "man_email");

    }
}
