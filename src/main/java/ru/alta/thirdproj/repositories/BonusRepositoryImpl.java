package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import ru.alta.thirdproj.entites.UserBonus;
import ru.alta.thirdproj.entites.UserBonusTest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BonusRepositoryImpl {

    private final Sql2o sql2o;

    private List<UserBonus> userBonusesList;
    private List<UserBonusTest> userBonusTest;


    private static final String SELECT_USER_QUERY = "SELECT * FROM fn_User_Bonus_by_Details (:date1, :date2, :user_id, :department_id)";

    public BonusRepositoryImpl(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public List<UserBonus> getUserBonuses(LocalDate date1, LocalDate date12, Integer userId, Integer departmentId) {
        try (Connection connection = sql2o.open()) {
            userBonusesList = connection.createQuery(SELECT_USER_QUERY, false)


        //    Query query = connection.createQuery(SELECT_USER_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date12)
                    .addParameter("user_id", userId)
                    .addParameter("department_id", departmentId)

//            Table table = query.executeAndFetchTable();
//            List<Map<String, Object>> list = table.asList();
//
//
//            for (Map<String, Object> n : list) {
//
//                HashMap<String,Object> map = new HashMap<>();
//
//                int i = 0;
//                for (var entry : n.entrySet()) {
//
//                    if (n.keySet().equals(userBonusTest.get(i).getEmployer().getManId())){
//
//                    }
//
//                    System.out.println(entry.getKey() + "/" + entry.getValue());
//                }
//
////                HashMap<String,Object> map = new HashMap<>();
////
////
////
////                for (int j = 0; j < n.size() ; j++) {
////                    n.get(j).toString();
//////                    if (n.get(j).getFio().equals(n.getFio())){
//////                        moneyByCandidate.add(userBonuses.get(j).getMoneyByCandidate()) ;
//////                        userSumList.add(userBonuses.get(j).getSumUser());
//////                        candidateName.add(userBonuses.get(j).getCandidateName());
//////                        companyName.add(userBonuses.get(j).getCompanyName());
//////                    }
////                }
//
//
//
//
////                map.put("fio", n.getFio());
////                map.put("position", n.getPosition());
////                map.put("department", n.getDepartment());
////
////                map.put("moneyAll", n.getMoneyAll());
////
////                map.put("sumTotal", n.getSumTotal());
////                map.put("percent", n.getPercent());
////                map.put("month", n.getMonth());
////                map.put("year", n.getYear());
//
//            }

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

            for (Map<String, Object> n : list) {
                HashMap<String,Object> map = new HashMap<>();
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



       //     query.addColumnMapping("man_id", list.get(0).get(0).)
     //       assertEquals("tutorials", list.get(0).get("name"));

            return userBonusTest;
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
