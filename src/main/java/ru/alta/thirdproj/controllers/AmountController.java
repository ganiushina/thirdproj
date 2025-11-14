package ru.alta.thirdproj.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.alta.thirdproj.services.UserBonusServiceImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/amount")
public class AmountController {

    private static final Logger logger = LoggerFactory.getLogger(AmountController.class);

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter RUS_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final UserBonusServiceImpl userBonusService;

    public AmountController(UserBonusServiceImpl userBonusService) {
        this.userBonusService = userBonusService;
    }

    @PostMapping("/updatePaymentStatus")
    public ResponseEntity<Void> updatePaymentStatus(@RequestParam Map<String, String> rawParams) {
        Long actId = extractActId(rawParams);
        if (actId == null) {
            logger.warn("Payment update request is missing act identifier. Raw params: {}", rawParams);
            return ResponseEntity.badRequest().build();
        }

        boolean paid = extractPaid(rawParams);
        LocalDate paymentDate = paid ? extractPaymentDate(rawParams).orElse(null) : null;

        logger.info("Updating payment status: actId={}, paid={}, paymentDate={}", actId, paid, paymentDate);

        userBonusService.updatePaymentStatus(actId, paid, paymentDate);
        return ResponseEntity.ok().build();
    }

    private Long extractActId(Map<String, String> rawParams) {
        return readFirst(rawParams, "actId", "act_id")
                .flatMap(this::parseLongSafely)
                .orElse(null);
    }

    private boolean extractPaid(Map<String, String> rawParams) {
        return readFirst(rawParams, "paid", "is_paid")
                .map(value -> {
                    String normalized = value.trim().toLowerCase(Locale.ROOT);
                    return normalized.equals("1")
                            || normalized.equals("true")
                            || normalized.equals("on")
                            || normalized.equals("yes");
                })
                .orElse(false);
    }

    private Optional<LocalDate> extractPaymentDate(Map<String, String> rawParams) {
        return readFirst(rawParams, "paymentDate", "payment_date")
                .flatMap(this::parseDateSafely);
    }

    private Optional<String> readFirst(Map<String, String> rawParams, String... keys) {
        if (CollectionUtils.isEmpty(rawParams) || keys == null) {
            return Optional.empty();
        }
        for (String key : keys) {
            if (rawParams.containsKey(key)) {
                String value = rawParams.get(key);
                if (value != null && !value.isBlank()) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Long> parseLongSafely(String candidate) {
        try {
            return Optional.of(Long.parseLong(candidate));
        } catch (NumberFormatException ex) {
            logger.warn("Unable to parse act id from '{}'", candidate, ex);
            return Optional.empty();
        }
    }

    private Optional<LocalDate> parseDateSafely(String candidate) {
        try {
            return Optional.of(LocalDate.parse(candidate, ISO_DATE_FORMATTER));
        } catch (DateTimeParseException ignored) {
            // try another format
        }

        try {
            return Optional.of(LocalDate.parse(candidate, RUS_DATE_FORMATTER));
        } catch (DateTimeParseException ex) {
            logger.warn("Unable to parse payment date from '{}'", candidate, ex);
            return Optional.empty();
        }
    }
}
