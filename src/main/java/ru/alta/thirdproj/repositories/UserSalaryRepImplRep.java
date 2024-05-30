package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import ru.alta.thirdproj.entites.MarginBonusBDM;
import ru.alta.thirdproj.entites.UserSalary;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

@Component
public class UserSalaryRepImplRep  {

    private final Sql2o sql2o;

    public UserSalaryRepImplRep(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    private static final String SELECT_SALARY_PAYMENT_QUERY = "select * from fn_salary_for_all_user(:date1,:date2)\n";
    private static final String SELECT_MARGIN_QUERY =  "select * from fn_margin_bonus (:date1,:date2)";


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
                    if (entry.getKey().equals("bonusKPI")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            userSalary.setUserBonusKPI(currencyInstance.format(d));
                        }
                    }
                    if (entry.getKey().equals("bonus")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            userSalary.setUserBonus(currencyInstance.format(d));
                        }
                    }
                    if (entry.getKey().equals("manzp")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            userSalary.setUserSalary(currencyInstance.format(d));
                        }
                    }
                    if (entry.getKey().equals("allsumm")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            userSalary.setUserSalaryAll(currencyInstance.format(d));
                        }
                    }
                    if (entry.getKey().equals("salary_month")) {
                        userSalary.setSalaryMonth((Integer) entry.getValue());
                    }
                    if (entry.getKey().equals("salary_year")) {
                        userSalary.setSalaryYear((Integer) entry.getValue());
                    }
                }
                userSalaryList.add(userSalary);
            }
            return userSalaryList;

        }

    }
    public List<MarginBonusBDM> getMarginBonus(LocalDate date1, LocalDate date2) {
        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_MARGIN_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            List<MarginBonusBDM> marginBonusList = new ArrayList<>();

            Locale ru = new Locale("ru", "RU");
            Currency rub = Currency.getInstance(ru);
            NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);

            for (Map<String, Object> n : list) {
                MarginBonusBDM marginBonus = new MarginBonusBDM();
                for (var entry : n.entrySet()) {

                    if (entry.getKey().equals("department_name")) {
                        marginBonus.setDepartmentName((String) entry.getValue());
                    }
                    if (entry.getKey().equals("division_name")) {
                        marginBonus.setDivisionName((String) entry.getValue());
                    }
                    if (entry.getKey().equals("money_itog")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginBonus.setAllMoney(d);
                        }
                    }
                    if (entry.getKey().equals("margin_department")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginBonus.setMarginDepartment(d);
                        }
                    }
                    if (entry.getKey().equals("margin_division")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginBonus.setMarginDivision(d);
                        }
                    }
                    if (entry.getKey().equals("margin_bonus")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginBonus.setMarginBonusBDM(d);
                        }
                    }
                    if (entry.getKey().equals("quat")) {
                        marginBonus.setMarginQuarter((Integer) entry.getValue());
                    }
                    if (entry.getKey().equals("ya")) {
                        marginBonus.setMarginYear((Integer) entry.getValue());
                    }
                }
                marginBonusList.add(marginBonus);
            }
            return marginBonusList;

        }

    }
    public UserSalary findByFio(String fio) {
        return null;
    }
}
