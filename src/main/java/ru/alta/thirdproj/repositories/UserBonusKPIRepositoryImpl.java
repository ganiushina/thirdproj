package ru.alta.thirdproj.repositories;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import ru.alta.thirdproj.entites.UserBonusKPI;
import ru.alta.thirdproj.entites.UserBonusNew;


import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserBonusKPIRepositoryImpl {

    private final Sql2o sql2o;

    private static final String SELECT_BONUS_KPI_QUERY = "SELECT * FROM userBonusKPI(:date1, :date2)";

    public UserBonusKPIRepositoryImpl(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    public List<UserBonusKPI> userBonusKPIList(LocalDate date1, LocalDate date2){

        try (Connection connection = sql2o.open()) {
            Query query =connection.createQuery(SELECT_BONUS_KPI_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            List<UserBonusKPI> userBonusKPIList = new ArrayList<>();

            for (Map<String, Object> n : list) {

                UserBonusKPI userBonusKPI = new UserBonusKPI();

                List<Double> bonus = new ArrayList<>();
                List<Double> bonusBest = new ArrayList<>();
                List<Double> bonusAll = new ArrayList<>();
                List<String> month = new ArrayList<>();


                boolean isNotNew = false;
                String manFIO = null;

                for (var entry : n.entrySet()) {

                    if (entry.getKey().equals("user_id")) {
                        userBonusKPI.setUserId((Integer) entry.getValue());
                    }

                    if (entry.getKey().equals("man_fio")) {
                        for (int j = 0; j < userBonusKPIList.size(); j++) {
                            if (entry.getKey().equals("man_fio")) {
                                if (userBonusKPIList.get(j).getFio().equals(entry.getValue())) {
                                    manFIO = (String) entry.getValue();//
                                    isNotNew = true;
                                }
                            }
                        }

                        userBonusKPI.setFio((String) entry.getValue());
                    }


                    if (entry.getKey().equals("position")) {
                        userBonusKPI.setPosition((String) entry.getValue());
                    }

                    if (entry.getKey().equals("bonus")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        bonus.add(d);
                    }

                    if (entry.getKey().equals("best_bonus")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        bonusBest.add(d);
                    }

                    if (entry.getKey().equals("all_bonus")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        bonusAll.add(d);

                    }

                    if (entry.getKey().equals("mon")) {
                        month.add((String) entry.getValue());
                    }
                }

                userBonusKPI.setBonus(bonus);
                userBonusKPI.setBonusBest(bonusBest);
                userBonusKPI.setBonusAll(bonusAll);
                userBonusKPI.setMonth(month);

                if (isNotNew) {
                    String finalManFIO = manFIO;
                    List<UserBonusKPI> result = userBonusKPIList.stream()
                            .filter(a -> Objects.equals(a.getFio(), finalManFIO))
                            .collect(Collectors.toList());

                    result.get(0).getBonus().add(userBonusKPI.getBonus().get(0));
                    result.get(0).getBonusBest().add(userBonusKPI.getBonusBest().get(0));
                    result.get(0).getBonusAll().add(userBonusKPI.getBonusAll().get(0));
                    result.get(0).getMonth().add(userBonusKPI.getMonth().get(0));


                } else
                    {
                        userBonusKPIList.add(userBonusKPI);
                    }
            }

            return userBonusKPIList;
        }
    }
}
