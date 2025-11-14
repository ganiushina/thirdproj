package ru.alta.thirdproj.controllers;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.MoneyByFinalist;
import ru.alta.thirdproj.services.ActPutServiceImpl;
import ru.alta.thirdproj.services.ExpectedMoneyByFinalistService;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@CrossOrigin("*")
@RequestMapping("/actPut")
@Tag(name="ActPutController", description="Управление актами")
public class ActPutController {

    private ActPutServiceImpl actBonusPercentService;
    private ExpectedMoneyByFinalistService moneyByFinalistService;

    @Autowired
    public void setActBonusPercentService(ActPutServiceImpl actBonusPercentService){
        this.actBonusPercentService = actBonusPercentService;
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
                .filter(e -> e.getPaid() == 0 || e.getPaid() == 2) // неоплаченные + частично оплаченные
                .mapToDouble(Act::getBonus)
                .sum();

//        double allActMoneyPeriod = actList.stream()
//                .filter(e -> !e.isPaid())
//                .mapToDouble(Act::getBonus)
//                .sum();

        double allActForClientMoneyPeriod = actList.stream()
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
                .collect(Collectors.groupingBy(Act::getOrganization,
                        Collectors.summingDouble(Act::getBonus)));

        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, Double> item : allActForClientMoneyPeriodByCompany.entrySet()) {
            sb.append(" ").append(item.getKey()).append(" - ").append(currencyInstance.format(item.getValue()));
        }
        String allActForClientMoneyPeriodByCompanyString = sb.toString();

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

    @GetMapping("/actAll")
    public String showAll(Model model,
                          @RequestParam(value = "date1") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                          @RequestParam(value = "date2") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2) {
        prepareCommonModel(model, date1, date2);
        return "fragments/actPeriodTable :: actPeriodTable";
    }

    @GetMapping("/unpaidActs")
    public String showUnpaidActs(Model model,
                                 @RequestParam(value = "date1") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                 @RequestParam(value = "date2") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2) {
        prepareCommonModel(model, date1, date2);
        return "fragments/actUnpaidTable :: actUnpaidTable";
    }

    @GetMapping("/expectedExits")
    public String showExpectedExits(Model model,
                                    @RequestParam(value = "date1") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                    @RequestParam(value = "date2") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2) {
        prepareCommonModel(model, date1, date2);
        return "fragments/actExpectedTable :: actExpectedTable";
    }
}

