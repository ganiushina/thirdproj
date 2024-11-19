package ru.alta.thirdproj.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.alta.thirdproj.entites.*;
import ru.alta.thirdproj.services.MarginBonusServiceImpl;
import ru.alta.thirdproj.services.UserSalaryServiceImpl;
import ru.alta.thirdproj.services.UserSalesServiceImpl;

import java.security.Principal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Controller
@CrossOrigin("*")
@RequestMapping("/salary") //http://localhost:8181/userbonus/api/v1/userbonus/all?date1=2021-12-01&date2=2021-12-31&userId=-1&departmentId=-1
@Api("Set of endpoints for CRUD operations for User")
public class UserSalaryController {
    private UserSalaryServiceImpl userSalaryService;
    private MarginBonusServiceImpl marginBonusService;

    private UserSalesServiceImpl userSalesService;

    @Autowired
    public void setUserSalaryService(UserSalaryServiceImpl userSalaryService, MarginBonusServiceImpl marginBonusService, UserSalesServiceImpl userSalesService){
        this.userSalaryService = userSalaryService;
        this.marginBonusService = marginBonusService;
        this.userSalesService = userSalesService;
    }

    @GetMapping("/getSalaryAll") // http://192.168.0.240:8181/userbonus/salary/getSalaryAll?date1=2024-06-01&date2=2024-06-30
//    @ApiOperation("Returns list of all products data transfer objects")
    public String showSalaryAll(Model model,
                          Principal principal,
                          @RequestParam(value = "date1")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                          @RequestParam(value = "date2")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2
                 //         ,@RequestParam(value = "qrt") String qrt
    ) {
        Locale ru = new Locale("ru", "RU");
        NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);

//        List<UserSalary> userSalaryList = userSalaryService.getUserSalaryList(date1,date2);
//        List<MarginBonusBDM> marginBonusList = marginBonusService.getAllMarginBonus(date1,date2);
//        List<DepartmentUserSales> userSaleList = userSalesService.getUserSalesList(date1,date2);
        List<MarginBonus> marginBonuses = userSalesService.getMarginBonusByMonth(date1,date2);

//        model.addAttribute("marginBonusList", marginBonusList);
//        model.addAttribute("userSalaryList", userSalaryList);
//        model.addAttribute("userSaleList", userSaleList);
        model.addAttribute("marginBonusByMonth", marginBonuses);

        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        return "salary1";

    }

    @GetMapping("/getMarginAll") // http://localhost:8189/userbonus/actPut/allact?date1=2022-06-01&date2=2022-06-27
//    @ApiOperation("Returns list of all products data transfer objects")
    public String showMarginAll(Model model,
                          Principal principal,
                          @RequestParam(value = "date1")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                          @RequestParam(value = "date2")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2) {
        Locale ru = new Locale("ru", "RU");
        NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);

        List<MarginBonusBDM> marginBonusList = marginBonusService.getAllMarginBonus(date1,date2);
        model.addAttribute("marginBonusList", marginBonusList);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        return "salary1";

    }

}
