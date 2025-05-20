//package ru.alta.thirdproj.controllers;
//
//
//import io.swagger.annotations.ApiOperation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.validation.ValidationUtils;
//import org.springframework.web.bind.annotation.*;
//import ru.alta.thirdproj.entites.EmployerNew;
//import ru.alta.thirdproj.entites.User;
//import ru.alta.thirdproj.entites.UserPaymentBonus;
//import ru.alta.thirdproj.exceptions.UserBonusNotFoundException;
//import ru.alta.thirdproj.export.ExcelGenerator;
//import ru.alta.thirdproj.response.JsonResponse;
//import ru.alta.thirdproj.services.BonusPaymentSuccessServiceImpl;
//import ru.alta.thirdproj.services.UserPaymentBonusServiceImpl;
//import ru.alta.thirdproj.services.UserService;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.security.Principal;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.util.*;
//
////@RestController
//@Controller
//@CrossOrigin("*")
//@RequestMapping("/payment2") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
//@Tag(name="RestBonusPaymentController", description="Выплаты по бонусам")
//public class BonusPaymentAjax {
//
//    private UserPaymentBonusServiceImpl paymentBonusService;
//    private BonusPaymentSuccessServiceImpl paymentSuccessService;
//    private UserService userService;
//    private List<EmployerNew> employerList;
//    private LocalDate dateS;
//    private LocalDate dateF;
//    private List<List<Object>> objectList;
//
//    @Autowired
//    public BonusPaymentAjax(UserPaymentBonusServiceImpl paymentBonusService) {
//        this.paymentBonusService = paymentBonusService;
//    }
//
//    @Autowired
//    public void setUserService(UserService userService) {
//        this.userService = userService;
//    }
//
//    @Autowired
//    public void setPaymentSuccessService(BonusPaymentSuccessServiceImpl paymentSuccessService){
//        this.paymentSuccessService = paymentSuccessService;
//    }
//
//    @GetMapping("/allpaymentAjax")
//    public String showForm(){
//        return "Ajaxtest";
//    }
//
//    @PostMapping(value = "/confirm")
//    public @ResponseBody JsonResponse paymentConfirm (
//            @RequestParam (value = "fio", required = false) String fio,
//            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Principal principal, Model model , BindingResult result)  {
//
//        User user = userService.findByUserName(principal.getName());
//
//        paymentSuccessService.findActInList(fio, user.getUserId(), employerList);
//
//        String allPaymentMoney ;
//
//
//        JsonResponse res = new JsonResponse();
//        ValidationUtils.rejectIfEmpty(result, "name", "Name can not be empty.");
//        ValidationUtils.rejectIfEmpty(result, "education", "Educatioan not be empty");
//        if(!result.hasErrors()){
//            paymentSuccessService.findActInList(fio, user.getUserId(), employerList);
//
//            allPaymentMoney = paymentBonusService.getAllPaymentMoney(employerList);
//            res.setStatus("SUCCESS");
//            res.setResult(objectList);
//        }else{
//            res.setStatus("FAIL");
//            res.setResult(result.getAllErrors());
//        }
//        return res;
//
//    }
//
//    @ExceptionHandler
//    public ResponseEntity<?> handleException(UserBonusNotFoundException exc) {
//        return new ResponseEntity<>(exc.getMessage(), HttpStatus.NOT_FOUND);
//    }
//}