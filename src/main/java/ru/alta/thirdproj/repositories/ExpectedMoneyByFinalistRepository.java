package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import ru.alta.thirdproj.entites.MoneyByFinalist;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Component
public class ExpectedMoneyByFinalistRepository {

    private final Sql2o sql2o;

    public ExpectedMoneyByFinalistRepository(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    private static final String SELECT_FINALIST_MONEY_QUERY = "select p.project_id, c.company_name,  m.man_fio, m.man_id, p.project_fee , p.project_name, r.recruiting_to_work, p.project_delay_pay, \n" +
            " (SELECT [dbo].[date_notholiday] (r.recruiting_to_work, p.project_delay_pay)) date_for_client_pay \n" +
            "from recruiting r\n" +
            "join project p on p.project_id = r.recruiting_project_id\n" +
            "join man m on m.man_id = r.recruiting_man_id\n" +
            "join company c on c.company_id = p.project_company_id\n" +
            "where r.recruiting_type_id = 8043271\n" +
            "and p.project_in_date >= CONVERT(datetime, '20220101')";


    public LocalDate convertToLocalDateViaSqlDate(Date dateToConvert) {
        return new java.sql.Date(dateToConvert.getTime()).toLocalDate();
    }


    public List<MoneyByFinalist> getMoneyByFinalist()  {

        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_FINALIST_MONEY_QUERY, false);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            List<MoneyByFinalist> finalistList = new ArrayList<>();
            DateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");

            Locale ru = new Locale("ru", "RU");
            NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);


            for (Map<String, Object> n : list) {

                MoneyByFinalist moneyByFinalist = new MoneyByFinalist();

                for (var entry : n.entrySet()) {
                    if (entry.getKey().equals("project_id")) {
                        Long id = (Long) entry.getValue();
                        if ( id != 0) {
                            moneyByFinalist.setProjectId(id.intValue());
                        }
                    } else
                    if (entry.getKey().equals("man_id")) {
                        Long id = (Long) entry.getValue();
                        moneyByFinalist.setManId(id.intValue());
                    }

                    if (entry.getKey().equals("man_fio")) {
                        moneyByFinalist.setManFio((String) entry.getValue());
                    } else

                    if (entry.getKey().equals("company_name")) {
                        moneyByFinalist.setCompanyName((String) entry.getValue());
                    } else
                    if (entry.getKey().equals("project_fee")) {
                        String val = "";
                        if (String.valueOf(entry.getValue()).indexOf(",") > 0){
                            val = String.valueOf(entry.getValue()).replace(",", ".");
                        }
                        else val = String.valueOf(entry.getValue());
                        BigDecimal bd = new BigDecimal(val);
                        double d = bd.doubleValue();
                        moneyByFinalist.setProjectFee(bd);
                        moneyByFinalist.setProjectFeeRUB(currencyInstance.format(d));
                    } else

                    if (entry.getKey().equals("project_name")) {
                        moneyByFinalist.setProjectName((String) entry.getValue());
                    } else

                    if (entry.getKey().equals("recruiting_to_work")) {
                        if (entry.getValue() != null) {
                            moneyByFinalist.setRecruitingToWorkString(formatter1.format((Date) entry.getValue()));
                            moneyByFinalist.setRecruitingToWork(convertToLocalDateViaSqlDate((Date) entry.getValue()));
                        }
                        else moneyByFinalist.setRecruitingToWorkString("");
                    } else

                    if (entry.getKey().equals("project_delay_pay")) {
                        if (entry.getValue() != null) {
                            moneyByFinalist.setProjectDelayPay((Integer) entry.getValue());
                        }
                    } else

                    if (entry.getKey().equals("date_for_client_pay")) {
                        if (entry.getValue() != null) {
                            moneyByFinalist.setRecruitingMoneyToWorkString(formatter1.format((Date) entry.getValue()));
                            moneyByFinalist.setRecruitingMoneyToWork(convertToLocalDateViaSqlDate((Date) entry.getValue()));
                        }
                        else moneyByFinalist.setRecruitingMoneyToWorkString("");
                    }

                }
                finalistList.add(moneyByFinalist);

            }
            return finalistList;
        }

    }
}
