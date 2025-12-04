package ru.alta.thirdproj.entites;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class BonusSchemeRangeTable {
    private List<String> columns;
    private List<Map<String, Object>> rows;
}
