package ru.alta.thirdproj.controllers;


import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.alta.thirdproj.entites.*;
import ru.alta.thirdproj.exceptions.UserBonusNotFoundException;
import ru.alta.thirdproj.repositories.UserLoginRepositorySlqO2;
import ru.alta.thirdproj.services.BonusPaymentSuccessServiceImpl;
import ru.alta.thirdproj.services.UserPaymentBonusServiceImpl;
import ru.alta.thirdproj.services.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

    @GetMapping("/allpayment") //http://localhost:8181/userbonus/allpayment?date1=2021-12-01&date2=2021-12-31
//    @ApiOperation("Returns list of all products data transfer objects")
    public ResponseEntity<UserPaymentBonus> getAllUserBonus(Principal principal,
                                            @RequestParam(value = "date1")
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                             @RequestParam(value = "date2")
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date2


    ) {
        User user = userService.findByUserName(principal.getName());

        List<HashMap<String, Object>> userPaymentBonuses;
//        if (!userName.equals("") || !departmentName.equals(""))   {
//            userPaymentBonuses =  paymentBonusService.findByUserFIOAnfDepartment(userName, departmentName);
//
//        }
//        else
            userPaymentBonuses = paymentBonusService.findAll(date1, date2);

        return new ResponseEntity(userPaymentBonuses, HttpStatus.OK);
    }

    @GetMapping("/allpayment1") //http://localhost:8181/userbonus/allpayment?date1=2021-12-01&date2=2021-12-31
//    @ApiOperation("Returns list of all products data transfer objects")
    public String showAll(Model model, Principal principal,
                                  @RequestParam(value = "date1")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                                  @RequestParam(value = "date2")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2
//                                  @RequestParam(value = "userName", required = false) String userName,
//                                  @RequestParam(value = "departmentName", required = false) String departmentName

    ) {
//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        UserLogin user = userR.getUser(userDetails.getUsername());

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
//    @ApiOperation("Returns list of all products data transfer objects")
    public String showAll3(Model model, Principal principal,
                          @RequestParam(value = "date1")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                          @RequestParam(value = "date2")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2


    ) {
        employerList = paymentBonusService.getEmployerList(date1, date2);

        model.addAttribute("employerList", employerList);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);

        return "paymentNew";
    }


    @PostMapping("/confirm")
    public String paymentConfirm(
            @RequestParam (value = "fio", required = false) String fio,
            HttpServletRequest httpServletRequest,  Principal principal) {

        User user = userService.findByUserName(principal.getName());
        paymentSuccessService.findEmployer(fio, user.getUserId(), employerList);
        String referrer = httpServletRequest.getHeader("referer");
        return "redirect:" + referrer;

    }


    @ExceptionHandler
    public ResponseEntity<?> handleException(UserBonusNotFoundException exc) {
        return new ResponseEntity<>(exc.getMessage(), HttpStatus.NOT_FOUND);
    }
}