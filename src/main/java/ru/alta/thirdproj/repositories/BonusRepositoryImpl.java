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
import java.text.SimpleDateFormat;
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
            "    WHERE eb.employer_id = :employer_id and eb.act_id = :act_id";


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


                String tmpCanditate = "";

                Double moneyAllTmp = null;
                Double sumTotalTmp = null;

                List<Double> moneyAll = new ArrayList<>();
                List<Double> moneyByCandidate = new ArrayList<>();
                List<Integer> percent = new ArrayList<>();
                List<Double> sumTotal = new ArrayList<>();
                List<Double> sumUser = new ArrayList<>();
                List<String> companyName = new ArrayList<>();
                List<String> candidateName = new ArrayList<>();
                List<String> extraBonusAct = new ArrayList<>();

                List<String> monthName = new ArrayList<>();
                List<Integer> month = new ArrayList<>();
                List<Integer> year = new ArrayList<>();

                List<String> monthMoneyName = new ArrayList<>();
                List<String> monthSummName = new ArrayList<>();

                List<Integer> actIds = new ArrayList<>();


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
                        if (!(entry.getValue()).equals("")) {

                            String tmpStr = (String) entry.getValue();
                            if (tmpStr.indexOf("%") > 0)
                                tmpCanditate = ((String) entry.getValue());

                            candidateName.add((String) entry.getValue());
                            userBonusNew.setCandidateName(candidateName);
                        }

                    }
                    if (entry.getKey().equals("company_name")) {
                        if (!(entry.getValue()).equals("")) {
                            companyName.add((String) entry.getValue());
                            userBonusNew.setCompanyName(companyName);
                        }

                    }
                    else if (entry.getKey().equals("persent")) {
                        if ((Integer) entry.getValue() != 0) {
                            percent.add((Integer) entry.getValue());
                            userBonusNew.setPercent(percent);
                        }
                    }

                    else if (entry.getKey().equals("money_itog")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            moneyAll.add(d);
                            moneyAllTmp = d;
                            userBonusNew.setMoneyAll(moneyAll);
                        }

                    }

                    else if (entry.getKey().equals("money_by_candidate")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            moneyByCandidate.add(d);
                            userBonusNew.setMoneyByCandidate(moneyByCandidate);
                        }
                    }

                    else if (entry.getKey().equals("summ_total")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            sumTotal.add(d);
                            sumTotalTmp = d;
                            userBonusNew.setSumTotal(sumTotal);
                        }
                    }

                    else if (entry.getKey().equals("summ_user")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            sumUser.add(d);
                            userBonusNew.setSumUser(sumUser);
                        }
                    }

                    else if (entry.getKey().equals("mon")) {
                        month.add((Integer) entry.getValue());
                    }
                    else if (entry.getKey().equals("mont")) {
                        if (!(entry.getValue()).equals("")) {
                            monthName.add((String) entry.getValue());
                            userBonusNew.setMonthName(monthName);
                        }
                    }

                    else if (entry.getKey().equals("ya")) {
                        year.add((Integer) entry.getValue());
                    }

                    else if (entry.getKey().equals("act_id")) {
                        actIds.add((Integer) entry.getValue());
                    }

                }



                if (moneyAll.size() > 0)
                    monthMoneyName.add(String.valueOf(moneyAll.get(0)));
//                else
//                    monthMoneyName.add(String.valueOf(moneyAll.get(0)));

                if (sumTotal.size() > 0)
                    monthSummName.add(String.valueOf(sumTotal.get(0))) ;
