package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.alta.thirdproj.entites.PaymentSuccess;
import ru.alta.thirdproj.entites.UserEmail;

import java.time.LocalDate;
import java.util.*;


@Component
public class EmailUserRepositoriesImpl {
    private final Sql2o sql2o;

    public EmailUserRepositoriesImpl(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }
    private static final String SELECT_USER_EMAILS = "SELECT * FROM [dbo].[GetUserEmail] ()";

    private static final String SAVE_PERIOD_SUCCESS = "insert paymentPeriodSuccess values(:user_id, :dateFrom, :dateTo, :success)";

    public List<UserEmail> getUserBirthday() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_USER_EMAILS, false)
                    .setColumnMappings(UserEmail.COLUMN_MAPPINGS)
                    .executeAndFetch(UserEmail.class);
        }
    }


    @Transactional
    public void save(int userId , LocalDate dateFrom, LocalDate dateTo, int success)
    {
        try (Connection connection = sql2o.open()) {
            connection.createQuery(SAVE_PERIOD_SUCCESS, false)
                    .addParameter("user_id", userId)
                    .addParameter("dateFrom",dateFrom )
                    .addParameter("dateTo", dateTo)
                    .addParameter("success", success)
                    .executeUpdate();
            connection.commit();

        }
    }
}