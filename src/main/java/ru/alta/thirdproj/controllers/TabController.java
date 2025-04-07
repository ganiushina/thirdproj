package ru.alta.thirdproj.controllers;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.alta.thirdproj.entites.MarginBonus;
import ru.alta.thirdproj.entites.MarginBonusBDM;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.entites.UserSalary;
import ru.alta.thirdproj.services.MarginBonusServiceImpl;
import ru.alta.thirdproj.services.UserSalaryServiceImpl;
import ru.alta.thirdproj.services.UserSalesServiceImpl;
import ru.alta.thirdproj.services.UserService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
public class TabController {
    private UserSalaryServiceImpl userSalaryService;
    private MarginBonusServiceImpl marginBonusService;
    private UserSalesServiceImpl userSalesService;
    private UserService userService;
    private LocalDate date3;
    private LocalDate date4 ;

    @Autowired
    public void setUserSalaryService(UserSalaryServiceImpl userSalaryService, MarginBonusServiceImpl marginBonusService,
                                     UserSalesServiceImpl userSalesService, UserService userService){
        this.userSalaryService = userSalaryService;
        this.marginBonusService = marginBonusService;
        this.userSalesService = userSalesService;
        this.userService = userService;
    }


    @GetMapping("/tabs")
    public String showTabs(
//            @RequestParam(required = false, value = "date1")
//                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
//                           @RequestParam(required = false, value = "date2")
//                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2
    ) {
//        date3 = date1;
//        date4 = date2;
        return "tabs"; // Renders the main tabs.html template tabs_2 - остается все по старому

    }

    // Home Tab Content
    @GetMapping("/tabs/home")
    public String getHomeContent(
//            @RequestParam(required = false) String date1,
//            @RequestParam(required = false) String date2,
            @RequestParam(required = false, value = "date1")
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                 @RequestParam(required = false, value = "date2")
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2,

                                 Model model){
//        List<MarginBonus> marginBonuses = userSalesService.getMarginBonusByMonth(date3,date4);
        List<MarginBonus> marginBonuses = userSalesService.getMarginBonusByMonth(date1,date2);

        model.addAttribute("marginBonusByMonth", marginBonuses);
//        model.addAttribute("date1", date3);
//        model.addAttribute("date2", date4);

//        model.addAttribute("homeUsers", homeUsers);
    //    return "fragments/home :: homeTable"; // Return only the homeTable fragment
        return "fragments/month :: monthTable";
    }

    // Profile Tab Content
    @GetMapping("/tabs/profile")
    public String getProfileContent( @RequestParam(required = false, value = "date1")
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                     @RequestParam(required = false, value = "date2")
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2,
                                    Model model, Principal principal) {
        User user = userService.findByUserName(principal.getName());
//        List<MarginBonusBDM> marginBonusList = marginBonusService.getAllMarginBonus(date3,date4);
        List<MarginBonusBDM> marginBonusList = marginBonusService.getAllMarginBonus(date1,date2);
        model.addAttribute("marginBonusList", marginBonusList);
//        model.addAttribute("date1", date3);
//        model.addAttribute("date2", date4);

    //    return "fragments/profile :: profileTable"; // Return only the profileTable fragment
        return "fragments/quarter :: quarterTable";
    }

    @GetMapping("/tabs/salary")
    public String getSalaryContent(Model model, Principal principal) {
        User user = userService.findByUserName(principal.getName());
        List<UserSalary> userSalaryList = marginBonusService.getUserSalary(date3,date4);
        model.addAttribute("userSalaryList", userSalaryList);
//        model.addAttribute("date1", date3);
//        model.addAttribute("date2", date4);

        //    return "fragments/profile :: profileTable"; // Return only the profileTable fragment
        return "fragments/salary :: salaryTable";
    }

}