package ru.alta.thirdproj.repositories;

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
public class BonusPaymentRepositoryImpl {

    private final Sql2o sql2o;
    private List<UserPaymentBonus> userPaymentBonusesList;

    private static final String SELECT_BONUS_PAYMENT_QUERY = "SELECT * FROM fn_User_Bonus_Payment (:date1, :date2, :userId, :department_id)";

    private static final String SELECT_BONUS_PAYMENT_QUERY_NEW = "SELECT * FROM fn_User_Bonus_Payment (:date1, :date2, :userId, :department_id)";

    public BonusPaymentRepositoryImpl(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public List<UserPaymentBonus> getUserBonuses(LocalDate date1, LocalDate date12) {
        try (Connection connection = sql2o.open()) {
            userPaymentBonusesList = connection.createQuery(SELECT_BONUS_PAYMENT_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date12)
                    .addParameter("userId", "")
                    .addParameter("department_id", "")
                    .setColumnMappings(UserPaymentBonus.COLUMN_MAPPINGS)
                    .executeAndFetch(UserPaymentBonus.class);
            return userPaymentBonusesList;
        }
    }


    public List<EmployerNew> userBonusPaymentList(LocalDate date1, LocalDate date12){

        try (Connection connection = sql2o.open()) {
            Query query =connection.createQuery(SELECT_BONUS_PAYMENT_QUERY_NEW, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date12)
                    .addParameter("userId", "")
                    .addParameter("department_id", "");
            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            List<EmployerNew> employerList = new ArrayList<>();

            for (Map<String, Object> n : list) {

                EmployerNew employer = new EmployerNew();
                Act act = new Act();
                List<Act> actList = new ArrayList<>();
                boolean isNotNew = false;
                String manFIO = null;

                    for (var entry : n.entrySet()) {

                            if (entry.getKey().equals("man_id")) {
                                employer.setManId((Integer) entry.getValue());
                            }

                            if (entry.getKey().equals("man_fio")) {
                                for (int j = 0; j < employerList.size(); j++) {
                                    if (entry.getKey().equals("man_fio")) {
                                        if (employerList.get(j).getManFIO().equals(entry.getValue())) {
                                            manFIO = (String) entry.getValue();//
                                            isNotNew = true;
                                        }
                                    }
                                }

                                employer.setManFIO((String) entry.getValue());
                            }

                            if (entry.getKey().equals("dep_name"))
                                employer.setUserDepartment((String) entry.getValue());


                            if (entry.getKey().equals("candidate")) {
                                act.setCandidate((String) entry.getValue());

                            }
                            if (entry.getKey().equals("company_name")) {
                                act.setCompanies((String) entry.getValue());
                            }
                            if (entry.getKey().equals("persent")) {
                                employer.setPercent((int) entry.getValue());
                            }

                            if (entry.getKey().equals("bonus")) {
                                BigDecimal bd = (BigDecimal) entry.getValue();
                                double d = bd.doubleValue();
                                act.setBonus(d);
                            }

                            if (entry.getKey().equals("all_bonus")) {
                                BigDecimal bd = (BigDecimal) entry.getValue();
                                double d = bd.doubleValue();
                                employer.setAllBonus(d);

                            }

                            if (entry.getKey().equals("date_for_pay")) {
                                act.setDateForPay((Date) entry.getValue());
                            }

                            if (entry.getKey().equals("payment_date")) {
                                act.setDatePayment((Date) entry.getValue());
                            }

                            if (entry.getKey().equals("payment_real_date")) {
                                act.setPaymentRealDate((Date) entry.getValue());
                            }

                            if (entry.getKey().equals("act_num")) {
                                act.setNum((String) entry.getValue());
                            }

                            if (entry.getKey().equals("act_id")) {
                                act.setId((Integer) entry.getValue());
                            }
                            if (entry.getKey().equals("paid")) {
                                act.setPaid ((Boolean) entry.getValue());
                            }

                            if (entry.getKey().equals("date_act")) {
                                act.setDate((Date) entry.getValue());

                            }

                            if (entry.getKey().equals("project_id")) {
                                act.setProjects((Integer) entry.getValue());
                            }

                            if (entry.getKey().equals("emploeduser")) {
                                act.setEmployerPaid((String) entry.getValue());
                            }
                    }


                actList.add(act);
                employer.setActList(actList);

                if (isNotNew) {
                    String finalManFIO = manFIO;
                    List<EmployerNew> result = employerList.stream()
                            .filter(a -> Objects.equals(a.getManFIO(), finalManFIO))
                            .collect(Collectors.toList());

                    result.get(0).getActList().add(employer.getActList().get(0));

                }
                else {

                    employerList.add(employer);
                }

            }

            return employerList;
        }


    }


}
