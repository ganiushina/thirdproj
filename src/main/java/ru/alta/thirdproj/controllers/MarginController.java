package ru.alta.thirdproj.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private UserSalaryServiceImpl userSalaryService;
    private MarginBonusServiceImpl marginBonusService;
    private UserSalesServiceImpl userSalesService;
    private UserService userService;
    private DepartmentService departmentService;
    private EmployeesService employeesService;

    private EmailUserSendConfiguration emailUserSendConfiguration;
    private EmailPaymentSuccessService emailPaymentSuccessService;

    @Autowired
    public void setUserSalaryService(UserSalaryServiceImpl userSalaryService, MarginBonusServiceImpl marginBonusService,
                                     UserSalesServiceImpl userSalesService, UserService userService,
                                     EmailUserSendConfiguration emailUserSendConfiguration,
                                     EmailPaymentSuccessService emailPaymentSuccessService,
                                     DepartmentService departmentService,
                                     EmployeesService employeesService){
        this.userSalaryService = userSalaryService;
        this.marginBonusService = marginBonusService;
        this.userSalesService = userSalesService;
        this.userService = userService;
        this.emailUserSendConfiguration = emailUserSendConfiguration;
        this.emailPaymentSuccessService = emailPaymentSuccessService;
        this.departmentService = departmentService;
        this.employeesService = employeesService;
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
        List<MarginBonus> marginBonuses = userSalesService.getMarginBonusByMonth(dateFrom, dateTo);
        List<MarginBonusBDM> marginBonusList = marginBonusService.getAllMarginBonus(dateFrom, dateTo);

        Double totalMargin = marginBonuses.stream()
                .mapToDouble(bonus -> bonus.getMargin() != null ? bonus.getMargin() : 0.0)
                .sum();

        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        model.addAttribute("marginBonusByMonth", marginBonuses);
        model.addAttribute("allMargin", formatter.format(totalMargin) + " ₽");
        model.addAttribute("departmentMargins", getFormattedMargins(marginBonusList));
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);

        return "summary :: summaryTab";
    }

    @GetMapping("/margin/detailed")
    public String getDetailedReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            Model model) {

        processDateRange(dateFrom, dateTo, model);
        List<MarginBonusBDM> marginBonusList = marginBonusService.getAllMarginBonus(dateFrom, dateTo);

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

        model.addAttribute("marginBonusList", marginBonusList);
        model.addAttribute("totalMargin", formatter.format(allMargin) + " ₽");
        model.addAttribute("departmentMargins", getFormattedMargins(marginBonusList));

        return "details :: detailsTab";
    }


    @GetMapping("/margin/charts")
    public String getChartData(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            Principal principal, Model model) {

        processDateRange(dateFrom, dateTo, model);
        User user = userService.findByUserName(principal.getName());

        int userDepartment = user.getLoginDepartment();


        if ((userDepartment == 4) && (user.getUserPosition().equals("7") )) {
            userDepartment = 0;
        }

        List<UserSalary> userSalaryList = marginBonusService.getUserSalary(dateFrom,dateTo, userDepartment);
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
    @GetMapping("/margin/userAct")
    public String getUserAct(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            Model model) {

        processDateRange(dateFrom, dateTo, model);

        // Получаем данные о выплатах
        List<ActByUserCheck> actByUserList = marginBonusService.getActByUserCheck(dateFrom, dateTo);
        // Группируем данные по номеру акта
        Map<String, List<ActByUserCheck>> groupedActs = actByUserList != null ?
                actByUserList.stream()
                        .collect(Collectors.groupingBy(ActByUserCheck::getActNum)) :
                new HashMap<>();
        if (groupedActs != null) {
            groupedActs.values().forEach(this::hideDuplicateResearchers);
        }

        model.addAttribute("groupedActs", groupedActs);
        model.addAttribute("actByUserList", actByUserList != null ? actByUserList : Collections.emptyList());

        return "actByUserCheck :: actByUserTab";
    }

    @GetMapping("/userAct/departments")
    @ResponseBody
    public List<Department> getDepartments() {
        return departmentService.getDepartments();
    }

    @GetMapping("/userAct/employees")
    @ResponseBody
    public List<Employees> getEmployees() {
        return employeesService.getActiveEmployeesForPlanMonth();
    }

    @PostMapping("/userAct/update")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateFailedProbationAct(
            @RequestBody JsonNode payload) {
        List<ActByUserCheck> participants = parseParticipants(payload);
        ActByUserCheck base = participants != null && !participants.isEmpty() ? participants.get(0) : null;
        Integer actId = base != null ? base.getActId() : null;

        log.info("[UpdateAct] Received update request: actId={}, totalNoNds={}, candidate={}, participants={}",
                actId,
                base != null ? base.getTotalNoNds() : null,
                base != null ? base.getCandidate() : null,
                participants != null ? participants.size() : 0);

        try {
            if (participants == null || participants.isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "participants are required"));
            }

            if (base == null || base.getActId() == 0) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "actId is required"));
            }

            List<ActByUserCheck> filtered = participants.stream()
                    .filter(p -> (p.getResponsibleUserName() != null && !p.getResponsibleUserName().isBlank())
                            || (p.getResecherName() != null && !p.getResecherName().isBlank()))
                    .collect(Collectors.toList());

            marginBonusService.saveFailedProbationAct(actId, base.getTotalNoNds(),
                    base.getCandidate(), filtered);
            log.info("[UpdateAct] Update finished successfully for actId={}", actId);

            return ResponseEntity.ok(Collections.singletonMap("status", "updated"));
        } catch (Exception e) {
            log.error("[UpdateAct] Failed to update act {}", actId, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "Ошибка при сохранении: " + e.getMessage()));
        }
    }

    private List<ActByUserCheck> parseParticipants(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return Collections.emptyList();
        }

        if (payload.isArray()) {
            return convertArray((ArrayNode) payload, null);
        }

        ActByUserCheck base = OBJECT_MAPPER.convertValue(payload, ActByUserCheck.class);
        JsonNode participantsNode = payload.get("participants");
        if (participantsNode != null && participantsNode.isArray()) {
            List<ActByUserCheck> participants = convertArray((ArrayNode) participantsNode, base);
            if (!participants.isEmpty()) {
                return participants;
            }
        }

        return Collections.singletonList(base);
    }

    private List<ActByUserCheck> convertArray(ArrayNode node, ActByUserCheck base) {
        List<ActByUserCheck> result = new ArrayList<>();
        for (JsonNode item : node) {
            ActByUserCheck participant = OBJECT_MAPPER.convertValue(item, ActByUserCheck.class);
            if (base != null) {
                mergeActFields(participant, base);
            }
            result.add(participant);
        }
        return result;
    }

    private void mergeActFields(ActByUserCheck target, ActByUserCheck base) {
        if (target.getActId() == 0 && base.getActId() != 0) {
            target.setActId(base.getActId());
        }
        if (target.getTotalNoNds() == null) {
            target.setTotalNoNds(base.getTotalNoNds());
        }
        if (target.getCandidate() == null) {
            target.setCandidate(base.getCandidate());
        }
        if (target.getDepartmentName() == null) {
            target.setDepartmentName(base.getDepartmentName());
        }
        if (target.getCandidatePercent() == null) {
            target.setCandidatePercent(base.getCandidatePercent());
        }
        if (target.getResecherPercent() == null) {
            target.setResecherPercent(base.getResecherPercent());
        }
        if (target.getSummResponsibleUser() == null) {
            target.setSummResponsibleUser(base.getSummResponsibleUser());
        }
        if (target.getSummResecher() == null) {
            target.setSummResecher(base.getSummResecher());
        }
        if (target.getResecherDepartmentName() == null) {
            target.setResecherDepartmentName(base.getResecherDepartmentName());
        }
    }


    private void hideDuplicateResearchers(List<ActByUserCheck> acts) {
        // Ключ: ресечер + его департамент + сумма
        Set<String> seenResearchers = new HashSet<>();

        for (ActByUserCheck act : acts) {
            String name = act.getResecherName();
            if (name == null || name.isBlank()) {
                continue; // нечего обрабатывать
            }

            String key = name + "|" +
                    Objects.toString(act.getResecherDepartmentName(), "") + "|" +
                    Objects.toString(act.getSummResecher(), "");

            if (seenResearchers.contains(key)) {
                // Повтор — очищаем поля, чтобы не дублировались в таблице
                act.setResecherName(null);
                act.setResecherDepartmentName(null);
                act.setSummResecher(null);
            } else {
                seenResearchers.add(key);
            }
        }
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

    private Double safeParseDouble(Object value) {
        if (value == null) return 0.0;
        try {
            String strValue = value.toString()
                    .replaceAll("[^\\d.,-]", "")
                    .replace(',', '.');
            return Double.parseDouble(strValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }



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

    private Map<String, String> getFormattedMargins(List<MarginBonusBDM> marginBonusList) {
        if (marginBonusList == null || marginBonusList.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> departmentMargins = marginBonusList.stream()
                .collect(Collectors.groupingBy(
                        MarginBonusBDM::getDepartmentName,
                        Collectors.summingDouble(d -> d.getMarginDepartmentSum().stream()
                                .mapToDouble(this::safeParseDouble)
                                .sum())
                ));

        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        return departmentMargins.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> formatter.format(e.getValue()).replace("\u00A0", " ") + " ₽"
                ));
    }


}

