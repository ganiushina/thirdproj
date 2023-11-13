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
import ru.alta.thirdproj.entites.*;
import ru.alta.thirdproj.exceptions.UserBonusNotFoundException;
import ru.alta.thirdproj.export.ExcelGenerator;
import ru.alta.thirdproj.services.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

//@RestController
@Controller
@CrossOrigin("*")
@RequestMapping("/bonus") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
//@Api("Set of endpoints for CRUD operations for UserBonus")
@Tag(name="RestBonusController", description="Заработанные бонусы")
public class BonusController {

    private UserBonusServiceImpl bonusService;
    private EmployerServiceImpl employerService;
    private UserService userService;
    private ActBonusPercentServiceImpl actBonusPercentService;
    private UserBonusKPIServiceImpl bonusKPIService;
    private List<UserBonusKPI> bonusKPIList;
    private List<UserBonusNew> userBonusNewList;
    private LocalDate dateS;
    private LocalDate dateF;
    private List<List<Object>> objectList;
    List<HashMap<String, Object>> entitiesTest;


    @Autowired
    public void setActBonusPercentService(ActBonusPercentServiceImpl actBonusPercentService) {
        this.actBonusPercentService = actBonusPercentService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public BonusController(UserBonusServiceImpl bonusService) {
        this.bonusService = bonusService;
    }

    @Autowired
    public void setEmployerService(EmployerServiceImpl employerService) {
        this.employerService = employerService;
    }

    @Autowired
    public void setBonusKPIService(UserBonusKPIServiceImpl bonusKPIService){
        this.bonusKPIService = bonusKPIService;
    }


    @GetMapping("/all") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
    //  @GetMapping
    @ApiOperation("Returns list of all products data transfer objects")
    public ResponseEntity<UserBonus> getAllUserBonus(
            Principal principal,
            @RequestParam(value = "date1")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
            @RequestParam(value = "date2")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2
            , @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "departmentName", required = false) String departmentName

    ) {

        User user = userService.findByUserName(principal.getName());
        List<HashMap<String, Object>> userBonuses;
        // List<UserBonus> userBonuses;
        if (date1 == null) {
            YearMonth month = YearMonth.now();
            userBonuses = bonusService.findAll(month.atDay(1), month.atEndOfMonth(), Math.toIntExact(user.getUserId()), user.getLoginDepartment());
        } else {
            userBonuses = bonusService.findAll(date1, date2, Math.toIntExact(user.getUserId()), user.getLoginDepartment());
        }

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
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "departmentName", required = false) String departmentName

    ) {

        User user = userService.findByUserName(principal.getName());
        List<HashMap<String, Object>> entities;


        entities = bonusService.findAll(date1, date2, Math.toIntExact(user.getUserId()), user.getLoginDepartment());

        return new ResponseEntity<>(entities, HttpStatus.OK);


    }

    @GetMapping("/all1") //http://localhost:8181/userbonus/all1?date1=2021-12-01&date2=2021-12-31
    // @ApiOperation("Returns list of all products data transfer objects")
    public String getAllUserBonus(Model model,
                                  Principal principal,
                                  @RequestParam(value = "date1")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                  @RequestParam(value = "date2")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2

    ) {
        User user = userService.findByUserName(principal.getName());
        List<HashMap<String, Object>> entities;

        entities = bonusService.findAll(date1, date2, Math.toIntExact(user.getUserId()), user.getLoginDepartment());

        HashMap<String, Object> mapMoney = bonusService.getMapMoney();
        HashMap<String, Object> mapSum = bonusService.getMapSum();
        HashMap<String, Object> mapCandidate = bonusService.getMapCandidate();
        HashMap<String, Object> mapCompany = bonusService.getMapCompany();

        List<String> employers = bonusService.getEmployers();
        List<String> department = bonusService.getDepartment();

        double allMoney = bonusService.getAllMoney(mapMoney);



        model.addAttribute("userBonus", entities);
        model.addAttribute("moneyByCandidate", mapMoney);
        model.addAttribute("sumUser", mapSum);
        model.addAttribute("candidateName", mapCandidate);
        model.addAttribute("companyName", mapCompany);
        model.addAttribute("employers", employers);
        model.addAttribute("department", department);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        model.addAttribute("allMoney", allMoney);
        return "bonus"; //getUserBonusList
    }

    @GetMapping("/getall") //http://localhost:8181/userbonus/all1?date1=2021-12-01&date2=2021-12-31
    // @ApiOperation("Returns list of all products data transfer objects")
    public String getAllBonuses(Model model,
                                  Principal principal,
                                  @RequestParam(value = "date1")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                  @RequestParam(value = "date2")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2

    ) {

        userBonusNewList =  bonusService.getUserBonusList(date1, date2);
        Double allBonusMoney = Double.valueOf(0);;
        dateS = date1;
        dateF = date2;


        for (int i = 0; i < userBonusNewList.size() ; i++) {
            if (userBonusNewList.get(i).getMoneyByCandidate() != null) {
                for (int j = 0; j < userBonusNewList.get(i).getMoneyByCandidate().size(); j++) {
                    allBonusMoney += userBonusNewList.get(i).getMoneyByCandidate().get(j);
                }
            }
        }

        List<UserBonusKPI> bonusKPIList = bonusKPIService.getUserBonusKPIList(date1, date2);

        Locale ru = new Locale("ru", "RU");
        Currency rub = Currency.getInstance(ru);
        NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(ru);

        Double allMoney = Double.valueOf(0);
        allMoney = bonusService.getCompanyMoney(date1, date2);


        Double percentWithoutPKI = Double.valueOf(0);
        if (allMoney != null) {
            percentWithoutPKI = allBonusMoney * 100 / allMoney;
        }

        bonusKPIList = bonusKPIService.getUserBonusKPIList(date1, date2);

        double allKPIMoney = 0;

        for (int i = 0; i < bonusKPIList.size() ; i++) {
            for (int j = 0; j < bonusKPIList.get(i).getBonusAll().size(); j++) {
                allKPIMoney += bonusKPIList.get(i).getBonusAll().get(j);
            }
        }
        Double percentWithPKI = Double.valueOf(0);
        if (allMoney != null) {
         percentWithPKI = (allBonusMoney + allKPIMoney) * 100 / allMoney;
        }

        DecimalFormat decimalFormat = new DecimalFormat( "#.##" );

        String allMoneyStr = "";
        if (allMoney != null)
            allMoneyStr = currencyInstance.format(allMoney);

        objectList = new ArrayList<>();

        if (bonusKPIList.size() >0){
            objectList.add(Collections.singletonList(bonusKPIList));
        }
        objectList.add(Collections.singletonList((userBonusNewList)));


        model.addAttribute("userBonusKPI", bonusKPIList);
        model.addAttribute("userBonus", userBonusNewList);
        model.addAttribute("allBonusMoney", currencyInstance.format(allBonusMoney));
        model.addAttribute("allMoney", allMoneyStr);
        model.addAttribute("percentWithoutPKI", decimalFormat.format(percentWithoutPKI));
        model.addAttribute("percentWithPKI", decimalFormat.format(percentWithPKI));
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        return "bonusNew2";
    }

    @GetMapping("/export-to-excel")
    public void exportIntoExcelFile(HttpServletResponse response) throws Exception {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=bonus" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ExcelGenerator generator = new ExcelGenerator(objectList, dateS, dateF);
        generator.generate(response);
    }

    @GetMapping("/getkpi") //http://localhost:8181/userbonus/all1?date1=2021-12-01&date2=2021-12-31
    // @ApiOperation("Returns list of all products data transfer objects")
    public String getAllKPIBonuses(Model model,
                                Principal principal,
                                @RequestParam(value = "date1")
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                @RequestParam(value = "date2")
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2

    ) {
        model.addAttribute("userBonusKPI", bonusKPIList);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        return "bonusNew";
    }

    @GetMapping("/all2") //http://localhost:8181/userbonus/all1?date1=2021-12-01&date2=2021-12-31
    // @ApiOperation("Returns list of all products data transfer objects")
    public String showUserBonus(Model model) {
        List<Employer> employers = employerService.getAll();
        model.addAttribute("employers", employers);
        return "bonusShow";
    }

    @GetMapping("/add") //http://localhost:8181/userbonus/all1?date1=2021-12-01&date2=2021-12-31
    // @ApiOperation("Returns list of all products data transfer objects")
    public String addExtraBonus(Model model, @RequestParam(value ="fio") String fio,
                                @RequestParam(value = "date1")
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                @RequestParam(value = "date2")
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2,
                                HttpServletRequest httpServletRequest){

        Employer employer = employerService.findByUserName(fio);
        List<Act> actList = actBonusPercentService.getAllAct(date1, date2, employer.getManId());
        model.addAttribute("actList", actList);
        model.addAttribute("employer", employer);
        model.addAttribute("requestParam", httpServletRequest.getHeader("referer"));
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        return "act-bonus";
    }


    @ExceptionHandler
    public ResponseEntity<?> handleException(UserBonusNotFoundException exc) {
        return new ResponseEntity<>(exc.getMessage(), HttpStatus.NOT_FOUND);
    }
}