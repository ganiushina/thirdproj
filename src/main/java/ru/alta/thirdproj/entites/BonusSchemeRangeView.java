package ru.alta.thirdproj.entites;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class BonusSchemeRangeView {

    private String positionName;
    private String schemeName;
    private String percentRange;
    private Timestamp dateScheme;
}
