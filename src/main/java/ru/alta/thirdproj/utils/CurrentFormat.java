package ru.alta.thirdproj.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CurrentFormat {
    private String formatCurrency(BigDecimal value, NumberFormat formatter) {
        return value != null && value.doubleValue() != 0.0
                ? formatter.format(value.doubleValue())
                : null;
    }
    private String formatDate(Object dateValue) {
        if (dateValue instanceof Date) {
            return ((Date) dateValue).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MMM-dd"));
        }
        return null;
    }


}
