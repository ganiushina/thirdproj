package ru.alta.thirdproj.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.alta.thirdproj.entites.*;
import ru.alta.thirdproj.services.*;

import java.security.Principal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class MarginController {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private UserSalaryServiceImpl userSalaryService;
    private MarginBonusServiceImpl marginBonusService;
    private UserSalesServiceImpl userSalesService;
    private UserService userService;

    private EmailUserSendConfiguration emailUserSendConfiguration;
    private EmailPaymentSuccessService emailPaymentSuccessService;

    @Autowired
    public void setUserSalaryService(UserSalaryServiceImpl userSalaryService, MarginBonusServiceImpl marginBonusService,
                                     UserSalesServiceImpl userSalesService, UserService userService,
                                     EmailUserSendConfiguration emailUserSendConfiguration,
                                     EmailPaymentSuccessService emailPaymentSuccessService){
        this.userSalaryService = userSalaryService;
        this.marginBonusService = marginBonusService;
        this.userSalesService = userSalesService;
        this.userService = userService;
        this.emailUserSendConfiguration = emailUserSendConfiguration;
        this.emailPaymentSuccessService = emailPaymentSuccessService;
    }


    @GetMapping("/margin")
    public String showTabs() {
        return "margin"; // Renders the main tabs.html template tabs_2 - остается все по старому
    }

    // Shared date range processing method
    private void processDateRange(LocalDate dateFrom, LocalDate dateTo, Model model) {
        if (dateFrom == null) dateFrom = LocalDate.now().minusDays(7);
        if (dateTo == null) dateTo = LocalDate.now();

        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
    }

    @GetMapping("/margin/summary")
    public String getSummaryReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            Model model) {

        processDateRange(dateFrom, dateTo, model);
        List<MarginBonus> marginBonuses = userSalesService.getMarginBonusByMonth(dateFrom,dateTo);
        Double totalMargin = marginBonuses.stream()
                .mapToDouble(bonus -> bonus.getMargin() != null ? bonus.getMargin() : 0.0)
                .sum();
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        String allMargin = formatter.format(totalMargin) + " ₽";
        model.addAttribute("marginBonusByMonth", marginBonuses);
        model.addAttribute("allMargin", allMargin);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        return "summary :: summaryTab"; // Fragment for AJAX
    }

    @GetMapping("/margin/detailed")
    public String getDetailedReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            Model model) {

        processDateRange(dateFrom, dateTo, model);
        List<MarginBonusBDM> marginBonusList = marginBonusService.getAllMarginBonus(dateFrom,dateTo);
        Double allMargin = marginBonusList.stream()
                .filter(Objects::nonNull)
                .flatMap(bdm -> bdm.getMarginDepartmentSum() != null ?
                        bdm.getMarginDepartmentSum().stream() :
                        Stream.empty())
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        String formattedMargin = formatter.format(allMargin) + " ₽";
        model.addAttribute("marginBonusList", marginBonusList);
        model.addAttribute("totalMargin", formattedMargin);
        return "details :: detailsTab"; // Fragment for AJAX
    }

    @GetMapping("/margin/charts")
    public String getChartData(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            Principal principal, Model model) {

        processDateRange(dateFrom, dateTo, model);
        User user = userService.findByUserName(principal.getName());

        //user.getRoles()
        List<UserSalary> userSalaryList = marginBonusService.getUserSalary(dateFrom,dateTo, user.getLoginDepartment());
        model.addAttribute("userSalaryList", userSalaryList);
        return "charts :: chartsTab"; // Fragment for AJAX
    }

    @GetMapping("/margin/interpreters")
    public String getInterpreterData(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            Model model) {

        processDateRange(dateFrom, dateTo, model);

        // Получаем данные о выплатах
        List<UserSalaryDetail> userSalaryDetailList = marginBonusService.getUserSalaryInterpreter(dateFrom, dateTo);
        model.addAttribute("userSalaryList", userSalaryDetailList);

        // Получаем статус проверки
        List<UserSalarySuccess> userSalarySuccesses = userSalaryService.getUserSalarySuccess(dateFrom, dateTo);
        String verificationInfo = userSalarySuccesses.isEmpty() ? null :
                getSalarySuccess(userSalarySuccesses);

        model.addAttribute("userSalarySuccesses", verificationInfo);

        return "interpreter :: interpreterTab";
    }

    @PostMapping("/margin/send-verification-emails")
    @ResponseBody
    public ResponseEntity<Map<String, String>> sendVerificationEmails(
            @RequestBody Map<String, String> dateParams,
            Principal principal) {

        Map<String, String> response = new HashMap<>();

        try {
            LocalDate dateFrom = LocalDate.parse(dateParams.get("dateFrom"));
            LocalDate dateTo = LocalDate.parse(dateParams.get("dateTo"));

            // 1. Отправка писем
            String period = formatPeriod(dateFrom, dateTo);
            emailUserSendConfiguration.sendAccessToAllUsers(dateFrom, dateTo, period);

            // 2. Сохранение статуса проверки
            User user = userService.findByUserName(principal.getName());
            emailPaymentSuccessService.save(user.getUserId(), dateFrom, dateTo, 1);

            // 3. Получение информации для отображения
            List<UserSalarySuccess> successData = userSalaryService.getUserSalarySuccess(dateFrom, dateTo);
            String successInfo = getSalarySuccess(successData);

            response.put("status", "Проверено");
            response.put("successInfo", successInfo);
            response.put("message", "Проверка выплат за " + period + " успешно отправлена");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Ошибка: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    private String getSalarySuccess(List<UserSalarySuccess> userSalarySuccesses) {
        if (userSalarySuccesses == null || userSalarySuccesses.isEmpty()) {
            return "Нет данных о проверке";
        }

        UserSalarySuccess first = userSalarySuccesses.get(0);
        String period = formatPeriod(first.getDateFrom(), first.getDateTo());

        String inspectors = userSalarySuccesses.stream()
                .filter(u -> u.getSuccess() == 1)
                .map(UserSalarySuccess::getUserFio)
                .distinct()
                .collect(Collectors.joining(", "));

        return inspectors.isEmpty() ?
                "Данные за период " + period + " не были проверены" :
                "Данные за период " + period + " были проверены: " + inspectors;
    }


//    @GetMapping("/margin/interpreters")
//    public String getInterpreterData(
//            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
//            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
//            Model model) {
//        try {
//            processDateRange(dateFrom, dateTo, model);
//            List<UserSalaryDetail> userSalaryDetailList = marginBonusService.getUserSalaryInterpreter(dateFrom,dateTo);
//            List<UserSalarySuccess> userSalarySuccesses = userSalaryService.getUserSalarySuccess(dateFrom,dateTo);
//            String userSalarySuccessesStr = getSalarySuccess(userSalarySuccesses);
//            model.addAttribute("userSalaryList", userSalaryDetailList);
//            model.addAttribute("userSalarySuccesses", userSalarySuccessesStr);
//            return "interpreter :: interpreterTab";
//        } catch (Exception e) {
//            e.printStackTrace(); // или logger.error("Error in getInterpreterData", e);
//            throw e; // или return error page
//        }
//    }
//
//    @PostMapping("/margin/send-verification-emails")
//    @ResponseBody
//    public ResponseEntity<Map<String, String>> sendVerificationEmails(
//            @RequestBody Map<String, String> dateParams,
//            Principal principal) {
//
//        Map<String, String> response = new HashMap<>();
//
//        try {
//            LocalDate dateFrom = LocalDate.parse(dateParams.get("dateFrom"));
//            LocalDate dateTo = LocalDate.parse(dateParams.get("dateTo"));
//
//            // 1. Отправка писем
//            String period = formatPeriod(dateFrom, dateTo);
//            emailUserSendConfiguration.sendAccessToAllUsers(dateFrom, dateTo, period);
//
//            // 2. Сохранение статуса проверки
//            User user = userService.findByUserName(principal.getName());
//            emailPaymentSuccessService.save(user.getUserId(), dateFrom, dateTo, 1);
//
//            // 3. Получение информации о проверке
//            List<UserSalarySuccess> successData = userSalaryService.getUserSalarySuccess(dateFrom, dateTo);
//            String successInfo = getSalarySuccess(successData);
//
//            // Формируем ответ
//            response.put("message", "Письма о сверке выплат успешно отправлены за " + period);
//            response.put("successInfo", successInfo);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            response.put("error", "Ошибка при отправке: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(response);
//        }
//    }

    private String formatPeriod(LocalDate dateFrom, LocalDate dateTo) {
        // Словарь для замены окончаний
        Map<String, String> monthReplacements = Map.ofEntries(
                Map.entry("января", "январь"),
                Map.entry("февраля", "февраль"),
                Map.entry("марта", "март"),
                Map.entry("апреля", "апрель"),
                Map.entry("мая", "май"),
                Map.entry("июня", "июнь"),
                Map.entry("июля", "июль"),
                Map.entry("августа", "август"),
                Map.entry("сентября", "сентябрь"),
                Map.entry("октября", "октябрь"),
                Map.entry("ноября", "ноябрь"),
                Map.entry("декабря", "декабрь")
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("ru"));
        String from = dateFrom.format(formatter);
        String to = dateTo.format(formatter);

        // Заменяем окончания
        for (Map.Entry<String, String> entry : monthReplacements.entrySet()) {
            from = from.replace(entry.getKey(), entry.getValue());
            to = to.replace(entry.getKey(), entry.getValue());
        }

        if (dateFrom.withDayOfMonth(1).equals(dateTo.withDayOfMonth(1))) {
            return from;
        }
        return from + " - " + to;
    }

//    private String getSalarySuccess(List<UserSalarySuccess> userSalarySuccesses) {
//        if (userSalarySuccesses == null || userSalarySuccesses.isEmpty()) {
//            return "Нет данных о проверке";
//        }
//
//        // Берем первый элемент для получения периода (предполагаем, что период одинаков для всех)
//        UserSalarySuccess first = userSalarySuccesses.get(0);
//        String period = formatPeriod(first.getDateFrom(), first.getDateTo());
//
//        // Собираем ФИО всех проверяющих с успешной проверкой (success = 1)
//        String inspectors = userSalarySuccesses.stream()
//                .filter(user -> user.getSuccess() == 1) // Проверяем, что success равно 1
//                .map(UserSalarySuccess::getUserFio)
//                .distinct()
//                .collect(Collectors.joining(", "));
//
//        if (inspectors.isEmpty()) {
//            return "Данные за период " + period + " не были проверены";
//        }
//
//        return "Данные за период " + period + " были проверены: " + inspectors;
//    }
}

