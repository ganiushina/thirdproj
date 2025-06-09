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
import java.util.stream.Stream;

//@RestController
@Controller
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


    @GetMapping("/bonus")
    public String showTabs() {
        return "bonuses"; // Renders the main tabs.html template tabs_2 - остается все по старому
    }
    @GetMapping("/bonus/getall")
    public String getAllBonuses(Model model,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo) {

        // Получение данных
        List<UserBonusMain> userBonusMains = getSafeUserBonuses(dateFrom, dateTo);
        List<UserBonusKPIMain> bonusKPIList = getSafeKPIBonuses(dateFrom, dateTo);

        // Расчет сумм
        double allBonusMoney = calculateTotalBonusMoney(userBonusMains);
        double allMoney = getSafeCompanyMoney(dateFrom, dateTo);
        double allKPIMoney = calculateTotalKPIMoney(bonusKPIList);

        // Форматирование и подготовка модели
        prepareModel(model, userBonusMains, bonusKPIList,
                allBonusMoney, allMoney, allKPIMoney,
                dateFrom, dateTo);

        return "bonus :: bonusTab";
    }

// Обновленные вспомогательные методы с улучшенной обработкой null

    private List<UserBonusMain> getSafeUserBonuses(LocalDate dateFrom, LocalDate dateTo) {
        return Optional.ofNullable(bonusService.getUserBonuses(dateFrom, dateTo))
                .orElse(Collections.emptyList());
    }

    private List<UserBonusKPIMain> getSafeKPIBonuses(LocalDate dateFrom, LocalDate dateTo) {
        return Optional.ofNullable(bonusKPIService.getUserBonusKPIList(dateFrom, dateTo))
                .orElse(Collections.emptyList());
    }

    private double getSafeCompanyMoney(LocalDate dateFrom, LocalDate dateTo) {
        return Optional.ofNullable(bonusService.getCompanyMoney(dateFrom, dateTo))
                .orElse(0.0);
    }

    private double calculateTotalBonusMoney(List<UserBonusMain> userBonusList) {
        return userBonusList.stream()
                .filter(Objects::nonNull)
                .flatMap(bonus -> bonus.getUserBonusDetails() != null ?
                        bonus.getUserBonusDetails().stream() : Stream.empty())
                .filter(Objects::nonNull)
                .mapToDouble(detail -> detail.getMoneyByCandidate() != null ?
                        detail.getMoneyByCandidate() : 0.0)
                .sum();
    }

    private double calculateTotalKPIMoney(List<UserBonusKPIMain> kpiMainList) {
        return kpiMainList.stream()
                .filter(Objects::nonNull)
                .flatMap(main -> main.getUserBonusKPIDetails() != null ?
                        main.getUserBonusKPIDetails().stream() : Stream.empty())
                .filter(Objects::nonNull)
                .mapToDouble(detail -> {
                    double sum = 0.0;
                    if (detail.getAllBonus() != null) sum += detail.getAllBonus();
                    return sum;
                })
                .sum();
    }

    private void prepareModel(Model model,
                              List<UserBonusMain> userBonusMains,
                              List<UserBonusKPIMain> bonusKPIList,
                              double allBonusMoney,
                              double allMoney,
                              double allKPIMoney,
                              LocalDate dateFrom,
                              LocalDate dateTo) {

        NumberFormat currencyFormatter = createCurrencyFormatter();
        DecimalFormat decimalFormatter = new DecimalFormat("#.##");

        // Форматирование с защитой от ошибок
        String formattedAllBonusMoney;
        String formattedAllMoney;
        try {
            formattedAllBonusMoney = currencyFormatter.format(allBonusMoney);
            formattedAllMoney = currencyFormatter.format(allMoney);
        } catch (IllegalArgumentException e) {
            formattedAllBonusMoney = String.format("%,.2f ₽", allBonusMoney);
            formattedAllMoney = String.format("%,.2f ₽", allMoney);
        }

        // Расчет процентов с защитой от деления на ноль
        double percentWithoutPKI = allMoney > 0 ? (allBonusMoney * 100 / allMoney) : 0;
        double percentWithPKI = allMoney > 0 ? ((allBonusMoney + allKPIMoney) * 100 / allMoney) : 0;

        // Добавление атрибутов в модель

        model.addAttribute("userBonus", userBonusMains != null ? userBonusMains : Collections.emptyList());
        model.addAttribute("allBonusMoney", formattedAllBonusMoney);
        model.addAttribute("allMoney", formattedAllMoney);
        model.addAttribute("percentWithoutPKI", decimalFormatter.format(percentWithoutPKI));
        model.addAttribute("percentWithPKI", decimalFormatter.format(percentWithPKI));
        model.addAttribute("date1", dateFrom);
        model.addAttribute("date2", dateTo);

        System.out.println("Передаваемые атрибуты: " +
                "allMoney=" + allMoney +
                ", allBonusMoney=" + allBonusMoney +
                ", percent=" + percentWithoutPKI);
    }

    private NumberFormat createCurrencyFormatter() {
        Locale russianLocale = new Locale("ru", "RU");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(russianLocale);
        currencyFormatter.setCurrency(Currency.getInstance("RUB"));
        return currencyFormatter;
    }
