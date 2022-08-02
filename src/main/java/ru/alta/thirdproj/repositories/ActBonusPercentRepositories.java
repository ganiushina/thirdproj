package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.ActBuilder;
import ru.alta.thirdproj.entites.EmployerNew;
import ru.alta.thirdproj.entites.PaymentSuccess;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ActBonusPercentRepositories {

    private final Sql2o sql2o;

    public ActBonusPercentRepositories(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    private static final String SELECT_ACT_QUERY = "SELECT distinct ab.id act_id, isnull(ebx.bonus_percent, '') per, CASE WHEN isnull(ebx.act_id, 0) = 0 then 0 ELSE 1 end paied,\n" +
            "ab.total_no_nds, ab.company_name, ab.candidate FROM dbo.act_buh ab\n" +
            "LEFT JOIN (SELECT eb.act_id, eb.bonus_percent FROM extra_bonus eb \n" +
            "\t\t\tJOIN act_buh ab ON ab.id= eb.act_id\n" +
            "\t\t\tWHERE eb.employer_id = :employer_id AND ab.date_act BETWEEN :date1 AND :date2) ebx ON ebx.act_id = ab.id\n" +
            "            WHERE ab.date_act BETWEEN :date1 AND :date2";

    private static final String SAVE_ACT_EXTRA_BONUS = "EXECUTE [dbo].[ExtraBonusInsert]" +
            " :employer_id\n" +
            " ,:user_id\n" +
            " ,:act_id\n" +
            " ,:bonus_percent\n" +
            " ,:date_add";

    private static final String DELETE_ACT_EXTRA_BONUS = "DELETE extra_bonus WHERE employer_id = :employer_id and act_id = :act_id";




    public List<Act> getAllAct(LocalDate date1, LocalDate date2, Integer employerId) {

        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_ACT_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2)
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
                    if (entry.getKey().equals("total_no_nds")) {
                        act.setBonus((double) entry.getValue());
                    }

                    if (entry.getKey().equals("company_name")) {
                        act.setCompanies((String) entry.getValue());
                    }

                    if (entry.getKey().equals("candidate")) {
                        act.setCandidate((String) entry.getValue());
                    }

                    if (entry.getKey().equals("paied")) {
                        boolean b = ((Integer) entry.getValue() == 1);
                        act.setPaid(b);
                    }

                    if (entry.getKey().equals("per")) {
                        act.setPercent((double) entry.getValue());
                    }

                }
                actList.add(act);

            }
            return actList;
        }
    }

    @Transactional
    public void saveExtraBonus(int employerId, int userId, int actId, Double percent) {
        try (Connection connection = sql2o.open()) {
            connection.createQuery(SAVE_ACT_EXTRA_BONUS, false)
                    .addParameter("user_id", userId)
                    .addParameter("employer_id", employerId )
                    .addParameter("act_id", actId)
                    .addParameter("bonus_percent", percent)
                    .addParameter("date_add", new Date())
                    .executeUpdate();
        }
    }

    @Transactional
    public void deleteExtraBonus(int employerId, int actId) {
        try (Connection connection = sql2o.open()) {
            connection.createQuery(DELETE_ACT_EXTRA_BONUS, false)
                    .addParameter("employer_id", employerId )
                    .addParameter("act_id", actId)
                    .executeUpdate();
        }
    }


}
