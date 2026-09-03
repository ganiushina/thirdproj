package ru.alta.thirdproj.entites;

import lombok.Data;

/**
 * Одна строка выборки маржи — направление за один квартал.
 * Все суммы лежат в одном объекте, поэтому колонки таблицы не могут разъехаться
 * между собой: раньше каждая колонка была отдельным списком и пропуск значения
 * (ноль или повтор суммы) сдвигал все значения ниже на строку вверх.
 */
@Data
public class MarginQuarterRow {
    private Integer quarter;
    private String earnedMoneyDepartment;
    private String earnedMoneyDivision;
    private String paidMoney;
    private String marginDepartment;
    private String marginDivision;
    private String marginBonusBDM;
    // Маржа за вычетом бонуса BDM
    private String marginWithBdmBonus;
    // Процент маржи от заработанного: "без бонуса BDM / с учётом бонуса BDM"
    private String marginPercent;
    // Маржа числом — для подсчёта итогов
    private Double marginDepartmentSum;
}
