package ru.alta.thirdproj.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.ActByCompanyByDepartment;
import ru.alta.thirdproj.entites.MoneyByFinalist;
import ru.alta.thirdproj.services.ActPutServiceImpl;
import ru.alta.thirdproj.services.ExpectedMoneyByFinalistService;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class ActsController {
    private static final Logger logger = LoggerFactory.getLogger(ActsController.class);



    private ActPutServiceImpl actBonusPercentService;
    private ExpectedMoneyByFinalistService moneyByFinalistService;

    @Autowired
    public void setActBonusPercentService(ActPutServiceImpl actBonusPercentService) {
        this.actBonusPercentService = actBonusPercentService;
    }

    @Autowired
    public void setMoneyByFinalistService(ExpectedMoneyByFinalistService moneyByFinalistService) {
        this.moneyByFinalistService = moneyByFinalistService;
    }

    @GetMapping("/acts")
    public String showActsPage(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            Model model) {

        prepareCommonModel(model, dateFrom, dateTo);
        return "acts"; // имя вашего основного шаблона (index.html)
    }

    // Общий метод для подготовки данных
    private void prepareCommonModel(Model model, LocalDate date1, LocalDate date2) {
        Locale ru = new Locale("ru", "RU");
        NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);

        List<Act> actList = actBonusPercentService.getAllPutAct(date1, date2)
                .stream()
                .sorted(Comparator.comparingInt(Act::getId))
                .collect(Collectors.toList());

        List<Act> actNoPayList = actBonusPercentService.getANoPaymentAct(date1);
        List<MoneyByFinalist> moneyByFinalists = moneyByFinalistService.getMoneyByFinalistList();

        // Расчеты сумм
        double allActMoney = actNoPayList.stream().mapToDouble(Act::getBonus).sum();

        double allActMoneyPeriod = actList.stream()
                .filter(e -> !e.isPaid())
                .mapToDouble(Act::getBonus)
                .sum();


        double allActForClientMoneyPeriod = actList.stream()
                .filter(act -> !act.getDateAct().isBefore(date1) && !act.getDateAct().isAfter(date2))
                .mapToDouble(Act::getBonus)
                .sum();

        double allActMoneyPeriodPaid = actList.stream()
                .filter(e -> e.getPaymentDate() != null)
                .mapToDouble(Act::getBonus)
                .sum();

        double allFinalistMoneyPeriodPaid = moneyByFinalists.stream()
                .filter(e -> e.getProjectFee() != null)
                .mapToDouble(e -> e.getProjectFee().doubleValue())
                .sum();

        Map<String, Double> allActForClientMoneyPeriodByCompany = actList.stream()
                .filter(act -> !act.getDateAct().isBefore(date1) && !act.getDateAct().isAfter(date2))
                .collect(Collectors.groupingBy(Act::getOrganization,
                        Collectors.summingDouble(Act::getBonus)));

        String allActForClientMoneyPeriodByCompanyString = allActForClientMoneyPeriodByCompany.entrySet().stream()
                .map(entry -> String.format("%s: %s",
                        entry.getKey(),
                        currencyInstance.format(entry.getValue())))
                .collect(Collectors.joining(", "));


        // Добавление атрибутов модели
        model.addAttribute("actNoPayList", actNoPayList);
        model.addAttribute("actPutList", actList);
        model.addAttribute("moneyByFinalists", moneyByFinalists);
        model.addAttribute("allActMoney", currencyInstance.format(allActMoney));
        model.addAttribute("allActMoneyPeriod", currencyInstance.format(allActMoneyPeriod));
        model.addAttribute("allActMoneyPeriodPaid", currencyInstance.format(allActMoneyPeriodPaid));
        model.addAttribute("allActForClientMoneyPeriod", currencyInstance.format(allActForClientMoneyPeriod));
        model.addAttribute("allFinalistMoneyPeriodPaid", currencyInstance.format(allFinalistMoneyPeriodPaid));
        model.addAttribute("allActForClientMoneyPeriodByCompanyString", allActForClientMoneyPeriodByCompanyString);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
    }

    @GetMapping("/acts/actAll")
    public String showAll(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
                          Model model) {
        prepareCommonModel(model, dateFrom, dateTo);
        return "fragments/actPeriodTable :: actPeriodTab";
    }

    @GetMapping("/acts/unpaidActs")
    public String showUnpaidActs(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
                                 Model model) {
        prepareCommonModel(model, dateFrom, dateTo);
        return "fragments/actUnpaidTable :: actUnpaidTab";
    }

    @GetMapping("/acts/expectedExits")
    public String showExpectedExits(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
                                    Model model) {
        prepareCommonModel(model, dateFrom, dateTo);
        return "fragments/actExpectedTable :: actExpectedTab";

    }

    @GetMapping("/acts/departmentActs")
    public String showActsByDepartment(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            @RequestParam(required = false) String companyFilter,
            Model model) {

        // Получаем исходные данные (все акты за период)
        List<ActByCompanyByDepartment> allActs = actBonusPercentService.getActByDepartmentByCompany(dateFrom, dateTo);

        List<ActByCompanyByDepartment> actsByDepartment = allActs;
        allActs = groupActsByDepartmentPercentage(allActs);
        // Всегда добавляем суммы по направлениям из всех данных
        model.addAttribute("formattedDepartmentSums", getFormattedDepartmentSums(actsByDepartment));

        // Инициализируем список для отфильтрованных данных
        List<ActByCompanyByDepartment> filteredActs = allActs; // По умолчанию показываем все данные

        // Применяем фильтр компании только если он не пустой
        if (companyFilter != null && !companyFilter.trim().isEmpty()) {
            // Ищем компании, которые содержат введенную подстроку (без учета регистра)
            filteredActs = allActs.stream()
                    .filter(act -> act.getCompany() != null &&
                            act.getCompany().toLowerCase().contains(companyFilter.toLowerCase().trim()))
                    .collect(Collectors.toList());

            // Получаем список уникальных компаний в отфильтрованных данных
            Set<String> uniqueCompanies = filteredActs.stream()
                    .map(ActByCompanyByDepartment::getCompany)
                    .collect(Collectors.toSet());

            // Если найдена ровно одна уникальная компания - показываем сумму
            if (uniqueCompanies.size() == 1) {
                String foundCompany = uniqueCompanies.iterator().next();

                // Суммируем sumActNoNDS только для уникальных актов (по actId)
                double companySum = filteredActs.stream()
                        .filter(distinctByKey(ActByCompanyByDepartment::getActId))
                        .mapToDouble(act -> act.getSumActNoNDS() != null ? act.getSumActNoNDS() : 0.0)
                        .sum();

                model.addAttribute("selectedCompany", foundCompany);
                model.addAttribute("formattedCompanySum", formatCurrency(companySum));

                logger.info("Для компании '{}' найдена сумма: {}", foundCompany, companySum);
            }
        }

        // Добавляем отфильтрованные данные для таблицы
        model.addAttribute("actByCompanyByDepartments", filteredActs);
        model.addAttribute("date1", dateFrom);
        model.addAttribute("date2", dateTo);

        return "fragments/actByDepartmentTable :: actByDepartmentTab";
    }

    // Вспомогательный метод для фильтрации уникальных значений по ключу
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }


    private String formatCurrency(Double value) {
        if (value == null) return "0,00 ₽";

        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(value).replace("\u00A0", " ") + " ₽";
    }

    private Map<String, String> getFormattedDepartmentSums(List<ActByCompanyByDepartment> acts) {
        if (acts == null || acts.isEmpty()) {
            return Collections.emptyMap();
        }

        // Группируем по departmentId, берем первую запись и из нее departmentName и sumByDepartment
        Map<String, Double> departmentSums = acts.stream()
                .collect(Collectors.toMap(
                        ActByCompanyByDepartment::getDepartmentId, // ключ - ID отдела
                        act -> Map.entry(act.getDepartmentName(), act.getSumByDepartment()), // значение - пара (название, сумма)
                        (existing, replacement) -> existing // при дубликатах оставляем существующий
                ))
                .values().stream() // получаем только значения (пары название-сумма)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, // departmentName
                        Map.Entry::getValue // sumByDepartment
                ));

        // Форматируем суммы
        return departmentSums.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> formatCurrency(e.getValue())
                ));
    }



    private Map<String, String> getFormattedCompaniesSums(List<ActByCompanyByDepartment> acts) {
        if (acts == null || acts.isEmpty()) {
            return Collections.emptyMap();
        }

        // Суммируем по отделам
        Map<String, Double> companiesSums = acts.stream()
                .collect(Collectors.toMap(
                        ActByCompanyByDepartment::getCompany,
                        ActByCompanyByDepartment::getSumActNoNDS,
                        Double::sum
                ));

        // Форматируем суммы
        return companiesSums.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> formatCurrency(e.getValue())
                ));
    }

    public List<ActByCompanyByDepartment> groupActsByDepartmentPercentage(List<ActByCompanyByDepartment> allActs) {
        if (allActs == null || allActs.isEmpty()) {
            return Collections.emptyList();
        }

        // Группируем сначала по actId, затем внутри каждого акта по departmentId
        return allActs.stream()
                .collect(Collectors.groupingBy(
                        ActByCompanyByDepartment::getActId,
                        Collectors.groupingBy(ActByCompanyByDepartment::getDepartmentId)
                ))
                .entrySet().stream()
                .flatMap(actEntry ->
                        actEntry.getValue().entrySet().stream()
                                .flatMap(departmentEntry -> {
                                    List<ActByCompanyByDepartment> departmentActs = departmentEntry.getValue();

                                    // Суммируем проценты для данного отдела в данном акте
                                    double totalPercent = departmentActs.stream()
                                            .mapToDouble(ActByCompanyByDepartment::getPercentByDepartment)
                                            .sum();

                                    totalPercent = Math.round(totalPercent * 100.0) / 100.0;

                                    if (Math.abs(totalPercent - 100.0) < 0.01) {
                                        // Если сумма ≈100%, берем первую запись и устанавливаем 100%
                                        ActByCompanyByDepartment representative = departmentActs.get(0);
                                        ActByCompanyByDepartment modified = copyAct(representative);
                                        modified.setPercentByDepartment(100.0);
                                        // Пересчитываем сумму для отдела
                                        modified.setSumByDepartment(representative.getSumActNoNDS());
                                        return Stream.of(modified);
                                    } else {
                                        // Если сумма не 100%, оставляем все записи как есть
                                        return departmentActs.stream();
                                    }
                                })
                )
                .collect(Collectors.toList());
    }

    private ActByCompanyByDepartment copyAct(ActByCompanyByDepartment original) {
        ActByCompanyByDepartment copy = new ActByCompanyByDepartment();
        copy.setNum(original.getNum());
        copy.setActId(original.getActId());
        copy.setCandidate(original.getCandidate());
        copy.setCompany(original.getCompany());
        copy.setProjectName(original.getProjectName());
        copy.setDateAct(original.getDateAct());
        copy.setOrganization(original.getOrganization());
        copy.setPercentByDepartment(original.getPercentByDepartment());
        copy.setSumActNoNDS(original.getSumActNoNDS());
        copy.setDepartmentId(original.getDepartmentId());
        copy.setDepartmentName(original.getDepartmentName());
        copy.setSumByDepartment(original.getSumByDepartment());
        copy.setSumByCompany(original.getSumByCompany());
        return copy;
    }
}
