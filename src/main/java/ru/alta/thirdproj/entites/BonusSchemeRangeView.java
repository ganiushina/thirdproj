package ru.alta.thirdproj.entites;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Data
public class BonusSchemeRangeView {

    private String positionName;
    private String schemeName;
    private BigDecimal limitFrom;
    private BigDecimal limitTo;
    private BigDecimal gapPercent;
    private Timestamp dateScheme;

    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("pos_name", "positionName");
        COLUMN_MAPPINGS.put("scheme_name", "schemeName");
        COLUMN_MAPPINGS.put("limit_from", "limitFrom");
        COLUMN_MAPPINGS.put("limit_to", "limitTo");
        COLUMN_MAPPINGS.put("gap_percent", "gapPercent");
        COLUMN_MAPPINGS.put("date_scheme", "dateScheme");
    }
}
