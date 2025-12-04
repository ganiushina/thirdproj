package ru.alta.thirdproj.entites;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class BonusSchemeEntry {

    private Integer id;
    private Integer schemeId;
    private BigDecimal limits;
    private Integer positionId;
    private Integer gapId;
    private LocalDate dateScheme;

    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("id", "id");
        COLUMN_MAPPINGS.put("scheme_id", "schemeId");
        COLUMN_MAPPINGS.put("limits", "limits");
        COLUMN_MAPPINGS.put("position_id", "positionId");
        COLUMN_MAPPINGS.put("gap_id", "gapId");
        COLUMN_MAPPINGS.put("date_scheme", "dateScheme");
    }
}
