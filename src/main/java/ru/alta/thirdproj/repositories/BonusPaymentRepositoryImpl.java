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
            DateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");

            Locale ru = new Locale("ru", "RU");
            Currency rub = Currency.getInstance(ru);
            NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);


            for (Map<String, Object> n : list) {

                EmployerNew employer = new EmployerNew();
                Act act = new Act();
                List<Act> actList = new ArrayList<>();
                List<Integer> percent = new ArrayList<>();
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
                                if (!(entry.getValue()).equals(""))
                                    act.setCandidate((String) entry.getValue());

                            }
                            if (entry.getKey().equals("company_name")) {
                                if (!(entry.getValue()).equals(""))
                                act.setCompanies((String) entry.getValue());
                            }
                            if (entry.getKey().equals("persent")) {
                                if ((Integer) entry.getValue() != 0) {
                                    percent.add((Integer) entry.getValue());
                                    employer.setPercent(percent);
                                }
                            }

                            if (entry.getKey().equals("bonus")) {
                                BigDecimal bd = (BigDecimal) entry.getValue();
                                double d = bd.doubleValue();
                                if (d != 0.0) {
                                    act.setBonus(d);
                                    act.setBonusRUB(currencyInstance.format(d));
                                }
                            }

                            if (entry.getKey().equals("all_bonus")) {
                                BigDecimal bd = (BigDecimal) entry.getValue();
                                double d = bd.doubleValue();
                                if (d != 0.0) {
                                    employer.setAllBonus(d);
                                    employer.setAllBonusRUB(currencyInstance.format(d));
                                }
                            }

                            if (entry.getKey().equals("date_for_pay")) {
                                if (entry.getValue() != null)
                                    act.setDateForPay(formatter1.format((Date) entry.getValue()));
                                else act.setDateForPay("");

                            }

                            if (entry.getKey().equals("payment_date")) {
                                if (entry.getValue() != null)
                                    act.setDatePayment(formatter1.format((Date) entry.getValue()));
                                else act.setDatePayment("");//
                            }

                            if (entry.getKey().equals("payment_real_date")) {
                                if (entry.getValue() != null)
                                    act.setPaymentRealDate(formatter1.format((Date) entry.getValue()));
                            }

                            if (entry.getKey().equals("act_num")) {
                                if (!(entry.getValue()).equals(""))
                                act.setNum((String) entry.getValue());
                            }

                            if (entry.getKey().equals("act_id")) {
                                if ((Integer) entry.getValue() != 0)
                                act.setId((Integer) entry.getValue());
                            }
                            if (entry.getKey().equals("paid")) {
                                if (entry.getValue() != null)
                                act.setPaid ((Boolean) entry.getValue());
                            }

                            if (entry.getKey().equals("date_act")) {
                                if (entry.getValue() != null)
                                    act.setDate(formatter1.format((Date) entry.getValue()));
                            }

                            if (entry.getKey().equals("project_id")) {
                                if ((Integer) entry.getValue() != 0)
                                act.setProjects((Integer) entry.getValue());
                            }

                            if (entry.getKey().equals("emploeduser")) {
                                if (entry.getValue() != null)
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
                    if (employer.getPercent() != null) {
                        if (result.get(0).getPercent() == null) {
                            result.get(0).setPercent(employer.getPercent());
                        } else result.get(0).getPercent().add(employer.getPercent().get(0));
                    }
                }
                else {

                    employerList.add(employer);
                }

            }

            return employerList;
        }


    }


}