//    @GetMapping("/export-to-excel")
//    public void exportIntoExcelFile(HttpServletResponse response) throws Exception {
//        response.setContentType("application/octet-stream");
//        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
//        String currentDateTime = dateFormatter.format(new Date());
//        String headerKey = "Content-Disposition";
//        String headerValue = "attachment; filename=bonus" + currentDateTime + ".xlsx";
//        response.setHeader(headerKey, headerValue);
//        ExcelGenerator generator = new ExcelGenerator(objectList, dateS, dateF);
//        generator.generate(response);
//    }

    @GetMapping("/bonus/getkpi")
    public String getAllKPIBonuses(Model model,
                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo) {

        List<UserBonusKPIMain> bonusKPIList = getSafeKPIBonuses(dateFrom, dateTo);

        // Дополнительные расчеты для KPI вкладки
        Map<String, Double> kpiStats = calculateKPIStatistics(bonusKPIList);

        model.addAttribute("userBonusKPI", bonusKPIList);
        model.addAttribute("kpiStats", kpiStats);
        model.addAttribute("date1", dateFrom);
        model.addAttribute("date2", dateTo);
        return "bonusKPI :: bonusKPITab";
    }

    private Map<String, Double> calculateKPIStatistics(List<UserBonusKPIMain> kpiMainList) {
        Map<String, Double> stats = new HashMap<>();

        double totalBonus = kpiMainList.stream()
                .flatMap(main -> main.getUserBonusKPIDetails() != null ?
                        main.getUserBonusKPIDetails().stream() : Stream.empty())
                .mapToDouble(detail -> detail.getBonus() != null ? detail.getBonus() : 0.0)
                .sum();

        double totalBestBonus = kpiMainList.stream()
                .flatMap(main -> main.getUserBonusKPIDetails() != null ?
                        main.getUserBonusKPIDetails().stream() : Stream.empty())
                .mapToDouble(detail -> detail.getBestBonus() != null ? detail.getBestBonus() : 0.0)
                .sum();

        stats.put("totalBonus", totalBonus);
        stats.put("totalBestBonus", totalBestBonus);
        // Добавьте другие нужные статистики

        return stats;
    }
    @GetMapping("/bonus/add") //http://localhost:8181/userbonus/all1?date1=2021-12-01&date2=2021-12-31
    // @ApiOperation("Returns list of all products data transfer objects")
    public String addExtraBonus(Model model,
                                @RequestParam(value ="fio") String fio,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo
                               ){

        Employer employer = employerService.findByUserName(fio);
//        Employer employer = employerService.findByUserName("Полянская Евгения Львовна");
        List<ExtraAct> actList = actBonusPercentService.getAllExtraAct(dateFrom, dateTo, employer.getManId());
        model.addAttribute("actList", actList);
        model.addAttribute("employer", employer);
//        model.addAttribute("requestParam", httpServletRequest.getHeader("referer"));
        model.addAttribute("date1", dateFrom);
        model.addAttribute("date2", dateTo);
        System.out.println("Передаваемые атрибуты: " +
                "dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", fio=" + fio);
        return "extra-acts";
    }

    @GetMapping("/bonus/getstats")
    public ResponseEntity<Map<String, String>> getBonusStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo) {

        List<UserBonusMain> userBonusMains = getSafeUserBonuses(dateFrom, dateTo);
        List<UserBonusKPIMain> bonusKPIList = getSafeKPIBonuses(dateFrom, dateTo);

        double allBonusMoney = calculateTotalBonusMoney(userBonusMains);
        double allMoney = getSafeCompanyMoney(dateFrom, dateTo);
        double allKPIMoney = calculateTotalKPIMoney(bonusKPIList);

        DecimalFormat decimalFormatter = new DecimalFormat("#.##");
        NumberFormat currencyFormatter = createCurrencyFormatter();

        // Расчет процентов с защитой от деления на ноль
        double percentWithoutPKI = allMoney > 0 ? (allBonusMoney * 100 / allMoney) : 0;
        double percentWithPKI = allMoney > 0 ? ((allBonusMoney + allKPIMoney) * 100 / allMoney) : 0;

        Map<String, String> stats = new HashMap<>();
        stats.put("allMoney", currencyFormatter.format(allMoney));
        stats.put("allBonusMoney", currencyFormatter.format(allBonusMoney));
        stats.put("percentWithoutPKI", decimalFormatter.format(percentWithoutPKI));
        stats.put("percentWithPKI", decimalFormatter.format(percentWithPKI));

        return ResponseEntity.ok(stats);
    }


    @ExceptionHandler
    public ResponseEntity<?> handleException(UserBonusNotFoundException exc) {
        return new ResponseEntity<>(exc.getMessage(), HttpStatus.NOT_FOUND);
    }
}