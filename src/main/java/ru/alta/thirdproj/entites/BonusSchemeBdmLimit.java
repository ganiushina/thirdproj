package ru.alta.thirdproj.entites;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Data
public class BonusSchemeBdmLimit {

    private Integer id;
    private BigDecimal limits;
    private Integer positionId;
    private Integer gapId;
    private Integer divisionId;
    private Timestamp schemeLimitsDate;

    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("id", "id");
        COLUMN_MAPPINGS.put("limits", "limits");
        COLUMN_MAPPINGS.put("position_id", "positionId");
        COLUMN_MAPPINGS.put("gap_id", "gapId");
        COLUMN_MAPPINGS.put("division_id", "divisionId");
        COLUMN_MAPPINGS.put("scheme_limits_date", "schemeLimitsDate");
    }
}
