package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class PaymentSuccess {

    private int userId;
    private int employerId;
    private Double paymentSum;
    private Double paymentRealSum;
    private int actId;
    private Date paymentDate;
    private String candidate;
    private int projectId;

    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("userId", "userId");
        COLUMN_MAPPINGS.put("employer_id", "employerId");
        COLUMN_MAPPINGS.put("payment_summ", "paymentSum");
        COLUMN_MAPPINGS.put("payment_date", "paymentDate");
        COLUMN_MAPPINGS.put("candidate", "candidate");
        COLUMN_MAPPINGS.put("act_id", "actId");
        COLUMN_MAPPINGS.put("project_id", "projectId");
        COLUMN_MAPPINGS.put("payment_real_summ", "paymentRealSum");

    }

}
