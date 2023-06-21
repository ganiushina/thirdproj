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
import ru.alta.thirdproj.entites.EmployerNew;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.entites.UserPaymentBonus;
import ru.alta.thirdproj.exceptions.UserBonusNotFoundException;
import ru.alta.thirdproj.services.BonusPaymentSuccessServiceImpl;
import ru.alta.thirdproj.services.UserPaymentBonusServiceImpl;
import ru.alta.thirdproj.services.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

//@RestController
@Controller
@CrossOrigin("*")
@RequestMapping("/payment") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
@Tag(name="RestBonusPaymentController", description="Выплаты по бонусам")
public class RestBonusPaymentController {

    private UserPaymentBonusServiceImpl paymentBonusService;

    private BonusPaymentSuccessServiceImpl paymentSuccessService;

    private UserService userService;

    private List<EmployerNew> employerList;

    @Autowired
    public RestBonusPaymentController(UserPaymentBonusServiceImpl paymentBonusService) {
        this.paymentBonusService = paymentBonusService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setPaymentSuccessService(BonusPaymentSuccessServiceImpl paymentSuccessService){
        this.paymentSuccessService = paymentSuccessService;
    }

    @PostMapping("/allpayment") //http://localhost:8181/userbonus/allpayment?date1=2021-12-01&date2=2021-12-31
    @ApiOperation("Returns list of all products data transfer objects")
    public ResponseEntity<UserPaymentBonus> getAllUserBonus(Principal principal,
                                            @RequestParam(value = "date1")
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                             @RequestParam(value = "date2")
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date2


    ) {
        User user = userService.findByUserName(principal.getName());

        List<HashMap<String, Object>> userPaymentBonuses;
            userPaymentBonuses = paymentBonusService.findAll(date1, date2);

        return new ResponseEntity(userPaymentBonuses, HttpStatus.OK);
    }

    @GetMapping("/allpayment1") //http://localhost:8181/userbonus/allpayment?date1=2021-12-01&date2=2021-12-31
    @ApiOperation("Returns list of all products data transfer objects")
    public String showAll(Model model, Principal principal,
                                  @RequestParam(value = "date1")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                  @RequestParam(value = "date2")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2

    ) {


        User user = userService.findByUserName(principal.getName());


        List<HashMap<String, Object>> userPaymentBonuses;

            userPaymentBonuses = paymentBonusService.findAll(date1, date2);

        HashMap<String,Object> mapActNum = paymentBonusService.getMapActNum();
        HashMap<String,Object> mapBonus = paymentBonusService.getMapBonus();
        HashMap<String,Object> mapCandidate =  paymentBonusService.getMapCandidate();
        HashMap<String,Object> mapCompany =  paymentBonusService.getMapCompany();
        List<String> employers = paymentBonusService.getEmployers();
        List<String> department = paymentBonusService.getDepartment();


        model.addAttribute("userPaymentBonuses", userPaymentBonuses);
        model.addAttribute("actNum", mapActNum);
        model.addAttribute("bonus", mapBonus);
        model.addAttribute("candidateName", mapCandidate);
        model.addAttribute("companyName", mapCompany);
        model.addAttribute("employers", employers);
        model.addAttribute("department", department);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        return "payment";
    }



    @GetMapping("/allpayment3") //http://localhost:8181/userbonus/allpayment?date1=2021-12-01&date2=2021-12-31
    @ApiOperation("Returns list of all products data transfer objects")
    public String showAll3(Model model, Principal principal,
                          @RequestParam(value = "date1")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                          @RequestParam(value = "date2")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2


    ) {
        employerList = paymentBonusService.getEmployerList(date1, date2);
        String allMoney = paymentBonusService.getAllMoney(employerList);
        String allPaymentMoney = paymentBonusService.getAllPaymentMoney(employerList);
        String allNotPaymentMoney = paymentBonusService.getAllNotPaymentMoney(employerList);

        String moneyByDate = paymentBonusService.getMoneyByDate(employerList);

        model.addAttribute("employerList", employerList);
        model.addAttribute("allMoney", allMoney);
        model.addAttribute("allPaymentMoney", allPaymentMoney);
        model.addAttribute("moneyByDate", moneyByDate);
        model.addAttribute("allNotPaymentMoney", allNotPaymentMoney);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        return "paymentNew1";
    }


    @PostMapping(value = "/confirm", headers = "HX-Request")
    @ApiOperation("Confirm payment")
    public String paymentConfirm (
            @RequestParam (value = "fio", required = false) String fio,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Principal principal, Model model)  {

        User user = userService.findByUserName(principal.getName());

        paymentSuccessService.findActInList(fio, user.getUserId(), employerList);

        String allPaymentMoney = paymentBonusService.getAllPaymentMoney(employerList);
        String referrer = httpServletRequest.getHeader("referer");
        model.addAttribute("allPaymentMoney", allPaymentMoney);
        return "redirect:" + referrer;
    }

    @ExceptionHandler
    public ResponseEntity<?> handleException(UserBonusNotFoundException exc) {
        return new ResponseEntity<>(exc.getMessage(), HttpStatus.NOT_FOUND);
    }
}