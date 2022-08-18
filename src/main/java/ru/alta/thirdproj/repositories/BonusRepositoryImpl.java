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
            for (int i = 0; i < userBonusesList.size(); i++) {
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

    public List<UserBonusNew> getUserBonusList(LocalDate date1, LocalDate date12) {

        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_USER_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date12)
                    .addParameter("user_id", "")
                    .addParameter("department_id", "");

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            List<UserBonusNew> userBonusNewList = new ArrayList<>();

            for (Map<String, Object> n : list) {

                UserBonusNew userBonusNew = new UserBonusNew();

                List<Double> moneyAll = new ArrayList<>();
                List<Double> moneyByCandidate = new ArrayList<>();
                List<Integer> percent = new ArrayList<>();
                List<Double> sumTotal = new ArrayList<>();
                List<Double> sumUser = new ArrayList<>();
                List<String> companyName = new ArrayList<>();
                List<String> candidateName = new ArrayList<>();
                List<String> monthName = new ArrayList<>();
                List<Integer> month = new ArrayList<>();
                List<Integer> year = new ArrayList<>();

                List<String> monthMoneyName = new ArrayList<>();
                List<String> monthSummName = new ArrayList<>();

                boolean isNotNew = false;
                String manFIO = null;

                for (var entry : n.entrySet()) {

                    if (entry.getKey().equals("man_id")) {
                        userBonusNew.setUserId((Integer) entry.getValue());

                    }
                    else if (entry.getKey().equals("man_fio")) {
                        for (int j = 0; j < userBonusNewList.size(); j++) {
                            if (entry.getKey().equals("man_fio")) {
                                if (userBonusNewList.get(j).getFio().equals(entry.getValue())) {
                                    manFIO = (String) entry.getValue();//
                                    isNotNew = true;
                                }
                            }
                        }

                        userBonusNew.setFio((String) entry.getValue());
                    }

                    else if (entry.getKey().equals("dep_name"))
                        userBonusNew.setDepartment((String) entry.getValue());

                    else if (entry.getKey().equals("pos_name"))
                        userBonusNew.setPosition((String) entry.getValue());

                    else if (entry.getKey().equals("candidate")) {
                        candidateName.add((String) entry.getValue());

                    }
                    if (entry.getKey().equals("company_name")) {
                        companyName.add((String) entry.getValue());

                    }
                    else if (entry.getKey().equals("persent")) {
                        percent.add((Integer) entry.getValue());
                    }

                    else if (entry.getKey().equals("money_itog")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        moneyAll.add(d);
                    }

                    else if (entry.getKey().equals("money_by_candidate")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        moneyByCandidate.add(d);
                    }

                    else if (entry.getKey().equals("summ_total")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        sumTotal.add(d);
                    }

                    else if (entry.getKey().equals("summ_user")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        sumUser.add(d);
                    }

                    else if (entry.getKey().equals("mon")) {
                        month.add((Integer) entry.getValue());
                    }
                    else if (entry.getKey().equals("mont")) {
                        monthName.add((String) entry.getValue());
                    }

                    else if (entry.getKey().equals("ya")) {
                        year.add((Integer) entry.getValue());
                    }
                }

                if (BigDecimal.ZERO.compareTo(BigDecimal.valueOf(moneyAll.get(0))) != 0)
                    monthMoneyName.add(moneyAll.get(0) + " за " + monthName.get(0));
                else monthMoneyName.add(String.valueOf(moneyAll.get(0)));

                if (BigDecimal.ZERO.compareTo(BigDecimal.valueOf(sumTotal.get(0))) != 0)
                    monthSummName.add(sumTotal.get(0) + " за " + monthName.get(0));
                else monthSummName.add(String.valueOf(sumTotal.get(0)));

                userBonusNew.setCandidateName(candidateName);
                    userBonusNew.setCompanyName(companyName);
                    userBonusNew.setMoneyAll(moneyAll);
                    userBonusNew.setMoneyByCandidate(moneyByCandidate);
                    userBonusNew.setMonth(month);
                    userBonusNew.setPercent(percent);
                    userBonusNew.setSumTotal(sumTotal);
                    userBonusNew.setSumUser(sumUser);
                    userBonusNew.setYear(year);
                    userBonusNew.setMonthName(monthName);
                    userBonusNew.setMonthMoneyName(monthMoneyName);
                    userBonusNew.setMonthSummName(monthSummName);


                    if (isNotNew) {
                        String finalManFIO = manFIO;
                        List<UserBonusNew> result = userBonusNewList.stream()
                                .filter(a -> Objects.equals(a.getFio(), finalManFIO))
                                .collect(Collectors.toList());

                        result.get(0).getCandidateName().add(userBonusNew.getCandidateName().get(0));
                        result.get(0).getCompanyName().add(userBonusNew.getCompanyName().get(0));

                        if (!userBonusNew.getMoneyAll().get(0).equals(result.get(0).getMoneyAll().get(0)))
                            result.get(0).getMoneyAll().add(userBonusNew.getMoneyAll().get(0));

                        result.get(0).getMoneyByCandidate().add(userBonusNew.getMoneyByCandidate().get(0));

                        if (!userBonusNew.getMonthName().get(0).equals(result.get(0).getMonthName().get(0)))
                            result.get(0).getMonthName().add(userBonusNew.getMonthName().get(0));

                        result.get(0).getPercent().add(userBonusNew.getPercent().get(0));
                        if (!userBonusNew.getSumTotal().get(0).equals(result.get(0).getSumTotal().get(0)))
                            result.get(0).getSumTotal().add(userBonusNew.getSumTotal().get(0));
                        result.get(0).getSumUser().add(userBonusNew.getSumUser().get(0));
                        result.get(0).getYear().add(userBonusNew.getYear().get(0));

                        if (!userBonusNew.getMonthMoneyName().get(0).equals(result.get(0).getMonthMoneyName().get(0)))
                            result.get(0).getMonthMoneyName().add(userBonusNew.getMonthMoneyName().get(0));

                        if (!userBonusNew.getMonthSummName().get(0).equals(result.get(0).getMonthSummName().get(0)))
                            result.get(0).getMonthSummName().add(userBonusNew.getMonthSummName().get(0));

                    } else {
                        userBonusNew.setActList(getExtraAct(userBonusNew.getUserId()));
                        userBonusNewList.add(userBonusNew);
                    }

                }

            return userBonusNewList;

        }


    }


}

