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
import java.time.LocalDate;
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
        model.addAttribute("actPutList", actList);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);

        return "newTest";
    }




}
