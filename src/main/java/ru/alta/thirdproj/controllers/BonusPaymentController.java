package ru.alta.thirdproj.controllers;


import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
import ru.alta.thirdproj.services.BonusPaymentSuccessServiceImpl;
import ru.alta.thirdproj.services.UserPaymentBonusServiceImpl;
import ru.alta.thirdproj.services.UserService;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

//@RestController
@Controller
@CrossOrigin("*")
@Slf4j
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

    @GetMapping("/amount/allpayment3") //http://localhost:8181/userbonus/allpayment?date1=2021-12-01&date2=2021-12-31
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

    @PostMapping("/amount/updatePaymentStatus")
    @ResponseBody
    public ResponseEntity<?> updatePaymentStatus(@RequestBody PaymentUpdateRequest request, Principal principal) {

        System.out.println("Received payment update request: " + request);
        Map<String, Object> response = new HashMap<>();

        try {
            // Детальное логирование входящего запроса
            logIncomingRequest(request, principal);

            // Валидация обязательных полей
            List<String> validationErrors = validateRequest(request);
            if (!validationErrors.isEmpty()) {
                return buildValidationErrorResponse(validationErrors);
            }

            // Получение пользователя
            User user = getUser(principal);
            if (user == null) {
                return buildErrorResponse("Пользователь не найден");
            }

            // Обработка платежа
            processPayment(request, user);

            // Получение обновленных данных
            PaymentSuccess paymentRecord = getPaymentRecord(request, user);

            // Расчет итоговых сумм
            updatePaymentTotals(request);

            // Формирование ответа
            return buildSuccessResponse(request, paymentRecord, user);

        } catch (Exception e) {
            log.error("Critical error updating payment status", e);
            return buildErrorResponse("Критическая ошибка: " + e.getMessage());
        }
    }

