package ru.alta.thirdproj.entites;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SchemeRow {
    private String positionName;              // колонка [должность] = pos_name
    private String schemeName;               // колонка [позиция + схема] = scheme_name
    private java.time.LocalDate dateScheme;  // колонка [date_scheme]
    private java.util.List<BonusCell> cells; // динамический набор процентов

    public SchemeRow(String positionName,
                     String schemeName,
                     LocalDate dateScheme,
                     List<BonusCell> cells) {
        this.positionName = positionName;
        this.schemeName = schemeName;
        this.dateScheme = dateScheme;
        this.cells = cells;
    }
}
