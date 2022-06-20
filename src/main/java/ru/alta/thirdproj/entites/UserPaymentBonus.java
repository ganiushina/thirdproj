package ru.alta.thirdproj.entites;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

//@Builder
@NoArgsConstructor
@Data
public class UserPaymentBonus {

    private long id;

    private int userId;

    private String fio;

    private String department;

    private String actNum;

    private int actId;

    private String candidateName;

    private int projectId;

    private String companyName;

    private Double bonus;

    private Double percent;

    private Double allBonus;

    private Date dateAct;

    private Date dateForPay;

    private Date paymentDate;

    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("man_fio", "fio");
        COLUMN_MAPPINGS.put("man_id", "userId");
        COLUMN_MAPPINGS.put("dep_name", "department");
        COLUMN_MAPPINGS.put("act_num", "actNum");
        COLUMN_MAPPINGS.put("act_id", "actId");
        COLUMN_MAPPINGS.put("candidate", "candidateName");
        COLUMN_MAPPINGS.put("project_id", "projectId");
        COLUMN_MAPPINGS.put("company_name", "companyName");
        COLUMN_MAPPINGS.put("bonus", "bonus");
        COLUMN_MAPPINGS.put("persent", "percent");
        COLUMN_MAPPINGS.put("all_bonus", "allBonus");
        COLUMN_MAPPINGS.put("date_act", "dateAct");
        COLUMN_MAPPINGS.put("date_for_pay", "dateForPay");
        COLUMN_MAPPINGS.put("payment_date", "paymentDate");

    }
}
