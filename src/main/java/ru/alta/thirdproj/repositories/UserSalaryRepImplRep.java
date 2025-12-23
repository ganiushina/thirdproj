package ru.alta.thirdproj.repositories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.alta.thirdproj.entites.*;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.thymeleaf.util.NumberUtils.formatCurrency;

@Component
@Slf4j
public class UserSalaryRepImplRep  {

    private final Sql2o sql2o;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserSalaryRepImplRep(@Autowired Sql2o sql2o, @Autowired javax.sql.DataSource dataSource) {
        this.sql2o = sql2o;
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }


    private static final String SELECT_SALARY_PAYMENT_QUERY = "select * from fn_salary_for_all_user(:date1,:date2, :department_id)\n";
    private static final String SELECT_MARGIN_QUARTER_QUERY =  "select * from fn_margin_bonus_by_quarter (:date1,:date2)";
    private static final String SELECT_MARGIN_MONTH_QUERY =  "select * from fn_marginality_by_month (:date1,:date2)";
    private static final String SELECT_SALES_QUERY =  "select * from [fn_User_Sale] (:date1, :date2)";
    private static final String SELECT_SALARY_PAYMENT_INTERPRETER_QUERY = "select * from fn_salary_for_all_user_sverka(:date1,:date2) order by dep_name, salary_month, man_fio\n";
    private static final String SELECT_SALARY_PAYMENT_SUCCESS_QUERY =
            "select ps.user_id, m.man_fio, ps.dateFrom, ps.dateTo, ps.success from paymentPeriodSuccess ps\n" +
            "join man m on m.man_id = ps.user_id " +
                    "where convert(date, ps.dateFrom) = convert(date,:dateFrom) and convert(date,ps.dateTo) = convert(date,:dateTo)";
    private static final String SELECT_ACT_BY_USER_CHECK =
            "SELECT\n" +
                    "    ab.id AS act_id,\n" +
                    "    ab.date_act,\n" +
                    "    LEFT(ab.act_num, 11) AS act_num,\n" +
                    "    ab.company_name,\n" +
                    "    ab.total_no_nds,\n" +
                    "    ab.candidate,\n" +
                    "    ab.organization,\n" +
                    "    p.project_name,\n" +
                    "    pfpp.depatment AS dep_name,\n" +
                    "    pfpp.responsible_user_name,\n" +
                    "    pfpp.responsible_user_id,\n" +
                    "    pfpp.resecher_name,\n" +
                    "    pfpp.resecher_id,\n" +
                    "    pfpp.summ_resecher,\n" +
                    "    pfpp.percent_reseacher_by_candidate_percent AS resecher_percent,\n" +
                    "    pfpp.percent_responsible_user_by_candidate_percent AS candidate_percent,\n" +
                    "    pfpp.percent_responsible_user_complicity,\n" +
                    "    pfpp.percent_resecher_complicity,\n" +
                    "    pfpp.teame_leader_name,\n" +
                    "    pfpp.teame_leader_id,\n" +
                    "    pfpp.city_responsible_user,\n" +
                    "    pfpp.city_responsible_user_id,\n" +
                    "    pfpp.depatment_id,\n" +
                    "    pfpp.depatment_resecher_id,\n" +
                    "    pfpp.summ_responsible_user,\n" +
                    "    d1.dep_name AS resecher_dep_name\n" +
                    "FROM project_buh_failed_probation_period pfpp\n" +
                    "JOIN dbo.act_buh ab ON pfpp.act_id = ab.id\n" +
                    "OUTER APPLY (SELECT TOP 1 pb.depatment_id, pb.depatment_resecher_id FROM project_buh pb WHERE pb.act_id = ab.id) pb\n" +
                    "LEFT JOIN depatment d ON d.id = pfpp.depatment_id\n" +
                    "LEFT JOIN depatment d1 ON d1.id = pfpp.depatment_resecher_id\n" +
                    "LEFT JOIN dbo.project p ON p.project_id = ab.project_id\n" +
                    "WHERE CONVERT(date, ab.date_act)  BETWEEN :date1 AND :date2 or CONVERT(date, pfpp.date_update)  BETWEEN :date1 AND :date2\n" +
                    "UNION ALL\n" +
                    "SELECT\n" +
                    "    ab.id AS act_id,\n" +
                    "    ab.date_act,\n" +
                    "    LEFT(ab.act_num, 11) AS act_num,\n" +
                    "    ab.company_name,\n" +
                    "    ab.total_no_nds,\n" +
                    "    ab.candidate,\n" +
                    "    ab.organization,\n" +
                    "    p.project_name,\n" +
                    "    d.dep_name,\n" +
                    "    pb.responsible_user_name,\n" +
                    "    pb.responsible_user_id,\n" +
                    "    pb.resecher_name,\n" +
                    "    pb.resecher_id,\n" +
                    "    pb.summ_resecher,\n" +
                    "    pb.percent_reseacher_by_candidate_percent AS resecher_percent,\n" +
                    "    pb.percent_responsible_user_by_candidate_percent AS candidate_percent,\n" +
                    "    pb.percent_responsible_user_complicity,\n" +
                    "    pb.percent_resecher_complicity,\n" +
                    "    pb.teame_leader_name,\n" +
                    "    pb.teame_leader_id,\n" +
                    "    pb.city_responsible_user,\n" +
                    "    pb.city_responsible_user_id,\n" +
                    "    pb.depatment_id,\n" +
                    "    pb.depatment_resecher_id,\n" +
                    "    pb.summ_responsible_user,\n" +
                    "    d1.dep_name AS resecher_dep_name\n" +
                    "FROM dbo.act_buh ab\n" +
                    "JOIN project_buh pb ON pb.act_id = ab.id\n" +
                    "LEFT JOIN depatment d ON d.id = pb.depatment_id\n" +
                    "LEFT JOIN depatment d1 ON d1.id = pb.depatment_resecher_id\n" +
                    "LEFT JOIN dbo.project p ON p.project_id = ab.project_id\n" +
                    "WHERE CONVERT(date, ab.date_act) BETWEEN :date1 AND :date2\n" +
                    "  AND NOT EXISTS (SELECT 1 FROM project_buh_failed_probation_period fp WHERE fp.act_id = ab.id);";
    private static final String SELECT_EMPLOYEES_FOR_PLAN_MONTH =
            "select m.man_fio, m.man_id from login l " +
                    "join man m on m.man_id = l.login_user_id " +
                    "join userplanByMonth ubm on ubm.user_id = m.man_id " +
                    "where l.login_active=1 " +
                    "and ubm.userplan_year = datepart(yy, getdate()) and ubm.userplan_month =  datepart(mm, getdate()) " +
                    "order by m.man_fio";
    private static final String SELECT_DEPARTMENTS_QUERY =
            "SELECT DISTINCT id, dep_name FROM depatment WHERE dep_name IS NOT NULL " +
                    "and id not in (5,9,7,10) ORDER BY dep_name";
    private static final String DEPARTMENT_ID_COLUMN = "id";
    private static final String DEPARTMENT_NAME_COLUMN = "dep_name";

