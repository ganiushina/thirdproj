package ru.alta.thirdproj.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.alta.thirdproj.entites.*;
import ru.alta.thirdproj.services.MarginBonusServiceImpl;
import ru.alta.thirdproj.services.UserSalaryServiceImpl;
import ru.alta.thirdproj.services.UserSalesServiceImpl;
import ru.alta.thirdproj.services.UserService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class MarginController {
    private UserSalaryServiceImpl userSalaryService;
    private MarginBonusServiceImpl marginBonusService;
    private UserSalesServiceImpl userSalesService;
    private UserService userService;

    @Autowired
    public void setUserSalaryService(UserSalaryServiceImpl userSalaryService, MarginBonusServiceImpl marginBonusService,
                                     UserSalesServiceImpl userSalesService, UserService userService){
        this.userSalaryService = userSalaryService;
        this.marginBonusService = marginBonusService;
        this.userSalesService = userSalesService;
        this.userService = userService;
    }


    @GetMapping("/margin")
    public String showTabs() {
        return "margin"; // Renders the main tabs.html template tabs_2 - остается все по старому
    }

    // Shared date range processing method
    private void processDateRange(LocalDate dateFrom, LocalDate dateTo, Model model) {
        if (dateFrom == null) dateFrom = LocalDate.now().minusDays(7);
        if (dateTo == null) dateTo = LocalDate.now();

        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
    }

    @GetMapping("/margin/summary")
    public String getSummaryReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            Model model) {

        processDateRange(dateFrom, dateTo, model);
        List<MarginBonus> marginBonuses = userSalesService.getMarginBonusByMonth(dateFrom,dateTo);

        model.addAttribute("marginBonusByMonth", marginBonuses);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        return "summary :: summaryTab"; // Fragment for AJAX
    }

    @GetMapping("/margin/detailed")
    public String getDetailedReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            Model model) {

        processDateRange(dateFrom, dateTo, model);
        List<MarginBonusBDM> marginBonusList = marginBonusService.getAllMarginBonus(dateFrom,dateTo);
        model.addAttribute("marginBonusList", marginBonusList);
        return "details :: detailsTab"; // Fragment for AJAX
    }

    @GetMapping("/margin/charts")
    public String getChartData(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            Principal principal, Model model) {

        processDateRange(dateFrom, dateTo, model);
        User user = userService.findByUserName(principal.getName());

        //user.getRoles()
        List<UserSalary> userSalaryList = marginBonusService.getUserSalary(dateFrom,dateTo, user.getLoginDepartment());
        model.addAttribute("userSalaryList", userSalaryList);
        return "charts :: chartsTab"; // Fragment for AJAX
    }

    @GetMapping("/margin/interpreters")
    public String getInterpreterData(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            Model model) {
        processDateRange(dateFrom, dateTo, model);
        List<UserSalaryDetail> userSalaryDetailList = marginBonusService.getUserSalaryInterpreter(dateFrom,dateTo);
        model.addAttribute("userSalaryList", userSalaryDetailList);
        return "interpreter :: interpreterTab"; // Fragment for AJAX
    }


}