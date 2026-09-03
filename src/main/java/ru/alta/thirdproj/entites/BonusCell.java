package ru.alta.thirdproj.entites;

import lombok.Data;

@Data
public class BonusCell {
    private String gapLabel;   // например "8%", "12%" (имя колонки)
    private String rangeValue; // текст диапазона из range_value: "360000 - 480000" или "850000+"
    public BonusCell(String gapLabel, String rangeValue) {
        this.gapLabel = gapLabel;
        this.rangeValue = rangeValue;
    }

}
