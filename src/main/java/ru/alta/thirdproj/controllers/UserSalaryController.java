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
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.UserSalary;
import ru.alta.thirdproj.services.UserSalaryServiceImpl;

import java.security.Principal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Controller
@CrossOrigin("*")
@RequestMapping("/api/v1/user") //http://localhost:8181/userbonus/api/v1/userbonus/all?date1=2021-12-01&date2=2021-12-31&userId=-1&departmentId=-1
@Api("Set of endpoints for CRUD operations for User")
public class UserSalaryController {
    private UserSalaryServiceImpl userSalaryService;

    @Autowired
    public void setUserSalaryService(UserSalaryServiceImpl userSalaryService){
        this.userSalaryService = userSalaryService;
    }

    @GetMapping("/salary") // http://localhost:8189/userbonus/actPut/allact?date1=2022-06-01&date2=2022-06-27
//    @ApiOperation("Returns list of all products data transfer objects")
    public String showAll(Model model,
                          Principal principal,
                          @RequestParam(value = "date1")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                          @RequestParam(value = "date2")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2) {
        Locale ru = new Locale("ru", "RU");
        NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);

        List<UserSalary> userSalaryList = userSalaryService.getUserSalaryList(date1,date2);
        model.addAttribute("userSalaryList", userSalaryList);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        return "salary";

    }

    }
