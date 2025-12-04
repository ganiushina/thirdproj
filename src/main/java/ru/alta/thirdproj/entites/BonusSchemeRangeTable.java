package ru.alta.thirdproj.entites;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BonusSchemeRangeTable {
    private List<String> columns;
    private List<List<Object>> rows;
}
