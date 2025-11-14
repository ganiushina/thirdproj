package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.alta.thirdproj.entites.UserBonus;

import java.time.LocalDate;
import java.util.List;

@Component
public class BonusRepositoryImpl {

    private final Sql2o sql2o;
    private final String updatePaymentStatusQuery;

    private static final String SELECT_USER_QUERY = "SELECT * FROM fn_User_Bonus_by_Details (:date1, :date2, :user_id, :department_id)";

    public BonusRepositoryImpl(@Autowired Sql2o sql2o,
                               @Value("${bonus.payment.update.query:EXEC dbo.UpdatePaymentStatus :act_id, :paid, :payment_date}")
                               String updatePaymentStatusQuery) {
        this.sql2o = sql2o;
        this.updatePaymentStatusQuery = updatePaymentStatusQuery;
    }

    public List<UserBonus> getUserBonuses(LocalDate date1, LocalDate date12, Integer userId, Integer departmentId) {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_USER_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date12)
                    .addParameter("user_id", userId)
                    .addParameter("department_id", departmentId)
                    .setColumnMappings(UserBonus.COLUMN_MAPPINGS)
                    .executeAndFetch(UserBonus.class);
        }
    }

    public void updatePaymentStatus(Long actId, boolean paid, LocalDate paymentDate) {
        if (actId == null) {
            return;
        }
        try (Connection connection = sql2o.open()) {
            connection.createQuery(updatePaymentStatusQuery, false)
                    .addParameter("act_id", actId)
                    .addParameter("paid", paid ? 1 : 0)
                    .addParameter("payment_date", paymentDate)
                    .executeUpdate();
        }
    }


}
