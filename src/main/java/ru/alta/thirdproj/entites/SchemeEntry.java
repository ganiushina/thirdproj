package ru.alta.thirdproj.entites;

import lombok.Data;

@Data
public class SchemeEntry {
    private Long schemeId;
    private Long positionId;
    private Long gapId;
    private java.math.BigDecimal limits;
    private java.time.LocalDate dateScheme;
}
