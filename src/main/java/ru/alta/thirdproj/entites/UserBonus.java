package ru.alta.thirdproj.entites;

import lombok.Data;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Data
public class UserBonus {


    private long id;

    private long userId;

    private String fio;

    private String position;

    private String department;

    private float moneyAll;

    private float moneyByCandidate;

    private float percent;

    private float sumTotal;

    private float sumUser;

    private String companyName;

    private String candidateName;

    private int month;

    private int year;


    public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

    static {
        COLUMN_MAPPINGS.put("man_fio", "fio");
        COLUMN_MAPPINGS.put("man_id", "userId");
        COLUMN_MAPPINGS.put("pos_name", "position");
        COLUMN_MAPPINGS.put("dep_name", "department");
        COLUMN_MAPPINGS.put("money_itog", "moneyAll"); // всего бонус
        COLUMN_MAPPINGS.put("money_by_candidate", "moneyByCandidate"); //бонус за кандидата
        COLUMN_MAPPINGS.put("persent", "percent");
        COLUMN_MAPPINGS.put("summ_total", "sumTotal"); //всего заработано за период
        COLUMN_MAPPINGS.put("summ_user", "sumUser"); // заработано за кандидата
        COLUMN_MAPPINGS.put("company_name", "companyName");
        COLUMN_MAPPINGS.put("candidate", "candidateName");
        COLUMN_MAPPINGS.put("mon", "month");
        COLUMN_MAPPINGS.put("ya", "year");
    }

}