// Вспомогательные методы

    private void logIncomingRequest(PaymentUpdateRequest request, Principal principal) {
        System.out.println("=== INCOMING PAYMENT UPDATE REQUEST ===");
        System.out.println("User: " + (principal != null ? principal.getName() : "null"));
        System.out.println("actId: " + request.getActId());
        System.out.println("employerId: " + request.getEmployerId());
        System.out.println("candidate: " + request.getCandidate());
        System.out.println("bonus: " + request.getBonus());
        System.out.println("paid: " + request.isPaid());
        System.out.println("datePayment: " + request.getDatePayment());
        System.out.println("paymentRealDate: " + request.getPaymentRealDate());
        System.out.println("========================================");
    }

    private List<String> validateRequest(PaymentUpdateRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getEmployerId() == null) {
            errors.add("employerId обязателен");
        }

        if (request.getBonus() == null || request.getBonus() <= 0) {
            errors.add("bonus должен быть положительным числом");
        }

        if (request.getActId() == null) {
            errors.add("actId обязателен");
        }

        if (request.getCandidate() == null || request.getCandidate().trim().isEmpty()) {
            errors.add("candidate обязателен");
        }

        return errors;
    }

    private User getUser(Principal principal) {
        try {
            return userService.findByUserName(principal.getName());
        } catch (Exception e) {
            log.error("Error finding user", e);
            return null;
        }
    }

    private void processPayment(PaymentUpdateRequest request, User user) throws Exception {
        LocalDate paymentDate = request.getPaymentRealDate() != null ?
                request.getPaymentRealDate() : LocalDate.now();

        if (request.isPaid()) {
            // Оплата бонуса
            int type = 0;
            int month = 0;

            if (request.getActId() == 0) {
                // Обработка для actId = 0
                Date date = new SimpleDateFormat("dd-MM-yyyy").parse(request.getDatePayment());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                month = cal.get(Calendar.MONTH) + 1;
                type = 2;
            } else {
                // Обычная обработка
                type = 1;
                month = 0;
            }

            paymentSuccessService.addPayment(
                    user.getUserId(),
                    request.getEmployerId(),
                    request.getBonus(),
                    request.getActId(),
                    request.getCandidate(),
                    0,
                    month,
                    type
            );
        } else {
            // Отмена оплаты
            paymentSuccessService.deletePayment(
                    user.getUserId(),
                    request.getEmployerId(),
                    paymentDate,
                    request.getBonus(),
                    request.getActId(),
                    request.getCandidate(),
                    request.getBonus()
            );
        }
    }

    private PaymentSuccess getPaymentRecord(PaymentUpdateRequest request, User user) {
        try {
            Optional<PaymentSuccess> paymentRecord = paymentSuccessService.findByActId(
                    user.getUserId(),
                    request.getActId(),
                    request.getCandidate(),
                    request.getBonus()
            );
            return paymentRecord.orElse(null);
        } catch (Exception e) {
            log.error("Error finding payment record", e);
            return null;
        }
    }

    private void updatePaymentTotals(PaymentUpdateRequest request) {
        double bonus = request.getBonus() != null ? request.getBonus() : 0.0;

        if (request.isPaid()) {
            allPaymentAmount += bonus;
            if (allNotPaymentAmount != 0.0) {
                allNotPaymentAmount -= bonus;
            }
        } else {
            allPaymentAmount -= bonus;
            allNotPaymentAmount += bonus;
        }

        // Защита от отрицательных значений
        allPaymentAmount = Math.max(0, allPaymentAmount);
        allNotPaymentAmount = Math.max(0, allNotPaymentAmount);
    }

    private ResponseEntity<?> buildSuccessResponse(PaymentUpdateRequest request,
                                                   PaymentSuccess paymentRecord,
                                                   User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("paid", request.isPaid());

        // Данные о дате платежа
        if (paymentRecord != null && paymentRecord.getPaymentDateOnly() != null) {
            response.put("paymentRealDate", paymentRecord.getPaymentDateOnly());
        } else {
            response.put("paymentRealDate", request.getPaymentRealDate() != null ?
                    request.getPaymentRealDate().toString() : "");
        }

        // Данные о сотруднике
        if (paymentRecord != null && user != null) {
            response.put("employerPaid", user.getUserFIOShot() + ' ' + request.getBonus());
        } else {
            response.put("employerPaid", "");
        }

        // Итоговые суммы
        response.put("allPaymentMoney", formatMoney(allPaymentAmount));
        response.put("allNotPaymentMoney", formatMoney(allNotPaymentAmount));

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> buildValidationErrorResponse(List<String> errors) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Ошибки валидации");
        response.put("errors", errors);

        System.err.println("Validation errors: " + errors);
        return ResponseEntity.badRequest().body(response);
    }

    private ResponseEntity<?> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);

        System.err.println("Error: " + message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


//    @PostMapping("/amount/updatePaymentStatus")
//    @ResponseBody
//    public ResponseEntity<?> updatePaymentStatus(@RequestBody PaymentUpdateRequest request, Principal principal) {
//        try {
////            if (request.getActId() == null) {
////                throw new IllegalArgumentException("ID акта обязательно");
////            }
//
//
//
//            LocalDate paymentDate = null;
//            LocalDate dateForKpi = null;
//            int type = 0;
//            int month = 0;
//            System.out.println("Received payment update request: " + request);
//            User user = userService.findByUserName(principal.getName());
//
//
//            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
//
//
//            if (request.getPaymentRealDate() != null) {
//             paymentDate = request.getPaymentRealDate() != null ?
//                     request.getPaymentRealDate() :
//                     LocalDate.now();
//            }
//            if (request.isPaid()) {
//                if (request.getActId() == 0){
//                    Date date = format.parse(request.getDatePayment());
//                    Calendar cal = Calendar.getInstance();
//                    cal.setTime(date);
//                    month = cal.get(Calendar.MONTH) + 1;
//                    type = 2;
//                }
//                else {
//                    type =1;
//                    month = 0;
//                }
//                paymentSuccessService.addPayment(user.getUserId(), request.getEmployerId(), request.getBonus(),
//                        request.getActId(), request.getCandidate(), 0, month, type);
//            } else {
//                paymentSuccessService.deletePayment(user.getUserId(), request.getEmployerId(),
//                        paymentDate,
//                        request.getBonus(),
//                        request.getActId(), request.getCandidate(), request.getBonus());
//            }
//            Optional<PaymentSuccess> act =  paymentSuccessService.findByActId(user.getUserId(), request.getActId(),
//                    request.getCandidate(), request.getBonus());
//
//            double bonus = request.getBonus();
//            if (request.isPaid()) {
//                allPaymentAmount += bonus;
//                if (allNotPaymentAmount != 0.0) {
//                    allNotPaymentAmount -= bonus;
//                }
//            } else {
//                allPaymentAmount -= bonus;
//                allNotPaymentAmount += bonus;
//            }
//
//            // Форматируем для отображения
//            String formattedPayment = formatMoney(allPaymentAmount);
//            String formattedNotPayment = formatMoney(allNotPaymentAmount);
//
//
//
//            return ResponseEntity.ok(Map.of(
//                    "status", "success",
//                    "paid", request.isPaid(),
//                    "paymentRealDate", !act.isEmpty() ?
//                            act.get().getPaymentDateOnly() : "",
//                    "employerPaid", !act.isEmpty() ?
//                            user.getUserFIOShot() + ' ' +request.getBonus() : "",
//                    "allPaymentMoney", formattedPayment,
//                    "allNotPaymentMoney", formattedNotPayment
//            ));
//        } catch (Exception e) {
//            System.err.println("Error updating payment status: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", e.getMessage()));
//        }
//    }
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