//package ru.alta.thirdproj.controllers;
//
//
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import ru.alta.thirdproj.entites.Act;
//import ru.alta.thirdproj.entites.MoneyByFinalist;
//import ru.alta.thirdproj.services.ActPutServiceImpl;
//import ru.alta.thirdproj.services.ExpectedMoneyByFinalistService;
//
//import java.math.BigDecimal;
//import java.security.Principal;
//import java.text.NumberFormat;
//import java.time.LocalDate;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static org.codehaus.groovy.vmplugin.v8.PluginDefaultGroovyMethods.mapToDouble;
//
//@Controller
//@CrossOrigin("*")
//@RequestMapping("/actPut") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
//@Tag(name="ActPutController", description="Дополнительные бонусы по актам")
//public class ActPutController {
//
//    private ActPutServiceImpl actBonusPercentService;
//
//
//    @Autowired
//    public void setActBonusPercentService(ActPutServiceImpl actBonusPercentService){
//        this.actBonusPercentService = actBonusPercentService;
//    }
//    @Autowired
//    ExpectedMoneyByFinalistService moneyByFinalistService;
//
//
//    @GetMapping("/actAll") // http://localhost:8189/userbonus/actPut/allact?date1=2022-06-01&date2=2022-06-27
////    @ApiOperation("Returns list of all products data transfer objects")
//    public String showAll(Model model,
//                           Principal principal,
//                           @RequestParam(value = "date1")
//                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
//                           @RequestParam(value = "date2")
//                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2)  {
//       // User user = userService.findByUserName(principal.getName());
//
//        Locale ru = new Locale("ru", "RU");
//        NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);
//        String bestUser = "";
//        String bestUser_m = "";
//        String bestUser_s = "";
//
//        List<Act> actList = (actBonusPercentService.getAllPutAct(date1,date2)).
//                stream().sorted(Comparator.comparingInt(Act::getId)).collect(Collectors.toList());
//        Optional<Act> actList1 = null;
//        double allActMoneyPeriod = 0;
//        double allActMoneyPeriodPaid = 0;
//        double allActForClientMoneyPeriod = 0;
//        Map<String, Double> allActForClientMoneyPeriodByCompany =
//        actList.stream()
//                .filter(e->e.getDateAct().isBefore(date2.plusDays(1)))
//                .filter(e->e.getDateAct().isAfter(date1))
//                .collect(
//                Collectors.groupingBy(Act::getOrganization, Collectors.summingDouble(Act::getBonus)));
//
//        StringBuilder sb = new StringBuilder();
//
//        for(Map.Entry<String, Double> item : allActForClientMoneyPeriodByCompany.entrySet()){
//
//            sb.append(" " + item.getKey() + " - " + currencyInstance.format(item.getValue()));
//        }
//
//        String allActForClientMoneyPeriodByCompanyString = sb.toString();
//
//        boolean stop = false;
//        boolean stop_m = false;
//        boolean stop_s = false;
//
//        for (int i = 0; i < actList.size() ; i++) {
//            if (actList.get(i).getDateAct().isAfter(date1) && actList.get(i).getDateAct().isBefore(date2)) {
//                if (!actList.get(i).isPaid()) {
//                    allActMoneyPeriod += actList.get(i).getBonus();
//                }
//                allActForClientMoneyPeriod += actList.get(i).getBonus();
//            }
//
//            if (actList.get(i).getPaymentDate() != null) {
//                if (actList.get(i).getPaymentDate().isAfter(date1) && actList.get(i).getPaymentDate().isBefore(date2)) {
//                    allActMoneyPeriodPaid += actList.get(i).getBonus();
//                }
//            }
//        }
//
//        List<Act> actNoPayList = actBonusPercentService.getANoPaymentAct(date1);
//        double allActMoney =0;
//        for (int i = 0; i < actNoPayList.size() ; i++) {
//                allActMoney += actNoPayList.get(i).getBonus();
//        }
//
//        List<MoneyByFinalist> moneyByFinalists = moneyByFinalistService.getMoneyByFinalistList();
//
//        double allFinalistMoneyPeriodPaid = 0;
//
//         for (int i = 0; i < moneyByFinalists.size() ; i++) {
//            if (moneyByFinalists.get(i).getProjectFee() != null) {
//                allFinalistMoneyPeriodPaid += moneyByFinalists.get(i).getProjectFee().doubleValue();
//            }
//        }
//
//
//        model.addAttribute("actNoPayList", actNoPayList);
//        model.addAttribute("actPutList", actList);
//        model.addAttribute("actNoPayList", actNoPayList);
//        model.addAttribute("allActMoney", currencyInstance.format(allActMoney));
//        model.addAttribute("allActMoneyPeriod", currencyInstance.format(allActMoneyPeriod));
//        model.addAttribute("allActMoneyPeriodPaid", currencyInstance.format(allActMoneyPeriodPaid));
//        model.addAttribute("allActForClientMoneyPeriod", currencyInstance.format(allActForClientMoneyPeriod));
//        model.addAttribute("moneyByFinalists", moneyByFinalists);
//        model.addAttribute("allFinalistMoneyPeriodPaid", currencyInstance.format(allFinalistMoneyPeriodPaid));
//        model.addAttribute("allActForClientMoneyPeriodByCompanyString", allActForClientMoneyPeriodByCompanyString);
//        model.addAttribute("date1", date1);
//        model.addAttribute("date2", date2);
//        return "bonusAct2";
//    }
//
//
//
//}
