package ru.alta.thirdproj.entites;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class UserSalarySuccess {
    private int userId;      // соответствует user_id
    private String userFio;  // соответствует man_fio
    private LocalDate dateFrom; // соответствует datefrom
    private LocalDate dateTo;   // соответствует dateto
    private int success;     // соответствует success

    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("user_id", "userId");
        COLUMN_MAPPINGS.put("man_fio", "userFio");
        COLUMN_MAPPINGS.put("datefrom", "dateFrom"); // обратите внимание на lowercase
        COLUMN_MAPPINGS.put("dateto", "dateTo");     // обратите внимание на lowercase
        COLUMN_MAPPINGS.put("success", "success");
    }
}
