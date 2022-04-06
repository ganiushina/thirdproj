package ru.alta.thirdproj.controllers;


import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.alta.thirdproj.entites.Employer;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.entites.UserBonus;
import ru.alta.thirdproj.exceptions.UserBonusNotFoundException;
import ru.alta.thirdproj.services.EmployerServiceImpl;
import ru.alta.thirdproj.services.UserBonusServiceImpl;
import ru.alta.thirdproj.services.UserService;

import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

//@RestController
@Controller
@CrossOrigin("*")
@RequestMapping("/bonus") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
//@Api("Set of endpoints for CRUD operations for UserBonus")
@Tag(name="RestBonusController", description="Заработанные бонусы")
public class RestBonusController {

    private UserBonusServiceImpl bonusService;

    private EmployerServiceImpl employerService;

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public RestBonusController(UserBonusServiceImpl bonusService) {
        this.bonusService = bonusService;
    }

    @Autowired
    public void setEmployerService(EmployerServiceImpl employerService){
        this.employerService = employerService;
    }


    @GetMapping("/all") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
  //  @GetMapping
    @ApiOperation("Returns list of all products data transfer objects")
    public ResponseEntity<UserBonus> getAllUserBonus(
                                            Principal principal,
                                            @RequestParam(value = "date1")
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                            @RequestParam(value = "date2")
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date2
                                           ,@RequestParam(value = "userName", required = false) String userName,
                                            @RequestParam(value = "departmentName", required = false) String departmentName

    ) {

        User user = userService.findByUserName(principal.getName());
        List<HashMap<String, Object>> userBonuses;
       // List<UserBonus> userBonuses;
        if (date1 == null) {
            YearMonth month = YearMonth.now();
            userBonuses = bonusService.findAll(month.atDay(1), month.atEndOfMonth(), Math.toIntExact(user.getUserId()), user.getLoginDepartment());
        }
        else {
             userBonuses = bonusService.findAll(date1, date2, Math.toIntExact(user.getUserId()), user.getLoginDepartment());
        }

//        if (userName != null || departmentName != null)   {
//            List<UserBonus> userBonusesFilter;
//            userBonusesFilter =  bonusService.findByFioAndDepartment(userName, departmentName);
//            return new ResponseEntity(userBonusesFilter, HttpStatus.OK);
//        }
//        else
        return new ResponseEntity(userBonuses, HttpStatus.OK);

    }

    @GetMapping("/all3") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
   // @GetMapping
    @ApiOperation("Returns list of all products data transfer objects")
  //  @RequestMapping(value = "getAllUserBonusGson", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllUserBonusGson(
            Principal principal,
            @RequestParam(value = "page") Optional<Integer> page,
            @RequestParam(value = "date1")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
            @RequestParam(value = "date2")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date2,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "departmentName", required = false) String departmentName

    ) {

        User user = userService.findByUserName(principal.getName());
        List<HashMap<String, Object>> entities;

//        if (!userName.equals("") || !departmentName.equals(""))   {
//            entities =  bonusService.findByFioAndDepartment(userName, departmentName);
//        }
//        else
//        {

            entities = bonusService.findAll(date1, date2, Math.toIntExact(user.getUserId()), user.getLoginDepartment());
     //   }
//        HashMap<String,Object> mapMoney = bonusService.getMapMoney();
//        HashMap<String,Object> mapSum = bonusService.getMapSum();
//        HashMap<String,Object> mapCandidate = bonusService.getMapCandidate();
//        HashMap<String,Object> mapCompany = bonusService.getMapCompany();
//        List<String> employers = bonusService.getEmployers();
//        List<String> department = bonusService.getDepartment();

         return new ResponseEntity<>(entities, HttpStatus.OK);
//

    }

    @GetMapping("/all1") //http://localhost:8181/userbonus/all1?date1=2021-12-01&date2=2021-12-31
    // @ApiOperation("Returns list of all products data transfer objects")
    public String getAllUserBonus(Model model,
                                  Principal principal,
                                  @RequestParam(value = "date1")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                  @RequestParam(value = "date2")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2,
                                  @RequestParam(value = "userName", required = false) String userName,
                                  @RequestParam(value = "departmentName", required = false) String departmentName

    ) {
        User user = userService.findByUserName(principal.getName());
        List<HashMap<String, Object>> entities;

        if (!userName.equals("") || !departmentName.equals(""))   {
            entities =  bonusService.findByFioAndDepartment(userName, departmentName);
        }
        else
            {

                entities = bonusService.findAll(date1, date2, Math.toIntExact(user.getUserId()), user.getLoginDepartment());
        }
        HashMap<String,Object> mapMoney = bonusService.getMapMoney();
        HashMap<String,Object> mapSum = bonusService.getMapSum();
        HashMap<String,Object> mapCandidate = bonusService.getMapCandidate();
        HashMap<String,Object> mapCompany = bonusService.getMapCompany();
        List<String> employers = bonusService.getEmployers();
        List<String> department = bonusService.getDepartment();
        model.addAttribute("userBonus", entities);
        model.addAttribute("moneyByCandidate", mapMoney);
        model.addAttribute("sumUser", mapSum);
        model.addAttribute("candidateName", mapCandidate);
        model.addAttribute("companyName", mapCompany);
        model.addAttribute("employers", employers);
        model.addAttribute("department", department);
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