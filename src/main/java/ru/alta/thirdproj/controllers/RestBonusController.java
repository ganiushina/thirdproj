package ru.alta.thirdproj.controllers;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.alta.thirdproj.entites.Employer;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.entites.UserBonus;
import ru.alta.thirdproj.entites.UserLogin;
import ru.alta.thirdproj.exceptions.UserBonusNotFoundException;
import ru.alta.thirdproj.repositories.UserLoginRepositorySlqO2;
import ru.alta.thirdproj.repositories.UserRepositorySlqO2;
import ru.alta.thirdproj.services.EmployerServiceImpl;
import ru.alta.thirdproj.services.UserBonusServiceImpl;
import ru.alta.thirdproj.specification.UserBonusSpecification;
import ru.alta.thirdproj.specification.UserSpecification;
import ru.alta.thirdproj.specification.UserSpecificationsBuilder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@RestController
@Controller
@CrossOrigin("*")
@RequestMapping("/bonus") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
//@Api("Set of endpoints for CRUD operations for UserBonus")
@Tag(name="RestBonusController", description="Заработанные бонусы")
public class RestBonusController {

    private UserBonusServiceImpl bonusService;

    private EmployerServiceImpl employerService;

    @Autowired
    UserLoginRepositorySlqO2 userR;

    @Autowired
    public RestBonusController(UserBonusServiceImpl bonusService) {
        this.bonusService = bonusService;
    }

    @Autowired
    public void setEmployerService(EmployerServiceImpl employerService){
        this.employerService = employerService;
    }


  //  @GetMapping("/all") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
    @GetMapping
    @ApiOperation("Returns list of all products data transfer objects")
    public ResponseEntity<UserBonus> getAllUserBonus(
                                            @RequestParam(value = "page") Optional<Integer> page,
                                            @RequestParam(value = "date1")
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                            @RequestParam(value = "date2")
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date2
                                           ,@RequestParam(value = "userName", required = false) String userName,
                                            @RequestParam(value = "departmentName", required = false) String departmentName

    ) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserLogin user = userR.getUser(userDetails.getUsername());
        List<UserBonus> userBonuses;
        if (date1 == null) {
            YearMonth month = YearMonth.now();
            userBonuses = bonusService.findAll(month.atDay(1), month.atEndOfMonth(), Math.toIntExact(user.getUserId()), user.getLoginDepartment());
        }
        else {
             userBonuses = bonusService.findAll(date1, date2, Math.toIntExact(user.getUserId()), user.getLoginDepartment());
        }

        if (userName != null || departmentName != null)   {
            List<UserBonus> userBonusesFilter;
            userBonusesFilter =  bonusService.findByFioAndDepartment(userName, departmentName);
            return  new ResponseEntity(userBonusesFilter, HttpStatus.OK);
        }
        else
        return new ResponseEntity(userBonuses, HttpStatus.OK);
    }



    @GetMapping("/all1") //http://localhost:8181/userbonus/all1?date1=2021-12-01&date2=2021-12-31
    // @ApiOperation("Returns list of all products data transfer objects")
    public String getAllUserBonus(Model model,
                                  @RequestParam(value = "date1")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                  @RequestParam(value = "date2")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2,
                                  @RequestParam(value = "userName", required = false) String userName,
                                  @RequestParam(value = "departmentName", required = false) String departmentName

    ) {
        List<Employer> employers = employerService.getAll();
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserLogin user = userR.getUser(userDetails.getUsername());
        List<UserBonus> userBonuses;
        if (date1 == null || date1.equals("")) {
            YearMonth month = YearMonth.now();
            userBonuses = bonusService.findAll(month.atDay(1), month.atEndOfMonth(), Math.toIntExact(user.getUserId()), user.getLoginDepartment());
        }
        else {
            userBonuses = bonusService.findAll(date1, date2, Math.toIntExact(user.getUserId()), user.getLoginDepartment());
        }

        if (!userName.equals("") || !departmentName.equals("")) {
            List<UserBonus> userBonusesFilter = new ArrayList<>();
            userBonusesFilter = bonusService.findByFioAndDepartment(userName, departmentName);
            model.addAttribute("userBonus", userBonusesFilter);
        }   else
        model.addAttribute("userBonus", userBonuses);
        model.addAttribute("employers", employers);
        return "bonus";
    }

    @GetMapping("/all2") //http://localhost:8181/userbonus/all1?date1=2021-12-01&date2=2021-12-31
    // @ApiOperation("Returns list of all products data transfer objects")
    public String showUserBonus(Model model) {
        List<Employer> employers = employerService.getAll();
        model.addAttribute("employers", employers);
        return "bonusShow";
    }




    @ExceptionHandler
    public ResponseEntity<?> handleException(UserBonusNotFoundException exc) {
        return new ResponseEntity<>(exc.getMessage(), HttpStatus.NOT_FOUND);
    }
}