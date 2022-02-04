package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.alta.thirdproj.entites.UserBonus;
import ru.alta.thirdproj.entites.UserPaymentBonus;

import java.time.LocalDate;
import java.util.List;

@Component
public class BonusPaymentRepositoryImpl {

    private final Sql2o sql2o;

    private static final String SELECT_BONUS_PAYMENT_QUERY = "SELECT * FROM fn_User_Bonus_Payment (:date1, :date2, :userId, :department_id)";

    public BonusPaymentRepositoryImpl(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public List<UserPaymentBonus> getUserBonuses(LocalDate date1, LocalDate date12, Integer userId, Integer departmentId) {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_BONUS_PAYMENT_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date12)
                    .addParameter("userId", userId)
                    .addParameter("department_id", departmentId)
                    .setColumnMappings(UserPaymentBonus.COLUMN_MAPPINGS)
                    .executeAndFetch(UserPaymentBonus.class);
        }
    }


}
