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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.thymeleaf.util.NumberUtils.formatCurrency;

@Component
public class UserSalaryRepImplRep  {

    private final Sql2o sql2o;

    public UserSalaryRepImplRep(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    private static final String SELECT_SALARY_PAYMENT_QUERY = "select * from fn_salary_for_all_user(:date1,:date2, :department_id)\n";
    private static final String SELECT_MARGIN_QUARTER_QUERY =  "select * from fn_margin_bonus_by_quarter (:date1,:date2)";
    private static final String SELECT_MARGIN_MONTH_QUERY =  "select * from fn_marginality_by_month (:date1,:date2)";
    private static final String SELECT_SALES_QUERY =  "select * from [fn_User_Sale] (:date1, :date2)";
    private static final String SELECT_SALARY_PAYMENT_INTERPRETER_QUERY = "select * from fn_salary_for_all_user_sverka(:date1,:date2) order by dep_name, salary_month, man_fio\n";


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
                            marginDepartment.add(currencyInstance.format(d));
                            marginBonus.setMarginDepartment(marginDepartment);
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
                        if (result.get(0).getMarginDepartment() == null)
                            result.get(0).setMarginDepartment(marginBonus.getMarginDepartment());
                        else if (!result.get(0).getMarginDepartment().contains(marginBonus.getMarginDepartment().get(0))) {
                            result.get(0).getMarginDepartment().add(marginBonus.getMarginDepartment().get(0));
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

}
