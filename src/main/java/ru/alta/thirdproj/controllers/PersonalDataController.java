package ru.alta.thirdproj.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.alta.thirdproj.entites.PersonalData;
import ru.alta.thirdproj.services.PersonalDataServiceImpl;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/personal-data")
public class PersonalDataController {

    private final PersonalDataServiceImpl personalDataService;

    public PersonalDataController(PersonalDataServiceImpl personalDataService) {
        this.personalDataService = personalDataService;
    }

    @GetMapping
    public String getPersonalDataPage(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date1,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date2,
            Model model) {

        if (date1 == null) {
            date1 = LocalDate.now().withDayOfMonth(1);
        }
        if (date2 == null) {
            date2 = LocalDate.now();
        }

        List<PersonalData> personalDataList = personalDataService.getPersonalData(date1, date2);

        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        model.addAttribute("personalDataList", personalDataList);

        return "personaldate";
    }
}
