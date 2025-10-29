package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.ActBuilder;
import ru.alta.thirdproj.entites.ActByCompanyByDepartment;
import ru.alta.thirdproj.entites.Employer;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Component
public class ActPutRepository {

    private final Sql2o sql2o;

    public ActPutRepository(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    private static final String SELECT_ACT_PAYMENT_QUERY = "SELECT * FROM fn_GetPaymentDone (:date1, :date2)";

    private static final String SELECT_ACT_PAYMENT_BY_ACT_QUERY = "select pb.act_id, pb.responsible_user_name  from project_buh pb \n" +
            "\n" +
            "where pb.act_id = :act_id";

    private static final String SELECT_ACT_NO_PAYMENT_QUERY = "SELECT ab.id, ab.date_act, left(ab.act_num, 11) act_num, ab.company_name, ab.total, ab.total_no_nds, ab.project_name, \n" +
            "\tab.candidate, (SELECT [dbo].[date_notholiday] (ab.date_act, p.project_delay_pay)) date_for_client_pay \n" +
            "\tFROM dbo.act_buh ab\n" +
            "\tLEFT JOIN dbo.project p ON p.project_id = ab.project_id\n" +
            "\tWHERE ab.id NOT IN (SELECT pb.act_id FROM dbo.payment_buh pb) \n" +
            "\tAND convert(date, ab.date_act) >= convert(date, dateadd(yy, -1, :date1))";

    private static final String SELECT_ACT_DETAILS_BY_COMPANY_BY_DEPARTMENT = "SELECT ab.id, ab.date_act, left(ab.act_num, 11) act_num, ab.company_name, ab.total_no_nds, ab.project_name, \n" +
            "\tab.candidate, ab.organization, pb.depatment_id, d.dep_name, pb.percent_responsible_user_by_candidate_percent*100 dep_percent,\n" +
            "\tsum(pb.summ_responsible_user) over (partition by pb.depatment_id) sum_dep,\n" +
            "\tsum(ab.total_no_nds) over (partition by ab.company_name) sum_comp\n" +
            "\tFROM dbo.act_buh ab\n" +
            "\tjoin project_buh pb on pb.act_id = ab.id\n" +
            "\tjoin depatment d on d.id = pb.depatment_id\t\n" +
            "\tLEFT JOIN dbo.project p ON p.project_id = ab.project_id\n" +
            "\tWHERE convert(date, ab.date_act) BETWEEN :date1 AND :date2";

    public List<Act> getPutAct(LocalDate date1, LocalDate date2)  {

        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_ACT_PAYMENT_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            List<Act> actList = new ArrayList<>();
            DateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");

            Locale ru = new Locale("ru", "RU");
            Currency rub = Currency.getInstance(ru);
            NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);

            for (Map<String, Object> n : list) {

                Act act = ActBuilder.anAct().build();

                for (var entry : n.entrySet()) {
                    if (entry.getKey().equals("act_id")) {
                        act.setId((Integer) entry.getValue());
                    }

                    if (entry.getKey().equals("act_num")) {
                        act.setNum((String) entry.getValue());
                    }
                    if (entry.getKey().equals("total_no_nds")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            act.setBonus(d);
                            act.setBonusRUB(currencyInstance.format(d));
                        }
                    }

                    if (entry.getKey().equals("company_name")) {
                        act.setCompanies((String) entry.getValue());
                    }

                    if (entry.getKey().equals("candidate")) {
                        act.setCandidate((String) entry.getValue());
                    }

                    if (entry.getKey().equals("paied")) {
                        boolean b = ((Integer) entry.getValue() == 1);
                        act.setPaid(b);
                    }

                    if (entry.getKey().equals("project_name")) {
                        act.setProjectName((String) entry.getValue());
                    }

                    if (entry.getKey().equals("date_act")) {
                        if (entry.getValue() != null) {
                            act.setDate(formatter1.format((Date) entry.getValue()));
                            act.setDateAct(convertToLocalDateViaSqlDate((Date) entry.getValue()));
                        }
                        else act.setDate("");
                    }

                    if (entry.getKey().equals("date_for_client_pay")) {
                        if (entry.getValue() != null)
                            act.setDateClientPay(formatter1.format((Date) entry.getValue()));
                        else act.setDateClientPay("");

                    }

                    if (entry.getKey().equals("payment_date")) {
                        if (entry.getValue() != null) {
                            act.setDatePayment(formatter1.format((Date) entry.getValue()));
                            act.setPaymentDate(convertToLocalDateViaSqlDate((Date) entry.getValue()));
                        }
                        else act.setDatePayment("");
                    }
                    if (entry.getKey().equals("organization")) {
                        act.setOrganization((String) entry.getValue());
                    }
                }
                actList.add(act);

            }
            return actList;
    }

}
    public LocalDate convertToLocalDateViaSqlDate(Date dateToConvert) {
        return new java.sql.Date(dateToConvert.getTime()).toLocalDate();
    }

    public List<ActByCompanyByDepartment> getActByDepartmentByCompany(LocalDate dateStart, LocalDate dateFinish){
        try (Connection connection = sql2o.open()) {
            return   connection.createQuery(SELECT_ACT_DETAILS_BY_COMPANY_BY_DEPARTMENT, false)
                    .addParameter("date1", dateStart)
                    .addParameter("date2", dateFinish)
                    .executeAndFetch((ResultSetHandler<ActByCompanyByDepartment>) rs -> {
                        ActByCompanyByDepartment act = new ActByCompanyByDepartment();
                        act.setActId(rs.getInt("id"));
                        act.setNum(rs.getString("act_num"));
                        act.setDateAct(rs.getDate("date_act").toLocalDate());
                        act.setCompany(rs.getString("company_name"));
                        act.setProjectName(rs.getString("project_name"));
                        act.setCandidate(rs.getString("candidate"));
                        act.setOrganization(rs.getString("organization"));
                        act.setSumActNoNDS(rs.getDouble("total_no_nds"));
                        act.setDepartmentId(rs.getInt("depatment_id"));
                        act.setDepartmentName(rs.getString("dep_name"));
                        act.setPercentByDepartment(rs.getDouble("dep_percent"));
                        act.setSumByDepartment(rs.getDouble("sum_dep"));
                        act.setSumByCompany(rs.getDouble("sum_comp"));
                        return act;
                    });
        }
    }


    public List<Act> getNoPaymentAct(LocalDate date1)  {

        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_ACT_NO_PAYMENT_QUERY, false)
                    .addParameter("date1", date1);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            List<Act> actList = new ArrayList<>();
            DateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");

            Locale ru = new Locale("ru", "RU");
            NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);


            for (Map<String, Object> n : list) {

                Act act = ActBuilder.anAct().build();

                for (var entry : n.entrySet()) {
                    if (entry.getKey().equals("id")) {
                        act.setId((Integer) entry.getValue());
                    }

                    if (entry.getKey().equals("act_num")) {
                        act.setNum((String) entry.getValue());
                    }
                    if (entry.getKey().equals("total_no_nds")) {
                        act.setBonus((double) entry.getValue());
                        act.setBonusRUB(currencyInstance.format((double) entry.getValue()));
                    }

                    if (entry.getKey().equals("company_name")) {
                        act.setCompanies((String) entry.getValue());
                    }

                    if (entry.getKey().equals("candidate")) {
                        act.setCandidate((String) entry.getValue());
                    }

                    if (entry.getKey().equals("project_name")) {
                        act.setProjectName((String) entry.getValue());
                    }

                    if (entry.getKey().equals("date_act")) {
                        if (entry.getValue() != null) {
                            act.setDate(formatter1.format((Date) entry.getValue()));
                            act.setDateAct(convertToLocalDateViaSqlDate((Date) entry.getValue()));
                        }
                        else act.setDate("");
                    }
                    if (entry.getKey().equals("date_for_client_pay")) {
                        if (entry.getValue() != null)
                            act.setDateClientPay(formatter1.format((Date) entry.getValue()));
                        else act.setDateClientPay("");
                    }

                }
                actList.add(act);

            }
            return actList;
        }

    }

}
