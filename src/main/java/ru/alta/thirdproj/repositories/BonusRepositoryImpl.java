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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BonusRepositoryImpl {

    private final Sql2o sql2o;

    private List<UserBonus> userBonusesList;
    private List<UserBonusTest> userBonusTestList = new ArrayList<>();
    private UserBonusTest userBonusTest = new UserBonusTest();
    private Employer employer = new Employer();
    private UserBonusMoney userBonusMoney = new UserBonusMoney();
    private UserSum userSum = new UserSum();

    private List<String> candidateList = new ArrayList<>();



    private static final String SELECT_USER_QUERY = "SELECT * FROM fn_User_Bonus_by_Details (:date1, :date2, :user_id, :department_id)";

    public BonusRepositoryImpl(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public List<UserBonus> getUserBonuses(LocalDate date1, LocalDate date12, Integer userId, Integer departmentId) {
    //    userBonusTestList(date1, date12, userId, departmentId);
        try (Connection connection = sql2o.open()) {
            userBonusesList = connection.createQuery(SELECT_USER_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date12)
                    .addParameter("user_id", userId)
                    .addParameter("department_id", departmentId)
                    .setColumnMappings(UserBonus.COLUMN_MAPPINGS)
                    .executeAndFetch(UserBonus.class);
            return userBonusesList;
        }
    }

    public List<UserBonusTest> userBonusTestList(LocalDate date1, LocalDate date12, Integer userId, Integer departmentId){

        try (Connection connection = sql2o.open()) {
            Query query =connection.createQuery(SELECT_USER_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date12)
                    .addParameter("user_id", userId)
                    .addParameter("department_id", departmentId);
            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            int i = 0;
            for (Map<String, Object> n : list) {
                HashMap<String,Object> map = new HashMap<>();

                if (i == 0) {
//                        Integer manId = (Integer) entry.getValue();
//                        Long manIdLong = Long.valueOf(manId);
//
//                        if (entry.getKey().equals("man_id")) {
//                            if (userBonusTest.getEmployer().getManId() == manIdLong) {
//                                if (entry.getKey().equals("candidate")) {
//                                    candidateList.add((String) entry.getValue());
//                                    userBonusMoney.setCandidate(candidateList);
//                                }
//                                if (entry.getKey().equals("candidate")) {
//                                    candidateList.add((String) entry.getValue());
//                                    userBonusMoney.setCandidate(candidateList);
//                                }
//
//                            }
//                        }

                    for (var entry : n.entrySet()) {



                     // if (userBonusTest.getEmployer().getManId() == )

                        if (entry.getKey().equals("man_id")) {
                            Integer in = (Integer) entry.getValue();
                            Long two = Long.valueOf(in);
                            employer.setManId(two);
                        }
                        if (entry.getKey().equals("man_fio"))
                            employer.setManFIO((String) entry.getValue());
                        if (entry.getKey().equals("dep_name"))
                            employer.setUserDepartment((String) entry.getValue());
                        if (entry.getKey().equals("pos_name"))
                            employer.setUserPosition((String) entry.getValue());

                        if (entry.getKey().equals("candidate")) {
                            candidateList.add((String) entry.getValue());
                            userBonusMoney.setCandidate(candidateList);
                        }
                        if (entry.getKey().equals("company_name")) {
                            List<String> str = new ArrayList<>();
                            str.add((String) entry.getValue());
                            userBonusMoney.setCompany(str);
                        }
                        if (entry.getKey().equals("persent")) {
//                            BigDecimal bd = (BigDecimal) entry.getValue();
//                            double d = bd.doubleValue();
                            userBonusMoney.setPercent((Double) entry.getValue());
                        }
                        if (entry.getKey().equals("money_itog")) {
                            BigDecimal bd = (BigDecimal) entry.getValue();
                            double d = bd.doubleValue();
                            userBonusMoney.setUserBonus(d);
                        }
                        if (entry.getKey().equals("money_by_candidate")) {
                            List<Double> userMoneyList = new ArrayList<>();
                            BigDecimal bd = (BigDecimal) entry.getValue();
                            double d = bd.doubleValue();
                            userMoneyList.add(d);
                            userBonusMoney.setUserBonusByCandidate(userMoneyList);
                        }

                        if (entry.getKey().equals("summ_total"))
                            userSum.setUserSum((Double) entry.getValue());
                        if (entry.getKey().equals("summ_user")) {
                            List<Double> userSumList = new ArrayList<>();
                            BigDecimal bd = (BigDecimal) entry.getValue();
                            double d = bd.doubleValue();
                            userSumList.add(d);
                            userSum.setUserSumByCandidate(userSumList);
                        }

                        if (entry.getKey().equals("mon"))
                            userBonusTest.setMonth((Integer) entry.getValue());
                        if (entry.getKey().equals("ya"))
                            userBonusTest.setYear((Integer) entry.getValue());



//                        if (employer.getManId() != null)
//                        userBonusTest.setEmployer(employer);
//                        if (userBonusMoney.getCandidate() != null)
//                        userBonusTest.setUserBonusMoney(userBonusMoney);
//                        if (userSum.getUserSum() != null)
//                        userBonusTest.setUserSum(userSum);
//





                   //     userBonusMoney.setUserBonusByCandidate(entry.getValue());






                      //  map.put(entry.getKey(), entry.getValue());

//                        if (n.keySet().equals(userBonusTest.get(i).getEmployer().getManId())) {

//                        for (int j = 0; j < map.size(); j++) {
//                            if (map.get(j).equals("man_fio"))
//                                System.out.println(1);
//
//                        }
                   //      map.get(entry.getKey()).equals()
//                        for (var mentry : map.entrySet()) {
//                            if (mentry.getKey().equals("man_fio"))
//                                if (mentry.getKey().equals(entry.getKey()))
//
//
//                                System.out.println(1);
//                        }



                     //   }

                        System.out.println(entry.getKey() + "/" + entry.getValue());
                    }

                        userBonusTest.setEmployer(employer);
                        userBonusTest.setUserBonusMoney(userBonusMoney);
                        userBonusTest.setUserSum(userSum);


                    i++;
                }
//                map.put("fio", n.getFio());
//                map.put("position", n.getPosition());
//                map.put("department", n.getDepartment());
//
//                map.put("moneyAll", n.getMoneyAll());
//
//                map.put("sumTotal", n.getSumTotal());
//                map.put("percent", n.getPercent());
//                map.put("month", n.getMonth());
//                map.put("year", n.getYear());

            }
            userBonusTestList.add(userBonusTest);



       //     query.addColumnMapping("man_id", list.get(0).get(0).)
     //       assertEquals("tutorials", list.get(0).get("name"));

            return userBonusTestList;
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



//    @Override
//    public Optional<UserBonus> findOne(Specification<UserBonus> spec) {
//        return Optional.empty();
//    }
//
//    @Override
//    public List<UserBonus> findAll(Specification<UserBonus> spec) {
//        return null;
//    }
//
//    @Override
//    public Page<UserBonus> findAll(Specification<UserBonus> spec, Pageable pageable) {
//        return null;
//    }
//
//    @Override
//    public List<UserBonus> findAll(Specification<UserBonus> spec, Sort sort) {
//        return null;
//    }
//
//    @Override
//    public long count(Specification<UserBonus> spec) {
//        return 0;
//    }
}
