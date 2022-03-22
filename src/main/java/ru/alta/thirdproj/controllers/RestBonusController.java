package ru.alta.thirdproj.controllers;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import ru.alta.thirdproj.response.ResponseHandler;
import ru.alta.thirdproj.services.EmployerServiceImpl;
import ru.alta.thirdproj.services.UserBonusServiceImpl;
import ru.alta.thirdproj.specification.UserBonusSpecification;
import ru.alta.thirdproj.specification.UserSpecification;
import ru.alta.thirdproj.specification.UserSpecificationsBuilder;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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


    @GetMapping("/all") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
  //  @GetMapping
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
            @RequestParam(value = "page") Optional<Integer> page,
            @RequestParam(value = "date1")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
            @RequestParam(value = "date2")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date2,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "departmentName", required = false) String departmentName

    ) {

//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        UserLogin user = userR.getUser(userDetails.getUsername());
//        List<UserBonus> userBonuses;
//
//        if (userName != null || departmentName != null)   {
//            userBonuses =  bonusService.findByFioAndDepartment(userName, departmentName);
//        }
//        else
//            userBonuses = bonusService.findAll(date1, date2, Math.toIntExact(user.getUserId()), user.getLoginDepartment());
//
//
        List<HashMap<String, Object>> entities = new ArrayList<>();
//
//        int i = 0;
//
//        for (UserBonus n : userBonuses) {
//
//            HashMap<String,Object> map = new HashMap<>();
//            ArrayList<Double> moneyByCandidate = new ArrayList<>();
//            ArrayList<Double> userSumList = new ArrayList<>();
//            ArrayList<String> candidateName = new ArrayList<>();
//            ArrayList<String> companyName = new ArrayList<>();
//            map.put("fio", n.getFio());
//            map.put("pos_name", n.getPosition());
//            map.put("department", n.getDepartment());
//
//            map.put("moneyAll", n.getMoneyAll());
//
//            map.put("summ_total", n.getSumTotal());
//            map.put("mon", n.getMonth());
//            map.put("ya", n.getYear());
//
//            if (entities.isEmpty()){
//                for (int j = 0; j < userBonuses.size() ; j++) {
//                    if (userBonuses.get(j).getFio().equals(n.getFio())){
//                        moneyByCandidate.add(userBonuses.get(j).getMoneyByCandidate()) ;
//                        userSumList.add(userBonuses.get(j).getSumUser());
//                        candidateName.add(userBonuses.get(j).getCandidateName());
//                        companyName.add(userBonuses.get(j).getCompanyName());
//                    }
//                }
//                map.put("moneyByCandidate", moneyByCandidate);
//                map.put("sumUser", userSumList);
//                map.put("candidateName", candidateName);
//                map.put("companyName", companyName);
//
//                entities.add(map);
//            } else
//
//            if (!entities.get(i).get("fio").equals(n.getFio())) {
//
//                for (int j = 0; j < userBonuses.size() ; j++) {
//                    if (userBonuses.get(j).getFio().equals(n.getFio())){
//                        moneyByCandidate.add(userBonuses.get(j).getMoneyByCandidate()) ;
//                        userSumList.add(userBonuses.get(j).getSumUser());
//                        candidateName.add(userBonuses.get(j).getCandidateName());
//                        companyName.add(userBonuses.get(j).getCompanyName());
//                    }
//                }
//                map.put("moneyByCandidate", moneyByCandidate);
//                map.put("sumUser", userSumList);
//                map.put("candidateName", candidateName);
//                map.put("companyName", companyName);
//
//                entities.add(map);
//                i++;
//
//            }
//
//        }
         return new ResponseEntity<>(entities, HttpStatus.OK);
//

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
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserLogin user = userR.getUser(userDetails.getUsername());
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