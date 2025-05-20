package ru.alta.thirdproj.controllers;


import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.*;
import ru.alta.thirdproj.entites.*;
import ru.alta.thirdproj.exceptions.UserBonusNotFoundException;
import ru.alta.thirdproj.export.ExcelGenerator;
import ru.alta.thirdproj.response.JsonResponse;
import ru.alta.thirdproj.services.BonusPaymentSuccessServiceImpl;
import ru.alta.thirdproj.services.UserPaymentBonusServiceImpl;
import ru.alta.thirdproj.services.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

//@RestController
@Controller
@CrossOrigin("*")
@RequestMapping("/payment") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
@Tag(name="RestBonusPaymentController", description="Выплаты по бонусам")
public class BonusPaymentController {

    private UserPaymentBonusServiceImpl paymentBonusService;
    private BonusPaymentSuccessServiceImpl paymentSuccessService;
    private UserService userService;
    private LocalDate dateS;
    private LocalDate dateF;
    private List<List<Object>> objectList;
    private String allMoney;
    private String allPaymentMoney;
    private String allNotPaymentMoney;
    private String moneyByDate;

    private double allPaymentAmount;
    private double allNotPaymentAmount;

    @Autowired
    public BonusPaymentController(UserPaymentBonusServiceImpl paymentBonusService) {
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

    @GetMapping("/amount")
    public String showTabs() {
        return "payment"; // Renders the main tabs.html template tabs_2 - остается все по старому
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

    @GetMapping("/allpayment3") //http://localhost:8181/userbonus/allpayment?date1=2021-12-01&date2=2021-12-31
    @ApiOperation("Returns list of all products data transfer objects")
    public String showAll3(Model model, Principal principal,
                          @RequestParam(value = "date1")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                          @RequestParam(value = "date2")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2


    ) {
        dateS = date1;
        dateF = date2;
        List<EmployerNew> employerList = paymentBonusService.getEmployerList(date1, date2);
        this.allMoney = paymentBonusService.getAllMoney(employerList);
        String allPaymentMoney = paymentBonusService.getAllPaymentMoney(employerList);
        String allNotPaymentMoney = paymentBonusService.getAllNotPaymentMoney(employerList);


        this.allPaymentAmount = paymentBonusService.getAllPaymentMoneyDouble(employerList);

        this.allNotPaymentAmount = paymentBonusService.getAllNotPaymentMoneyDouble(employerList);

        this.moneyByDate = paymentBonusService.getMoneyByDate(employerList);

        objectList = new ArrayList<>();

        objectList.add(Collections.singletonList(employerList));

        model.addAttribute("employerList", employerList);
        model.addAttribute("allMoney", allMoney);
        model.addAttribute("allPaymentMoney", allPaymentMoney);
        model.addAttribute("moneyByDate", moneyByDate);
        model.addAttribute("allNotPaymentMoney", allNotPaymentMoney);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        return "payment";
    }


    @PostMapping("/updatePaymentStatus")
    @ResponseBody
    public ResponseEntity<?> updatePaymentStatus(@RequestBody PaymentUpdateRequest request, Principal principal) {
        try {
//            if (request.getActId() == null) {
//                throw new IllegalArgumentException("ID акта обязательно");
//            }

            LocalDate paymentDate = null;
            System.out.println("Received payment update request: " + request);
            User user = userService.findByUserName(principal.getName());
            if (request.getPaymentRealDate() != null) {
             paymentDate = request.getPaymentRealDate() != null ?
                    request.getPaymentRealDate() :
                    LocalDate.now();//            //    date = format.parse(request.getPaymentRealDate());


//            Calendar cal = Calendar.getInstance();
//            cal.setTime(paymentDate);
//            int month = cal.get(Calendar.MONTH);
            }
            if (request.isPaid()) {

                paymentSuccessService.addPayment(user.getUserId(), request.getEmployerId(), request.getBonus(),
                        request.getActId(), request.getCandidate(), 0, "", 1);
            } else {
                paymentSuccessService.deletePayment(user.getUserId(), request.getEmployerId(),
                        paymentDate,
                        request.getBonus(),
                        request.getActId(), request.getCandidate(), request.getBonus());
            }
            Optional<PaymentSuccess> act =  paymentSuccessService.findByActId(user.getUserId(), request.getActId(),
                    request.getCandidate(), request.getBonus());

            double bonus = request.getBonus();
            if (request.isPaid()) {
                allPaymentAmount += bonus;
                if (allNotPaymentAmount != 0.0) {
                    allNotPaymentAmount -= bonus;
                }
            } else {
                allPaymentAmount -= bonus;
                allNotPaymentAmount += bonus;
            }

            // Форматируем для отображения
            String formattedPayment = formatMoney(allPaymentAmount);
            String formattedNotPayment = formatMoney(allNotPaymentAmount);



            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "paid", request.isPaid(),
                    "paymentRealDate", !act.isEmpty() ?
                            act.get().getPaymentDateOnly() : "",
                    "employerPaid", !act.isEmpty() ?
                            user.getUserFIOShot() + ' ' +request.getBonus() : "",
                    "allPaymentMoney", formattedPayment,
                    "allNotPaymentMoney", formattedNotPayment
            ));
        } catch (Exception e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    private double parseMoney(String moneyStr) {
        return Double.parseDouble(moneyStr.replaceAll("[^\\d.]", ""));
    }

    private String formatMoney(double amount) {
        return String.format("%,.2f", amount).replace(",", " ");
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



    @ExceptionHandler
    public ResponseEntity<?> handleException(UserBonusNotFoundException exc) {
        return new ResponseEntity<>(exc.getMessage(), HttpStatus.NOT_FOUND);
    }
}