package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.alta.thirdproj.entites.PersonalData;

import java.time.LocalDate;
import java.util.List;

@Component
public class PersonalDataRepository {

    private static final String SELECT_ACT_PAYMENT_QUERY = "select * from fn_personal_data_cnt_by_user(:date1, :date2)";

    private final Sql2o sql2o;

    public PersonalDataRepository(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public List<PersonalData> getPersonalData(LocalDate date1, LocalDate date2) {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_ACT_PAYMENT_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2)
                    .setColumnMappings(PersonalData.COLUMN_MAPPINGS)
                    .executeAndFetch(PersonalData.class);
        }
    }
}
