package ru.alta.thirdproj.repositories;

import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import ru.alta.thirdproj.entites.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class BonusRepositoryImpl {

    private final Sql2o sql2o;


    private List<UserBonus> userBonusesList;

    private static final String SELECT_USER_QUERY = "SELECT * FROM fn_User_Bonus_by_Details (:date1, :date2, :user_id, :department_id)";

    private static final String SELECT_EXTRA_BONUS = "SELECT ab.company_name, ab.candidate, ab.id FROM extra_bonus eb\n" +
            "    JOIN dbo.act_buh ab ON ab.id = eb.act_id\n" +
            "    WHERE eb.employer_id = :employer_id";



    public BonusRepositoryImpl(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public List<UserBonus> getUserBonuses(LocalDate date1, LocalDate date12, Integer userId, Integer departmentId) {
        try (Connection connection = sql2o.open()) {
            userBonusesList = connection.createQuery(SELECT_USER_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date12)
                    .addParameter("user_id", userId)
                    .addParameter("department_id", departmentId)
                    .setColumnMappings(UserBonus.COLUMN_MAPPINGS)
                    .executeAndFetch(UserBonus.class);
            for (int i = 0; i < userBonusesList.size() ; i++) {
                userBonusesList.get(i).setActList(getExtraAct((int) userBonusesList.get(i).getUserId()));
            }
            return userBonusesList;
        }
    }

    public List<UserBonus> findByFioAndDepartment(String fio, String department) {
        List<UserBonus> userBonusList = null;
        
        if (!fio.equals(""))
            userBonusList = userBonusesList
                    .stream()
                    .filter(c -> (c.getFio().equals(fio)))
                    .collect(Collectors.toList());
        if (!department.equals(""))
            userBonusList = userBonusesList
                    .stream()
                    .filter(c -> c.getDepartment().equals(department))
                    .collect(Collectors.toList());

        if (!department.equals("") && !fio.equals(""))
            userBonusList = userBonusesList
                    .stream()
                    .filter(c -> (c.getFio().equals(fio) && c.getDepartment().equals(department)))
                    .collect(Collectors.toList());
        return userBonusList;
    }

    public List<Act> getExtraAct(Integer employerId) {

        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_EXTRA_BONUS, false)
                    .addParameter("employer_id", employerId);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            List<Act> actList = new ArrayList<>();

            for (Map<String, Object> n : list) {

                Act act = ActBuilder.anAct().build();

                for (var entry : n.entrySet()) {
                    if (entry.getKey().equals("act_id")) {
                        act.setId((Integer) entry.getValue());

                    }

                    if (entry.getKey().equals("company_name")) {
                        act.setCompanies((String) entry.getValue());
                    }

                    if (entry.getKey().equals("candidate")) {
                        act.setCandidate((String) entry.getValue());
                    }

                    act.setPaid(true);

                }
                actList.add(act);

            }
            return actList;
        }
    }

}
