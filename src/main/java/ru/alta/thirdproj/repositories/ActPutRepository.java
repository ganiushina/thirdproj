package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.ActBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ActPutRepository {

    private final Sql2o sql2o;

    public ActPutRepository(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    private static final String SELECT_ACT_PAYMENT_QUERY = "SELECT * FROM fn_GetPaymentDone (:date1, :date2)";

    public List<Act> getPutAct(LocalDate date1, LocalDate date2)  {

        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_ACT_PAYMENT_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            List<Act> actList = new ArrayList<>();
            DateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");


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
                        act.setBonus((double) entry.getValue());
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
                        if (entry.getValue() != null)
                            act.setDate(formatter1.format((Date) entry.getValue()));
                        else act.setDate("");
                        //                        act.setDateAct(formatter1.format((Date) entry.getValue()));
//                        if (entry.getValue() != null)
//                            try {
//                                act.setDate(formatter1.parse(formatter1.format((Date) entry.getValue())));
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
//                        else act.setDate((Date) entry.getValue());

                    }

                    if (entry.getKey().equals("payment_date")) {
//                        if (entry.getValue() != null)
//                        try {
//                            act.setDatePayment(formatter1.parse(formatter1.format((Date) entry.getValue())));
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        else act.setDatePayment((Date) entry.getValue());
                        if (entry.getValue() != null)
                            act.setDatePayment(formatter1.format((Date) entry.getValue()));
                        else act.setDatePayment("");
                    }

                }
                actList.add(act);

            }
            return actList;
    }

}
}