    private static final String DELETE_FAILED_PROBATION_ACT =
            "DELETE FROM project_buh_failed_probation_period WHERE act_id = :actId";

    private static final String INSERT_FAILED_PROBATION_ACT =
            "INSERT INTO project_buh_failed_probation_period (" +
                    "act_id, depatment, depatment_id, responsible_user_name, responsible_user_id, summ_responsible_user, " +
                    "percent_responsible_user_by_candidate_percent, percent_responsible_user_complicity, resecher_name, resecher_id, summ_resecher, " +
                    "percent_reseacher_by_candidate_percent, percent_resecher_complicity, depatment_resecher, depatment_resecher_id, " +
                    "teame_leader_name, teame_leader_id, city_responsible_user, city_responsible_user_id, date_update" +
                    ") VALUES (" +
                    ":actId, :departmentName, :departmentId, :responsibleUserName, :responsibleUserId, :summResponsibleUser, " +
                    ":candidatePercent, :percentResponsibleUserComplicity, :resecherName, :resecherId, :summResecher, :resecherPercent, " +
                    ":percentResecherComplicity, :resecherDepartment, :resecherDepartmentId, :teameLeaderName, :teameLeaderId, :cityResponsibleUser, :cityResponsibleUserId, GETDATE());";

    private static final String SELECT_FAILED_PROBATION_IDS =
            "SELECT id FROM project_buh_failed_probation_period WHERE act_id = :actId ORDER BY id";

    private static final String UPDATE_FAILED_PROBATION_ACT =
            "UPDATE project_buh_failed_probation_period SET " +
                    "depatment = :departmentName, " +
                    "depatment_id = :departmentId, " +
                    "responsible_user_name = :responsibleUserName, " +
                    "responsible_user_id = :responsibleUserId, " +
                    "summ_responsible_user = :summResponsibleUser, " +
                    "percent_responsible_user_by_candidate_percent = :candidatePercent, " +
                    "percent_responsible_user_complicity = :percentResponsibleUserComplicity, " +
                    "resecher_name = :resecherName, " +
                    "resecher_id = :resecherId, " +
                    "summ_resecher = :summResecher, " +
                    "percent_reseacher_by_candidate_percent = :resecherPercent, " +
                    "percent_resecher_complicity = :percentResecherComplicity, " +
                    "depatment_resecher = :resecherDepartment, " +
                    "depatment_resecher_id = :resecherDepartmentId, " +
                    "teame_leader_name = :teameLeaderName, " +
                    "teame_leader_id = :teameLeaderId, " +
                    "city_responsible_user = :cityResponsibleUser, " +
                    "city_responsible_user_id = :cityResponsibleUserId, " +
                    "date_update = GETDATE() " +
                    "WHERE id = :id";

    private static final String DELETE_FAILED_PROBATION_ACT_BY_ID =
            "DELETE FROM project_buh_failed_probation_period WHERE id = :id";


    public List<UserSalary> getAllUserSalary(LocalDate date1, LocalDate date2, Integer departmentId) {
        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_SALARY_PAYMENT_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2)
                    .addParameter("department_id", departmentId);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            List<UserSalary> userSalaryList = new ArrayList<>();