//                else
//                    monthSummName.add(String.valueOf(sumTotal.get(0)));







                userBonusNew.setMonth(month);



                userBonusNew.setYear(year);

                userBonusNew.setMonthMoneyName(monthMoneyName);
                userBonusNew.setMonthSummName(monthSummName);

                if (!tmpCanditate.equals("")) {
                    extraBonusAct.add(tmpCanditate + " " + userBonusNew.getCompanyName().get(0));
                    userBonusNew.setExtraBonusAct(extraBonusAct);
                }
                else {
                    userBonusNew.setExtraBonusAct(extraBonusAct);
                }

                    if (isNotNew) {
                        String finalManFIO = manFIO;
                        List<UserBonusNew> result = userBonusNewList.stream()
                                .filter(a -> Objects.equals(a.getFio(), finalManFIO))
                                .collect(Collectors.toList());


                        if (userBonusNew.getCandidateName() != null) {
                            if (result.get(0).getCandidateName() == null)
                                result.get(0).setCandidateName(userBonusNew.getCandidateName());
                            else
                                result.get(0).getCandidateName().add(userBonusNew.getCandidateName().get(0));
                        }

                        if (userBonusNew.getCompanyName() != null) {
                            if (result.get(0).getCompanyName() == null)
                                result.get(0).setCompanyName(userBonusNew.getCompanyName());
                            else
                                result.get(0).getCompanyName().add(userBonusNew.getCompanyName().get(0));
                        }

                        if (moneyAllTmp != null) {
                            if (result.get(0).getMoneyAll() == null ) {
                                result.get(0).setMoneyAll(userBonusNew.getMoneyAll());
                                result.get(0).getMonthMoneyName().add(userBonusNew.getMonthMoneyName().get(0));
                            } else if (!result.get(0).getMoneyAll().contains(moneyAllTmp) && userBonusNew.getMonthMoneyName().size() > 0) {
                                result.get(0).getMoneyAll().add(moneyAllTmp);
                                result.get(0).getMonthMoneyName().add(userBonusNew.getMonthMoneyName().get(0));
                            }
                        }

                        if (sumTotalTmp != null) {
                            if (result.get(0).getSumTotal() == null ) {
                                result.get(0).setSumTotal(userBonusNew.getSumTotal());
                                result.get(0).getMonthSummName().add(userBonusNew.getMonthSummName().get(0));
                            } else if (!result.get(0).getSumTotal().contains(sumTotalTmp) && sumTotalTmp != 0.0) {
                                result.get(0).getSumTotal().add(sumTotalTmp);
                                result.get(0).getMonthSummName().add(userBonusNew.getMonthSummName().get(0));
                            }
                        }

                        if (userBonusNew.getMoneyByCandidate() != null) {
                            if (result.get(0).getMoneyByCandidate() == null) {
                                result.get(0).setMoneyByCandidate(userBonusNew.getMoneyByCandidate());
                            } else result.get(0).getMoneyByCandidate().add(userBonusNew.getMoneyByCandidate().get(0));
                        }


                        if (userBonusNew.getMonthName() != null) {
                            if (result.get(0).getMonthName() == null)
                                result.get(0).setMonthName(userBonusNew.getMonthName());
                            else
                                if (!result.get(0).getMonthName().contains(userBonusNew.getMonthName().get(0))) {
                                    result.get(0).getMonthName().add(userBonusNew.getMonthName().get(0));
                                    //   userBonusNew.getMonthName().stream().distinct().collect(Collectors.toList());
                                }
                        }


                        if (userBonusNew.getPercent() != null)
                        {
                            if (result.get(0).getPercent() == null )
                                result.get(0).setPercent(userBonusNew.getPercent());
                            else
                                result.get(0).getPercent().add(userBonusNew.getPercent().get(0));
                        }


                        if (userBonusNew.getSumUser() != null){
                            if (result.get(0).getSumUser() == null)
                                result.get(0).setSumUser(userBonusNew.getSumUser());
                            else result.get(0).getSumUser().add(userBonusNew.getSumUser().get(0));
                        }


                        result.get(0).getYear().add(userBonusNew.getYear().get(0));

                        if (!tmpCanditate.equals("")) {
                            if (result.get(0).getExtraBonusAct() == null)
                                result.get(0).setExtraBonusAct(userBonusNew.getExtraBonusAct());
                            else

                                result.get(0).getExtraBonusAct().add(userBonusNew.getExtraBonusAct().get(0));

                        }

                    } else {
                        userBonusNewList.add(userBonusNew);
                    }

                }

            return userBonusNewList;

        }

    }


}

