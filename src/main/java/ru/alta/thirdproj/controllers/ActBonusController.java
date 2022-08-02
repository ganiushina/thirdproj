package ru.alta.thirdproj.controllers;


import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.Employer;
import ru.alta.thirdproj.entites.EmployerNew;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.services.ActBonusPercentServiceImpl;
import ru.alta.thirdproj.services.EmployerServiceImpl;
import ru.alta.thirdproj.services.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Controller
@CrossOrigin("*")
@RequestMapping("/act") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
@Tag(name="ActBonusController", description="Дополнительные бонусы по актам")
public class ActBonusController {

    private ActBonusPercentServiceImpl actBonusPercentService;
    private EmployerServiceImpl employerService;
    private UserService userService;

  //  private String request;

    @Autowired
    public void setActBonusPercentService(ActBonusPercentServiceImpl actBonusPercentService){
        this.actBonusPercentService = actBonusPercentService;
    }

    @Autowired
    public void setEmployerService(EmployerServiceImpl employerService) {
        this.employerService = employerService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/allact") // http://localhost:8189/userbonus/act/allact?date1=2022-06-01&date2=2022-06-27
//    @ApiOperation("Returns list of all products data transfer objects")
    public String showAll3(Model model, @RequestParam(value = "act", required = false) String act, String actList,
                           String percent, Principal principal, String request, String fio,
                            LocalDate date1,
                            LocalDate date2)  {
        User user = userService.findByUserName(principal.getName());
        Employer employer = employerService.findByUserName(fio);

        final String[] items1 = actList.split("Act");

        List<String> list1 = new ArrayList<>(Arrays.asList(items1));
        list1.removeAll(Arrays.asList("[", null));

        List<String> list3 = new ArrayList<>();

        for (int i = 0; i < list1.size() ; i++) {
            if (list1.get(i).contains("paid=true")){
                list3.add(list1.get(i).substring(list1.get(i).indexOf("id")+3, list1.get(i).indexOf("date")-2));
            }
        }

        final String[] items = percent.split(",");

        List<String> list = new ArrayList<>(Arrays.asList(items));
        list.removeAll(Arrays.asList("0.0", null));
        list.removeAll(Arrays.asList("0", null));
        list.removeAll(Arrays.asList("", null));

        if (act != null) {
            List<String> actIds = Arrays.asList(act.split(","));
            list3.removeAll(actIds);
            actBonusPercentService.saveActBonus(employer.getManId(), user.getUserId(), list, act);
        }

        if (list3.size() >0)
            actBonusPercentService.deleteActBonus(employer.getManId(), list3);


        return "redirect:" + request;

    }




}