            Locale ru = new Locale("ru", "RU");
            Currency rub = Currency.getInstance(ru);
            NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);

            for (Map<String, Object> n : list) {
                UserSalary userSalary = new UserSalary();

                boolean isNotNew = false;
                String manFIO = null;

                double allsumm = 0;

                for (var entry : n.entrySet()) {

                    if (entry.getKey().equals("man_id")) {
                        userSalary.setUserId((Integer) entry.getValue());
                    }
                    if (entry.getKey().equals("man_fio")) {
                        userSalary.setUserFio((String) entry.getValue());
                    }

                    if (entry.getKey().equals("dep_name")) {
                        userSalary.setDepartment((String) entry.getValue());
                    }
                    if (entry.getKey().equals("pos_name")) {
                        userSalary.setPosition((String) entry.getValue());
                    }
                    if (entry.getKey().equals("bonuskpi")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        userSalary.setUserBonusKPI(currencyInstance.format(d));

                    }
                    if (entry.getKey().equals("bonus")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        userSalary.setUserBonus(currencyInstance.format(d));

                    }
                    if (entry.getKey().equals("bonus_bdm_kpi")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        userSalary.setUserBonusBDMKPI(currencyInstance.format(d));

                    }

                    if (entry.getKey().equals("bonus_project_bdm")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        userSalary.setUserBonusProjectBDM(currencyInstance.format(d));
                    }

                    if (entry.getKey().equals("manzp")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        userSalary.setUserSalary(currencyInstance.format(d));
                    }

                    if (entry.getKey().equals("allsumm")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        allsumm = d;
                        userSalary.setUserSalaryAll(currencyInstance.format(d));

                    }

                    if (entry.getKey().equals("allsumm_without_coef")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        allsumm = d;
                        userSalary.setUserSalaryAllWithOutCoef(currencyInstance.format(d));

                    }
                    if (entry.getKey().equals("salary_month")) {
                        userSalary.setSalaryMonth((Integer) entry.getValue());
                    }

                    if (entry.getKey().equals("salary_month_str")) {
                        if (!(entry.getValue()).equals("")) {
                            userSalary.setSalaryMonthStr((String) entry.getValue());
                        }

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

    /**
     * Возвращает первое ненулевое числовое значение из указанного набора ключей.
     * Помогает работать с функциями, которые могут возвращать сумму бонуса в разных колонках.
     */
    private BigDecimal firstNonZeroDecimal(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value == null) continue;

            BigDecimal decimalValue = null;
            if (value instanceof BigDecimal) {
                decimalValue = (BigDecimal) value;
            } else if (value instanceof Number) {
                decimalValue = BigDecimal.valueOf(((Number) value).doubleValue());
            } else if (value instanceof String) {
                try {
                    decimalValue = new BigDecimal((String) value);
                } catch (NumberFormatException ignored) {
                }
            }

            if (decimalValue != null && decimalValue.doubleValue() != 0.0) {
                return decimalValue;
            }
        }
        return null;
    }


    public List<MarginBonusBDM> getMarginBonus(LocalDate date1, LocalDate date2) {
        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_MARGIN_QUARTER_QUERY, false)
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

                boolean isNotNew = false;
                String departmentName = null;
                String divisionName = null;


                List<String> earnedMoneyDepartmentList = new ArrayList<>();
                List<String> earnedMoneyDivisionList = new ArrayList<>();
                List<String> paidMoneyList = new ArrayList<>();
                List<String> marginDepartment = new ArrayList<>();
                List<Double> marginDepartmentSum = new ArrayList<>();
                List<String> marginDivision = new ArrayList<>();
                List<String> marginBonusBDMList = new ArrayList<>();
                List<Integer> marginQuarter = new ArrayList<>();


                for (var entry : n.entrySet()) {

                    if (entry.getKey().equals("department_name")) {
                        for (int j = 0; j < marginBonusList.size(); j++) {
                            if (marginBonusList.get(j).getDepartmentName().equals(entry.getValue())) {
                                departmentName = (String) entry.getValue();//
                                isNotNew = true;
                            }
                        }
                        marginBonus.setDepartmentName((String) entry.getValue());
                    }

                    if (entry.getKey().equals("division_name")) {
                        for (int j = 0; j < marginBonusList.size(); j++) {
                            if (marginBonusList.get(j).getDivisionName().equals(entry.getValue())) {
                                divisionName = (String) entry.getValue();//
                            }
                        }
                        marginBonus.setDivisionName((String) entry.getValue());
                    }
                    if (entry.getKey().equals("money_earned_division")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            earnedMoneyDivisionList.add(currencyInstance.format(d));
                            marginBonus.setEarnedMoneyDivision(earnedMoneyDivisionList);
                        }
                    }
                    if (entry.getKey().equals("earned_money_department")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            earnedMoneyDepartmentList.add(currencyInstance.format(d));
                            marginBonus.setEarnedMoneyDepartment(earnedMoneyDepartmentList);
                        }
                    }

                    if (entry.getKey().equals("paid_money")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            paidMoneyList.add(currencyInstance.format(d));
                            marginBonus.setPaidMoney(paidMoneyList);
                        }
                    }

                    if (entry.getKey().equals("margin_department")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginDepartmentSum.add(d);
                            marginDepartment.add(currencyInstance.format(d));
                            marginBonus.setMarginDepartment(marginDepartment);
                            marginBonus.setMarginDepartmentSum(marginDepartmentSum);
                        }
                    }
                    if (entry.getKey().equals("margin_division")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginDivision.add(currencyInstance.format(d));
                            marginBonus.setMarginDivision(marginDivision);
                        }
                    }
                    if (entry.getKey().equals("margin_bonus")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginBonusBDMList.add(currencyInstance.format(d));
                            marginBonus.setMarginBonusBDM(marginBonusBDMList);
                        }
                    }
                    if (entry.getKey().equals("quat")) {
                        marginQuarter.add((Integer) entry.getValue());
                        marginBonus.setMarginQuarter(marginQuarter);
                    }
                    if (entry.getKey().equals("ya")) {
                        marginBonus.setMarginYear((Integer) entry.getValue());
                    }
                }

                if (isNotNew ) {
                    String finalDepartmentName = departmentName;
                    List<MarginBonusBDM> result = marginBonusList.stream()
                            .filter(a -> Objects.equals(a.getDepartmentName(), finalDepartmentName))
                            .collect(toList());

                    if (marginBonus.getEarnedMoneyDivision() != null) {
                        if (result.get(0).getEarnedMoneyDivision() == null)
                            result.get(0).setEarnedMoneyDivision(marginBonus.getEarnedMoneyDivision());
                        else
                            result.get(0).getEarnedMoneyDivision().add(marginBonus.getEarnedMoneyDivision().get(0));
                    }

                    if (marginBonus.getEarnedMoneyDepartment() != null) {
                        if (result.get(0).getEarnedMoneyDepartment() == null)
                            result.get(0).setEarnedMoneyDepartment(marginBonus.getEarnedMoneyDepartment());
                        else
                            result.get(0).getEarnedMoneyDepartment().add(marginBonus.getEarnedMoneyDepartment().get(0));
                    }

                    if (marginBonus.getPaidMoney() != null) {
                        if (result.get(0).getPaidMoney() == null)
                            result.get(0).setPaidMoney(marginBonus.getPaidMoney());
                        else if (!result.get(0).getPaidMoney().contains(marginBonus.getPaidMoney().get(0))) {
                            result.get(0).getPaidMoney().add(marginBonus.getPaidMoney().get(0));
                        }
                    }
                    if (marginBonus.getMarginDepartment() != null) {
                        if (result.get(0).getMarginDepartment() == null) {
                            result.get(0).setMarginDepartment(marginBonus.getMarginDepartment());
                            result.get(0).setMarginDepartmentSum(marginBonus.getMarginDepartmentSum());
                        }
                        else if (!result.get(0).getMarginDepartment().contains(marginBonus.getMarginDepartment().get(0))) {
                            result.get(0).getMarginDepartment().add(marginBonus.getMarginDepartment().get(0));
                            result.get(0).getMarginDepartmentSum().add(marginBonus.getMarginDepartmentSum().get(0));
                        }
                    }

                    if (marginBonus.getMarginDivision() != null) {
                        if (result.get(0).getMarginDivision() == null) {
                            result.get(0).setMarginDivision(marginBonus.getMarginDivision());
                        } else {
                            result.get(0).getMarginDivision().add(marginBonus.getMarginDivision().get(0));

                        }
                    }

                    if (marginBonus.getMarginBonusBDM() != null) {
                        if (result.get(0).getMarginBonusBDM() == null) {
                            result.get(0).setMarginBonusBDM(marginBonus.getMarginBonusBDM());
                        } else {
                            result.get(0).getMarginBonusBDM().add(marginBonus.getMarginBonusBDM().get(0));

                        }
                    }

                    if (marginBonus.getMarginQuarter() != null) {
                        if (result.get(0).getMarginQuarter() == null) {
                            result.get(0).setMarginQuarter(marginBonus.getMarginQuarter());
                        } else {
                            result.get(0).getMarginQuarter().add(marginBonus.getMarginQuarter().get(0));

                        }
                    }

                } else {
                    marginBonusList.add(marginBonus);
                }
            }

            return marginBonusList;

        }

    }

    public List<Department> getAllDepartments() {
        try (Connection connection = sql2o.open()) {
            Table table = connection.createQuery(SELECT_DEPARTMENTS_QUERY, false)
                    .executeAndFetchTable();

            if (table == null) {
                return Collections.emptyList();
            }

            List<Map<String, Object>> rows = table.asList();

            if (rows == null) {
                return Collections.emptyList();
            }

            Map<String, Department> departments = new LinkedHashMap<>();

            for (Map<String, Object> row : rows) {
                Department department = mapRowToDepartment(row);
                if (department == null || department.getName() == null || department.getName().isEmpty()) {
                    continue;
                }

                String key = department.getId() != null
                        ? "ID:" + department.getId()
                        : "NAME:" + department.getName().toLowerCase(Locale.ROOT);

                departments.putIfAbsent(key, department);
            }

            return Collections.unmodifiableList(new ArrayList<>(departments.values()));
        }
    }

    public List<Employees> getEmployeesForCurrentPlanMonth() {
        try (Connection connection = sql2o.open()) {
            Table table = connection.createQuery(SELECT_EMPLOYEES_FOR_PLAN_MONTH, false)
                    .executeAndFetchTable();

            if (table == null) {
                return Collections.emptyList();
            }

            Map<Integer, Employees> employees = new LinkedHashMap<>();
            for (Map<String, Object> row : table.asList()) {
                Employees employee = mapRowToEmployee(row);
                if (employee != null) {
                    employees.putIfAbsent(employee.getManId(), employee);
                }
            }

            return new ArrayList<>(employees.values());
        }
    }

    private Department mapRowToDepartment(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return null;
        }

        Department department = new Department();
        department.setId(extractInteger(row.get(DEPARTMENT_ID_COLUMN)));

        Object nameValue = row.get(DEPARTMENT_NAME_COLUMN);
        if (nameValue != null) {
            String name = nameValue.toString().trim();
            if (!name.isEmpty()) {
                department.setName(name);
            }
        }

        if (department.getId() == null && department.getName() == null) {
            return null;
        }

        return department;
    }

    private Employees mapRowToEmployee(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return null;
        }

        Integer manId = extractInteger(row.get("man_id"));
        Object fioValue = row.get("man_fio");

        if (manId == null || fioValue == null) {
            return null;
        }

        Employees employee = new Employees();
        employee.setManId(manId);
        employee.setManFIO(fioValue.toString());
        return employee;
    }

    private Integer extractInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            String trimmed = ((String) value).trim();
            if (!trimmed.isEmpty()) {
                try {
                    return Integer.valueOf(trimmed);
                } catch (NumberFormatException ex) {
                    log.warn("Unable to parse department id '{}'", value);
                }
            }
        }

        return null;
    }

    public List<DepartmentUserSales> getAllUsersSales(LocalDate date1, LocalDate date2) {

        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_SALES_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();
            List<DepartmentUserSales> depUserSaleList = new ArrayList<>();
            Locale ru = new Locale("ru", "RU");
            Currency rub = Currency.getInstance(ru);
            NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);
            DateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");
            final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MMM-dd");

            DepartmentUserSales depUserSales = new DepartmentUserSales();

            for (Map<String, Object> n : list) {


                EmployerNew employerNew = EmployerNewBuilder.anEmployer().build();
                Act act = ActBuilder.anAct().build();
                List<EmployerNew> employerNewList = new ArrayList();
                List<Act> actList = new ArrayList();


                boolean isNotNew = false;
                String departmentName = null;
                String userName = null;

                for (var entry : n.entrySet()) {



                    if (entry.getKey().equals("depatment_id")) {
                        for (int j = 0; j < depUserSaleList.size(); j++) {
                            if (depUserSaleList.get(j).getDepartmentId() == (entry.getValue())) {//
                                isNotNew = true;
                            }
                        }
                        depUserSales.setDepartmentId((Integer) entry.getValue());
                    }
                    if (entry.getKey().equals("dep_name")) {
                        departmentName = (String) entry.getValue();
                        depUserSales.setDepartment((String) entry.getValue());
                    }

                    if (entry.getKey().equals("responsible_user_name")) {
                        userName = (String) entry.getValue();
                        employerNew.setManFIO((String) entry.getValue());
                    }

                    if (entry.getKey().equals("resecher_name")) {
                        userName = (String) entry.getValue();
                        employerNew.setManFIO((String) entry.getValue());
                    }

                    if (entry.getKey().equals("summ_responsible_user")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            act.setBonus(d);
                        }
                    }

                    if (entry.getKey().equals("summ_resecher")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            act.setBonus(d);
                        }
                    }
                    if (entry.getKey().equals("candidate")) {
                        act.setCandidate((String) entry.getValue());
                    }

                    if (entry.getKey().equals("company_name")) {
                        act.setCompanies((String) entry.getValue());
                    }
                    if (entry.getKey().equals("date_act")) {
                        if (entry.getValue() != null) {
                            act.setDate(formatter1.format((Date) entry.getValue()));
                            act.setDateAct(convertToLocalDateViaSqlDate((Date) entry.getValue()));
                        }
                        else act.setDate("");
                    }

                    if (entry.getKey().equals("act_num")) {
                        act.setNum((String) entry.getValue());
                    }

                    if (entry.getKey().equals("project_name")) {
                        act.setProjectName((String) entry.getValue());
                    }

                }

                actList.add(act);
                employerNew.setActList(actList);
                employerNewList.add(employerNew);
                depUserSales.setUserSaleList(employerNewList);
                depUserSaleList.add(depUserSales);

                Map<Integer, List<Act>> actMap = actList.stream().collect(
                        groupingBy(Act::getId));

                Map<String, List<EmployerNew>> employerMap = employerNewList.stream().collect(
                        groupingBy(EmployerNew::getManFIO));


                Map<String, List<DepartmentUserSales>> departmentUserSalesMap = depUserSaleList.stream().collect(
                        groupingBy(DepartmentUserSales::getDepartment));


                for (Map.Entry<String, List<DepartmentUserSales>> item : departmentUserSalesMap.entrySet()) {
                    if (item.getKey().equals(departmentName)) {
                        if (item.getValue().get(0).getUserSaleList().get(0).getManFIO().equals(userName)) {
                            item.getValue().get(0).getUserSaleList().get(0).getActList().add(act);
                        }
                        else item.getValue().get(0).getUserSaleList().add(employerNew);
                    }
                }

            }

            depUserSaleList.stream().map(s1 -> s1.getUserSaleList().stream().collect(groupingBy(EmployerNew::getManFIO))).collect(Collectors.toList());



            return depUserSaleList;

            }
        }

    private LocalDate convertToLocalDateViaSqlDate(Date dateToConvert) {
        return new java.sql.Date(dateToConvert.getTime()).toLocalDate();
    }

    public List<MarginBonus> getMarginBonusByMonth(LocalDate date1, LocalDate date2) {
        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_MARGIN_MONTH_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();

            List<MarginBonus> marginBonusByMonthList = new ArrayList<>();
            Locale ru = new Locale("ru", "RU");
            NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);


            for (Map<String, Object> n : list) {
                MarginBonus marginBonus = new MarginBonus();

                for (var entry : n.entrySet()) {

                    if (entry.getKey().equals("dep_name")) {
                        marginBonus.setDepName((String) entry.getValue());
                    }

                    if (entry.getKey().equals("summ_zarabotano_depatment")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginBonus.setAmountEarned(d);
                            marginBonus.setAmountEarnedRUB(currencyInstance.format(d));
                        }
                    }

                    if (entry.getKey().equals("sum_salary_depatment")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginBonus.setAmountPaid(d);
                            marginBonus.setAmountPaidRUB(currencyInstance.format(d));
                        }
                    }

                    if (entry.getKey().equals("summ_vsego_potracheno")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginBonus.setAmountPaidAllPeriod(d);
                            marginBonus.setAmountPaidAllPeriodRUB(currencyInstance.format(d));
                        }
                    }

                    if (entry.getKey().equals("summ_vsego_zarabotano")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginBonus.setAmountEarnedAllPeriod(d);
                            marginBonus.setAmountEarnedAllPeriodRUB(currencyInstance.format(d));
                        }
                    }

                    if (entry.getKey().equals("margin")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginBonus.setMargin(d);
                            marginBonus.setMarginRUB(currencyInstance.format(d));
                        }
                    }
                    if (entry.getKey().equals("vsego_margin")) {
                        BigDecimal bd = (BigDecimal) entry.getValue();
                        double d = bd.doubleValue();
                        if (d != 0.0) {
                            marginBonus.setMarginAllPeriod(d);
                            marginBonus.setMarginAllPeriodRUB(currencyInstance.format(d));
                        }
                    }
                    if (entry.getKey().equals("salary_year")) {
                        marginBonus.setSalaryYear((Integer) entry.getValue());
                    }
                    if (entry.getKey().equals("salary_month")) {
                        marginBonus.setSalaryMonth((String) entry.getValue());
                    }
                }

                   marginBonusByMonthList.add(marginBonus);
            }

            return marginBonusByMonthList;

        }

    }

    public List<UserSalaryDetail> getAllUserSalaryInterpreter(LocalDate date1, LocalDate date2) {
        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery(SELECT_SALARY_PAYMENT_INTERPRETER_QUERY, false)
                    .addParameter("date1", date1)
                    .addParameter("date2", date2);

            Table table = query.executeAndFetchTable();
            List<Map<String, Object>> list = table.asList();
            List<UserSalaryDetail> userSalaryDetailList = new ArrayList<>();
            Locale ru = new Locale("ru", "RU");
            Currency rub = Currency.getInstance(ru);
            NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);

            // Создаем Map для временного хранения данных по пользователям
            Map<String, UserSalaryDetail> userMap = new HashMap<>();

            for (Map<String, Object> n : list) {
                String userFio = (String) n.get("man_fio");

                // Проверяем, есть ли уже такой пользователь
                UserSalaryDetail userSalaryDetail = userMap.get(userFio);

                if (userSalaryDetail == null) {
                    // Создаем нового пользователя
                    userSalaryDetail = new UserSalaryDetail();
                    userSalaryDetail.setUserFio(userFio);
                    userSalaryDetail.setDepartment((String) n.get("dep_name"));
                    userSalaryDetail.setPosition((String) n.get("pos_name"));
                    userSalaryDetail.setSalaryList(new ArrayList<>());
                    userMap.put(userFio, userSalaryDetail);
                }

                // Создаем новую запись о зарплате
                Salary salary = new Salary();

                // Заполняем данные о зарплате
                BigDecimal salaryAll = (BigDecimal) n.get("man_salary_all");
                if (salaryAll != null && salaryAll.doubleValue() != 0.0) {
                    salary.setUserSalaryAllRUB(currencyInstance.format(salaryAll.doubleValue()));
                }

                BigDecimal bonus = (BigDecimal) n.get("man_bonus");
                if (bonus != null && bonus.doubleValue() != 0.0) {
                    salary.setUserBonusRUB(currencyInstance.format(bonus.doubleValue()));
                }
                BigDecimal bonusBDMKPI = (BigDecimal) n.get("man_bonus_bdm_kpi");
                if (bonusBDMKPI != null && bonusBDMKPI.doubleValue() != 0.0) {
                    salary.setUserBonusBDMKPI(currencyInstance.format(bonusBDMKPI.doubleValue()));
                }

                BigDecimal bonusProjectBDM = (BigDecimal) n.get("man_project_bonus");
                if (bonusProjectBDM != null && bonusProjectBDM.doubleValue() != 0.0) {
                    salary.setUserBonusProjectBDM(currencyInstance.format(bonusProjectBDM.doubleValue()));
                }

                BigDecimal bonusBdm = (BigDecimal) n.get("man_bonus_bdm");
                if (bonusBdm != null && bonusBdm.doubleValue() != 0.0) {
                    salary.setUserBonusBDMRUB(formatCurrency(bonusBdm, ru));
//                    salary.setUserBonusBDMRUB(currencyInstance.format(bonusBdm.doubleValue()));
                }
                BigDecimal salaryNDFL = (BigDecimal) n.get("man_salary_ndfl");
                if (salaryNDFL != null && salaryNDFL.doubleValue() != 0.0) {
                    salary.setUserSalaryNDFLRUB(formatCurrency(salaryNDFL, ru));
//                    salary.setUserBonusBDMRUB(currencyInstance.format(bonusBdm.doubleValue()));
                }
                BigDecimal salarySingle = (BigDecimal) n.get("man_salary_single");
                if (salarySingle != null && salarySingle.doubleValue() != 0.0) {
                    salary.setUserSalarySingleRUB(formatCurrency(salarySingle, ru));
//                    salary.setUserBonusBDMRUB(currencyInstance.format(bonusBdm.doubleValue()));
                }
                BigDecimal manZP = (BigDecimal) n.get("manzp");
                if (manZP != null && manZP.doubleValue() != 0.0) {
                    salary.setUserSalaryRUB(formatCurrency(manZP, ru));
//                    salary.setUserBonusBDMRUB(currencyInstance.format(bonusBdm.doubleValue()));
                }
                BigDecimal salarySickDaysPay = (BigDecimal) n.get("salary_sick_days_pay");
                if (salarySickDaysPay != null && salarySickDaysPay.doubleValue() != 0.0) {
                    salary.setSalarySickDaysPayRUB(currencyInstance.format(salarySickDaysPay.doubleValue()));
                }
                BigDecimal salaryVacationPay = (BigDecimal) n.get("salary_vacation_pay");
                if (salaryVacationPay != null && salaryVacationPay.doubleValue() != 0.0) {
                    salary.setSalaryVacationPayRUB(currencyInstance.format(salaryVacationPay.doubleValue()));
                }

                // Аналогично заполняем остальные поля salary...

                salary.setSalaryMonthStr((String) n.get("salary_month_str"));
                salary.setSalaryMonth((Integer) n.get("salary_month"));
                salary.setSalaryYear((Integer) n.get("salary_year"));
                salary.setSalaryQuarter((Integer) n.get("salary_quarter"));

                // Добавляем зарплату только к текущему пользователю
                userSalaryDetail.getSalaryList().add(salary);
            }

            // Преобразуем Map обратно в List
            return new ArrayList<>(userMap.values());
        }

    }

    public List<UserSalarySuccess> getUserSalarySuccess(LocalDate dateFrom, LocalDate dateTo) {
        try (Connection connection = sql2o.open()) {
            List<Map<String, Object>> rows = connection.createQuery(SELECT_SALARY_PAYMENT_SUCCESS_QUERY)
                    .addParameter("dateFrom", dateFrom)
                    .addParameter("dateTo", dateTo)
                    .executeAndFetchTable()
                    .asList();

            return rows.stream().map(row -> {
                UserSalarySuccess item = new UserSalarySuccess();
                item.setUserId((Integer) row.get("user_id"));
                item.setUserFio((String) row.get("man_fio"));

                // Конвертируем java.sql.Date в LocalDate
                java.sql.Date sqlDateFrom = (java.sql.Date) row.get("datefrom");
                java.sql.Date sqlDateTo = (java.sql.Date) row.get("dateto");

                item.setDateFrom(sqlDateFrom != null ? sqlDateFrom.toLocalDate() : null);
                item.setDateTo(sqlDateTo != null ? sqlDateTo.toLocalDate() : null);

                item.setSuccess((Integer) row.get("success"));
                return item;
            }).collect(Collectors.toList());
        }
    }

    public List<ActByUserCheck> getActByUserCheck(LocalDate dateFrom, LocalDate dateTo) {
        try (Connection connection = sql2o.open()) {
            List<Map<String, Object>> rows = connection.createQuery(SELECT_ACT_BY_USER_CHECK)
                    .addParameter("date1", dateFrom)
                    .addParameter("date2", dateTo)
                    .executeAndFetchTable()
                    .asList();

            return rows.stream().map(row -> {
                ActByUserCheck item = new ActByUserCheck();
                item.setActId((Integer) row.get("act_id"));
                // Правильное преобразование даты
                Object dateValue = row.get("date_act");
                if (dateValue instanceof java.sql.Timestamp) {
                    java.sql.Timestamp timestamp = (java.sql.Timestamp) dateValue;
                    item.setDateAct(timestamp.toLocalDateTime().toLocalDate());
                } else if (dateValue instanceof java.sql.Date) {
                    item.setDateAct(((java.sql.Date) dateValue).toLocalDate());
                } else {
                    item.setDateAct(null);
                }
//                java.sql.Date dateAct = (java.sql.Date) row.get("date_act");
//                item.setDateAct(dateAct != null ? dateAct.toLocalDate() : null);
                item.setActNum((String) row.get("act_num"));
                item.setCompanyName((String) row.get("company_name"));
                Double totalNoNds = (Double) row.get("total_no_nds");
                item.setTotalNoNds(totalNoNds != null ? totalNoNds : 0.0);
                item.setProjectName((String) row.get("project_name"));
                item.setCandidate((String) row.get("candidate"));
                item.setOrganization((String) row.get("organization"));
                item.setDepartmentName((String) row.get("dep_name"));
                item.setResecherDepartmentName((String) row.get("resecher_dep_name"));
                item.setResponsibleUserName((String) row.get("responsible_user_name"));
                item.setResecherName((String) row.get("resecher_name"));
                Double summResecher = (Double) row.get("summ_resecher");
                item.setSummResecher(summResecher != null ? summResecher : 0.0);
                Double candidatePercent = (Double) row.get("candidate_percent");
                item.setCandidatePercent(candidatePercent != null ? candidatePercent : 0.0);
                Double resecherPercent = (Double) row.get("resecher_percent");
                item.setResecherPercent(resecherPercent != null ? resecherPercent : 0.0);
                Double summResponsibleUser = (Double) row.get("summ_responsible_user");
                item.setSummResponsibleUser(summResponsibleUser != null ? summResponsibleUser : 0.0);
                item.setResponsibleUserId((Integer) row.get("responsible_user_id"));
                item.setResecherId((Integer) row.get("resecher_id"));
                item.setDepartmentId((Integer) row.get("depatment_id"));
                item.setDepatmentResecherId((Integer) row.get("depatment_resecher_id"));
                item.setPercentResponsibleUserComplicity((Double) row.get("percent_responsible_user_complicity"));
                item.setPercentResecherComplicity((Double) row.get("percent_resecher_complicity"));
                item.setTeameLeaderName((String) row.get("teame_leader_name"));
                item.setTeameLeaderId((Integer) row.get("teame_leader_id"));
                item.setCityResponsibleUser((String) row.get("city_responsible_user"));
                item.setCityResponsibleUserId((Integer) row.get("city_responsible_user_id"));

                return item;
            }).collect(Collectors.toList());
        }
    }

    @Transactional
    public void saveFailedProbationAct(Integer actId, Double totalNoNds, String candidate,
                                       List<ActByUserCheck> participants) {
        log.info("[UpdateAct] Persisting actId={}, participants={} (totalNoNds={}, candidate={})",
                actId, participants != null ? participants.size() : 0,
                totalNoNds, candidate);

        List<ActByUserCheck> safeParticipants = participants != null ? participants : Collections.emptyList();
        List<Integer> existingIds = jdbcTemplate.query(SELECT_FAILED_PROBATION_IDS,
                Map.of("actId", actId),
                (rs, rowNum) -> rs.getInt("id"));

        if (!safeParticipants.isEmpty()) {
            int updated = 0;
            int inserted = 0;

            for (int i = 0; i < safeParticipants.size(); i++) {
                ActByUserCheck participant = safeParticipants.get(i);

                if (participant.getResecherName() == null) {
                    participant.setResecherDepartmentName(null);
                    participant.setDepatmentResecherId(null);
                }

                log.debug("[UpdateAct] Saving participant #{}: consultant='{}' researcher='{}' sumC={} sumR={}"
                                + " pctC={} pctR={} depC={} depR={}",
                        i + 1,
                        participant.getResponsibleUserName(), participant.getResecherName(),
                        participant.getSummResponsibleUser(), participant.getSummResecher(),
                        participant.getPercentResponsibleUserComplicity(),
                        participant.getPercentResecherComplicity(),
                        participant.getDepartmentName(), participant.getResecherDepartmentName());

                Map<String, Object> params = new HashMap<>();
                params.put("actId", actId);
                params.put("departmentName", participant.getDepartmentName());
                params.put("departmentId", participant.getDepartmentId());
                params.put("responsibleUserName", participant.getResponsibleUserName());
                params.put("responsibleUserId", participant.getResponsibleUserId());
                params.put("summResponsibleUser", participant.getSummResponsibleUser());
                params.put("candidatePercent", participant.getCandidatePercent());
                params.put("percentResponsibleUserComplicity", participant.getPercentResponsibleUserComplicity());
                params.put("resecherName", participant.getResecherName());
                params.put("resecherId", participant.getResecherId());
                params.put("summResecher", participant.getSummResecher());
                params.put("resecherPercent", participant.getResecherPercent());
                params.put("percentResecherComplicity", participant.getPercentResecherComplicity());
                params.put("resecherDepartment", participant.getResecherDepartmentName());
                params.put("resecherDepartmentId", participant.getDepatmentResecherId());
                params.put("teameLeaderName", participant.getTeameLeaderName());
                params.put("teameLeaderId", participant.getTeameLeaderId());
                params.put("cityResponsibleUser", participant.getCityResponsibleUser());
                params.put("cityResponsibleUserId", participant.getCityResponsibleUserId());

                if (i < existingIds.size()) {
                    params.put("id", existingIds.get(i));
                    jdbcTemplate.update(UPDATE_FAILED_PROBATION_ACT, params);
                    updated++;
                } else {
                    jdbcTemplate.update(INSERT_FAILED_PROBATION_ACT, params);
                    inserted++;
                }
            }

            if (existingIds.size() > safeParticipants.size()) {
                for (int i = safeParticipants.size(); i < existingIds.size(); i++) {
                    jdbcTemplate.update(DELETE_FAILED_PROBATION_ACT_BY_ID, Map.of("id", existingIds.get(i)));
                }
            }

            log.info("[UpdateAct] Updated rows: {}, inserted rows: {}, deleted rows: {}", updated, inserted,
                    Math.max(existingIds.size() - safeParticipants.size(), 0));
        } else {
            int deleted = jdbcTemplate.update(DELETE_FAILED_PROBATION_ACT, Map.of("actId", actId));
            log.warn("[UpdateAct] No participants provided for actId={}, removed existing rows: {}", actId,
                    deleted);
        }
    }
}
