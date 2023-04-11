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
import ru.alta.thirdproj.services.ActPutServiceImpl;

import java.security.Principal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

@Controller
@CrossOrigin("*")
@RequestMapping("/actPut") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
@Tag(name="ActPutController", description="Дополнительные бонусы по актам")
public class ActPutController {

    private ActPutServiceImpl actBonusPercentService;


    @Autowired
    public void setActBonusPercentService(ActPutServiceImpl actBonusPercentService){
        this.actBonusPercentService = actBonusPercentService;
    }

    @GetMapping("/actAll") // http://localhost:8189/userbonus/actPut/allact?date1=2022-06-01&date2=2022-06-27
//    @ApiOperation("Returns list of all products data transfer objects")
    public String showAll(Model model,
                           Principal principal,
                           @RequestParam(value = "date1")
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                           @RequestParam(value = "date2")
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2)  {
       // User user = userService.findByUserName(principal.getName());


        List<Act> actList = actBonusPercentService.getAllPutAct(date1,date2);
        double allActMoneyPeriod = 0;
        double allActMoneyPeriodPaid = 0;
        for (int i = 0; i < actList.size() ; i++) {
            if (!actList.get(i).isPaid()) {
                if (actList.get(i).getBonus() != null) {
                    allActMoneyPeriod += actList.get(i).getBonus();
                }
            }
            else {
                allActMoneyPeriodPaid += actList.get(i).getBonus();
            }
        }
        List<Act> actNoPayList = actBonusPercentService.getANoPaymentAct();
        double allActMoney =0;
        for (int i = 0; i < actNoPayList.size() ; i++) {
            if (actNoPayList.get(i).getBonus() != null) {
                allActMoney += actNoPayList.get(i).getBonus();

            }
        }
        final NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();
        currencyInstance.setCurrency(Currency.getInstance("RUB"));

        model.addAttribute("actNoPayList", actNoPayList);
        model.addAttribute("actPutList", actList);
        model.addAttribute("actNoPayList", actNoPayList);
        model.addAttribute("allActMoney", currencyInstance.format(allActMoney));
        model.addAttribute("allActMoneyPeriod", currencyInstance.format(allActMoneyPeriod));
        model.addAttribute("allActMoneyPeriodPaid", currencyInstance.format(allActMoneyPeriodPaid));
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);

    //    return "newTest";
     //   return "TestAct";
        return "bonusAct2";
    }



}
