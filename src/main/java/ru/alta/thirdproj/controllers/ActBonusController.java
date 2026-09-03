package ru.alta.thirdproj.controllers;


import com.google.gson.Gson;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.Employer;
import ru.alta.thirdproj.entites.EmployerNew;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.services.ActBonusPercentServiceImpl;
import ru.alta.thirdproj.services.EmployerServiceImpl;
import ru.alta.thirdproj.services.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

//@RestController
@Controller
@CrossOrigin("*")
//@RequestMapping("/act") //http://localhost:8181/userbonus/all?date1=2021-12-01&date2=2021-12-31
@Tag(name="ActBonusController", description="Дополнительные бонусы по актам")
public class ActBonusController {

    private static final Logger log = LoggerFactory.getLogger(ActBonusController.class);

    private ActBonusPercentServiceImpl actBonusPercentService;
    private EmployerServiceImpl employerService;
    private UserService userService;

  //  private String request;

    @Autowired
    public void setActBonusPercentService(ActBonusPercentServiceImpl actBonusPercentService){
        this.actBonusPercentService = actBonusPercentService;
    }

    @Autowired
    public void setEmployerService(EmployerServiceImpl employerService) {
        this.employerService = employerService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/act/allact")
    @ResponseBody
    public Map<String, Object> handleActs(
//    public ResponseEntity<Map<String, Object>> handleActs(
            @RequestParam(value = "actIds", required = false) List<Integer> actIds,
            @RequestParam(value = "percents", required = false) List<Double> percents,
            @RequestParam("fio") String fio,
            @RequestParam("request") String requestParam,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            Principal principal) {

//        Map<String, Object> response = new HashMap<>();
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userService.findByUserName(principal.getName());
            Employer employer = employerService.findByUserName(fio);

            // Логирование для отладки
            log.info("Processing acts for employer: {}, user: {}", fio, principal.getName());
            log.info("Received actIds: {}, percents: {}", actIds, percents);

            // Проверяем, что списки не null и имеют одинаковый размер
            if (actIds != null) {
                Map<Integer, Double> actPercentMap = new HashMap<>();

                for (int i = 0; i < actIds.size(); i++) {

                    percents.removeIf(value -> value == 0.0);
                    System.out.println("percents " + percents.toString());
                    if (percents.get(i) > 0) {
                        System.out.println("percents.get(i) " + percents.get(i));
                        actPercentMap.put(actIds.get(i), percents.get(i));
                    }
                    System.out.println("Передаваемые атрибуты5: " + actPercentMap.toString());
                }


                log.info("Processed actPercentMap: {}", actPercentMap);
                System.out.println("Processed actPercentMap: " + actPercentMap);

                // Сохраняем данные
                actBonusPercentService.saveActBonus(employer.getManId(), user.getUserId(), actPercentMap);

                // Добавляем данные для обновления
                response.put("success", true);
                response.put("fio", fio);
                response.put("updatedCount", actPercentMap.size());
            } else {
                response.put("success", false);
                response.put("message", "No data to process");
            }
        } catch (Exception e) {
            log.error("Error processing acts", e);
            response.put("success", false);
            response.put("message", e.getMessage());
        }

//                // Добавляем данные для обновления клиентской части
//                response.put("success", true);
//                response.put("message", "Данные успешно сохранены");
//                response.put("employerFio", fio);
//                response.put("updatedCount", actPercentMap.size());
//
//                return ResponseEntity.ok(response);
//            } else {
//                response.put("success", false);
//                return ResponseEntity.badRequest().body(response);
//            }
//        } catch (Exception e) {
//            log.error("Error processing acts", e);
//            response.put("success", false);
//            response.put("message", "Ошибка при обработке: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }

        return response;
    }

//    @PostMapping("/act/allact")
//    public String handleActs(
//            @RequestParam(value = "actIds", required = false) List<Integer> actIds,
//            @RequestParam(value = "percents", required = false) List<Double> percents,
//            @RequestParam("fio") String fio,
//            @RequestParam("request") String requestParam,
//            @RequestParam(value = "returnUrl", required = false) String returnUrl,
//            Principal principal) {
//
//        User user = userService.findByUserName(principal.getName());
//        Employer employer = employerService.findByUserName(fio);
//
//        System.out.println("Передаваемые атрибуты4: " + percents.toString() + "actIds " + actIds.toString()  + "requestParam = "  + requestParam);
//
//        // Проверяем, что списки не null и имеют одинаковый размер
//        if (actIds != null ) {
//            Map<Integer, Double> actPercentMap = new HashMap<>();
//
//            for (int i = 0; i < actIds.size(); i++) {
//
//                percents.removeIf(value -> value == 0.0);
//                System.out.println("percents " + percents.toString());
//                if (percents.get(i) > 0) {
//                    System.out.println("percents.get(i) " + percents.get(i));
//                    actPercentMap.put(actIds.get(i), percents.get(i));
//                }
//                System.out.println("Передаваемые атрибуты5: " + actPercentMap.toString());
//            }
//
//            actBonusPercentService.saveActBonus(employer.getManId(), user.getUserId(), actPercentMap);
//        }
//
//        // Если есть явный returnUrl, используем его
//        if (returnUrl != null) {
//            return "redirect:" + returnUrl;
//        }
//
//        // Иначе пробуем разобрать requestParam
//        if (requestParam != null) {
//            try {
//                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(requestParam);
//                String decodedReturnUrl = URLDecoder.decode(requestParam, StandardCharsets.UTF_8.toString());
//                System.out.println("Передаваемые атрибуты2: " +  "returnUrl=" + returnUrl );
//                return "redirect:" + decodedReturnUrl;
//            } catch (Exception e) {
//                System.err.println("Error processing request URL: " + e.getMessage());
//            }
//        }
//
//        // Если ничего не сработало, возвращаем на страницу бонусов
//        return "redirect:/bonusses";
//    }


}
