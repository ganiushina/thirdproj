package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import ru.alta.thirdproj.entites.UserSalary;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

@Component
public class UserSalaryRepImplRep implements iUserSalaryRep {

    private final Sql2o sql2o;

    public UserSalaryRepImplRep(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    private static final String SELECT_SALARY_PAYMENT_QUERY = "select * from fn_salary_for_BDM(:date1,:date2)\n";

    @Override
    public List<UserSalary> getAllUserSalary(LocalDate date1, LocalDate date2) {
        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_SALARY_PAYMENT_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            List<UserSalary> userSalaryList = new ArrayList<>();

            Locale ru = new Locale("ru", "RU");
            Currency rub = Currency.getInstance(ru);
            NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);

            for (Map<String, Object> n : list) {
                UserSalary userSalary = new UserSalary();
                for (var entry : n.entrySet()) {

                    if (entry.getKey().equals("man_id")) {
                        userSalary.setUserId((Integer) entry.getValue());
                    }
                    if (entry.getKey().equals("man_fio")) {
                        userSalary.setFio((String) entry.getValue());
                    }
                    if (entry.getKey().equals("man_salary_all")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        userSalary.setUserSalary(d);
                    }
                    if (entry.getKey().equals("man_bonus")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        userSalary.setUserBonus(d);
                    }
                    if (entry.getKey().equals("man_money_real")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        userSalary.setUserMoneyReal(d);
                    }
                    if (entry.getKey().equals("salary_month")) {
                        userSalary.setSalaryMonth((Integer) entry.getValue());
                    }
                    if (entry.getKey().equals("salary_year")) {
                        userSalary.setSalaryYear((Integer) entry.getValue());
                    }
                    if (entry.getKey().equals("salary_quarter")) {
                        userSalary.setSalaryQuarter((Integer) entry.getValue());
                    }
                }

            }
            return userSalaryList;

        }

    }

    @Override
    public UserSalary findByFio(String fio) {
        return null;
    }
}
