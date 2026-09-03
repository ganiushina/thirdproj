package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import ru.alta.thirdproj.entites.*;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class BonusRepositoryImpl {

    private final Sql2o sql2o;


    private List<UserBonus> userBonusesList;

    private static final String SELECT_USER_QUERY = "SELECT * FROM [fn_User_Bonus_by_Details_New] (:date1, :date2, :user_id, :department_id)";

    private static final String SELECT_EXTRA_BONUS = "SELECT ab.company_name, ab.candidate, ab.id FROM extra_bonus eb\n" +
            "    JOIN dbo.act_buh ab ON ab.id = eb.act_id\n" +
            "    WHERE eb.employer_id = :employer_id and eb.act_id = :act_id";

    private static final String SELECT_ALL_COMPANY_MONEY = "SELECT sum(total_no_nds) FROM (\n" +
            "SELECT DISTINCT pb.act_id, ab.date_act, ab.total_no_nds, ab.company_name, ab.candidate\n" +
            "\tFROM dbo.project_buh pb\n" +
            "\tJOIN dbo.act_buh ab ON ab.id = pb.act_id\n" +
            "\tWHERE convert(date, ab.date_act) BETWEEN :date1 AND :date2 \t\n" +
            "\tAND pb.responsible_user_id IS NOT NULL\n" +
            ") s" ;


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
//            for (int i = 0; i < userBonusesList.size(); i++) {
//                userBonusesList.get(i).setActList(getExtraAct((int) userBonusesList.get(i).getUserId(),
//                                                                    userBonusesList.get(i).()));
//            }
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

    public List<Act> getExtraAct(Integer employerId, List<Integer> actIds) {

        try (Connection connection = sql2o.open()) {

            List<Act> acts = new ArrayList<>();

            for (int i = 0; i < actIds.size(); i++) {

                Query query = connection.createQuery(SELECT_EXTRA_BONUS, false)
                        .addParameter("employer_id", employerId)
                        .addParameter("act_id", actIds.get(i));

                Table table = query.executeAndFetchTable();
                List<Map<String, Object>> list = table.asList();

                if (list.size() > 0) {

                    for (Map<String, Object> n : list) {
                        Act act = ActBuilder.anAct().build();

                        for (var entry : n.entrySet()) {
                            if (entry.getKey().equals("act_id")) {
                                act.setId((Integer) entry.getValue());
                            }

                            else if (entry.getKey().equals("company_name")) {
                                act.setCompanies((String) entry.getValue());
                            }

                            else if (entry.getKey().equals("candidate")) {
                                act.setCandidate((String) entry.getValue());
                            }

                        }
                        acts.add(act);
                    }
                }
            }

            return acts;
        }

    }


    public List<UserBonusMain> getUserBonuses(LocalDate date1, LocalDate date2) {

        try (Connection connection = sql2o.open()) {
            // Выполняем запрос и получаем список строк
            List<Map<String, Object>> rows = connection.createQuery(SELECT_USER_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2)
                    .addParameter("user_id", "")
                    .addParameter("department_id", "")
                    .executeAndFetchTable()
                    .asList();

            // Группируем данные по пользователям
            Map<Integer, UserBonusMain> userBonusMap = new HashMap<>();

            for (Map<String, Object> row : rows) {
                Integer userId = (Integer) row.get("man_id");

                UserBonusMain userBonus = userBonusMap.computeIfAbsent(userId, id -> {
                    UserBonusMain ub = new UserBonusMain();
                    ub.setUserId(id);
                    ub.setFio((String) row.get("man_fio"));
                    ub.setPosition((String) row.get("pos_name"));
                    ub.setDepartment((String) row.get("dep_name"));
                    BigDecimal sumTotal = (BigDecimal) row.get("summ_total");
                    ub.setSumTotal(sumTotal.doubleValue());
                    BigDecimal moneyItog = (BigDecimal) row.get("money_itog");
                    ub.setMoneyAll(moneyItog.doubleValue());

                    ub.setUserBonusDetails(new ArrayList<>());
                    return ub;
                });

                // Добавляем детали
                UserBonusDetail detail = new UserBonusDetail();
                BigDecimal moneyCandidate = (BigDecimal) row.get("money_by_candidate");
                detail.setMoneyByCandidate(moneyCandidate.doubleValue());
                detail.setPercent((Double) row.get("persent"));
                BigDecimal sumUser = (BigDecimal) row.get("summ_user");
                detail.setSumUser(sumUser.doubleValue());
                detail.setCompanyName((String) row.get("company_name"));
                detail.setCandidateName((String) row.get("candidate"));
                detail.setMonth((Integer) row.get("mon"));
                detail.setMonthName((String) row.get("mont"));
                detail.setYear((Integer) row.get("ya"));
                detail.setActId((Integer) row.get("act_id"));

                userBonus.getUserBonusDetails().add(detail);
            }

            return new ArrayList<>(userBonusMap.values());
        }
    }

    public Double getCompanyMoney(LocalDate date1, LocalDate date2) {
        try (Connection connection = sql2o.open()) {
            return   connection.createQuery(SELECT_ALL_COMPANY_MONEY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2)
                    .executeScalar(Double.class);
        }
    }


}

