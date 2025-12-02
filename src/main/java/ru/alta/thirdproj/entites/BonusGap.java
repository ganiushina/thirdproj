package ru.alta.thirdproj.entites;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class BonusGap {

    private Integer gapId;
    private Integer gapPercent;
    private LocalDateTime schemeDate;

    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("gap_id", "gapId");
        COLUMN_MAPPINGS.put("gap_percent", "gapPercent");
        COLUMN_MAPPINGS.put("scheme_date", "schemeDate");
    }
}
