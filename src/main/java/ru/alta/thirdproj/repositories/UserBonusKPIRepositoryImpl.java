package ru.alta.thirdproj.repositories;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import ru.alta.thirdproj.entites.UserBonusKPI;
import ru.alta.thirdproj.entites.UserBonusKPIDetail;
import ru.alta.thirdproj.entites.UserBonusKPIMain;
import ru.alta.thirdproj.entites.UserBonusNew;


import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserBonusKPIRepositoryImpl {

    private final Sql2o sql2o;

    private static final String SELECT_BONUS_KPI_QUERY = "SELECT * FROM userBonusKPI(:date1, :date2)";

    public UserBonusKPIRepositoryImpl(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public List<UserBonusKPIMain> getUserBonusKPIList(LocalDate dateFrom, LocalDate dateTo) {
        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_BONUS_KPI_QUERY, false)
                    .addParameter("date1", dateFrom)
                    .addParameter("date2", dateTo);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> rawData = table.asList();

            // Создаем мапу для группировки по пользователям
            Map<Integer, UserBonusKPIMain> userMap = new HashMap<>();
            Locale ruLocale = new Locale("ru", "RU");
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(ruLocale);

            for (Map<String, Object> row : rawData) {
                Integer userId = (Integer) row.get("user_id");

                // Создаем или получаем существующий основной объект
                UserBonusKPIMain main = userMap.computeIfAbsent(userId, id -> {
                    UserBonusKPIMain newMain = new UserBonusKPIMain();
                    newMain.setUserId(id);
                    newMain.setFio((String) row.get("man_fio"));
                    newMain.setPosition((String) row.get("position"));
                    newMain.setUserBonusKPIDetails(new ArrayList<>());
                    return newMain;
                });

                // Создаем детализированный объект
                UserBonusKPIDetail detail = new UserBonusKPIDetail();

                // Обрабатываем числовые значения
                BigDecimal bonus = (BigDecimal) row.get("bonus");
                BigDecimal bestBonus = (BigDecimal) row.get("best_bonus");
                BigDecimal bestBonusMarketing = (BigDecimal) row.get("best_bonus_kpi_marketing");
                BigDecimal allBonus = (BigDecimal) row.get("all_bonus");

                detail.setBonus(bonus != null ? bonus.doubleValue() : null);
                detail.setBestBonus(bestBonus != null ? bestBonus.doubleValue() : null);
                detail.setBestBonusKPIMarketing(bestBonusMarketing != null ? bestBonusMarketing.doubleValue() : null);
                detail.setAllBonus(allBonus != null ? allBonus.doubleValue() : null);

                // Обрабатываем дату и месяц
                detail.setMonthName((String) row.get("mon"));
                detail.setMonthNum((Integer) row.get("mont")); // Нужно реализовать этот метод

                // Для даты можно использовать либо дату из данных, либо сконструировать
               // LocalDate recordDate = constructDateFromRow(row); // Нужно реализовать
             //   detail.setDateKPI(recordDate);

                // Добавляем детали к основному объекту
                main.getUserBonusKPIDetails().add(detail);
            }

            return new ArrayList<>(userMap.values());
        }
    }


    private LocalDate constructDateFromRow(Map<String, Object> row) {
        // Пример реализации - нужно адаптировать под ваши данные
        Integer year = (Integer) row.get("year"); // если есть в запросе
        Integer monthNum = (Integer) row.get("mont");
        return LocalDate.of(year != null ? year : LocalDate.now().getYear(),
                monthNum, 1);
    }
}
