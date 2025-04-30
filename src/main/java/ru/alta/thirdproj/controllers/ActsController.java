package ru.alta.thirdproj.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.MoneyByFinalist;
import ru.alta.thirdproj.services.ActPutServiceImpl;
import ru.alta.thirdproj.services.ExpectedMoneyByFinalistService;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ActsController {

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
}
