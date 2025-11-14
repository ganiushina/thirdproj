package ru.alta.thirdproj.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.alta.thirdproj.controllers.dto.PaymentUpdateRequest;
import ru.alta.thirdproj.services.UserBonusServiceImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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

    @PostMapping(value = "/updatePaymentStatus", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updatePaymentStatusJson(@RequestBody PaymentUpdateRequest request) {
        return handleUpdate(request, Map.of());
    }

    @PostMapping(value = "/updatePaymentStatus", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> updatePaymentStatusForm(PaymentUpdateRequest request,
                                                        @RequestParam Map<String, String> rawParams) {
        return handleUpdate(request, rawParams);
    }

    @PostMapping(value = "/updatePaymentStatus", headers = "!Content-Type")
    public ResponseEntity<Void> updatePaymentStatusWithoutContentType(PaymentUpdateRequest request,
                                                                      @RequestParam Map<String, String> rawParams) {
        return handleUpdate(request, rawParams);
    }

    private ResponseEntity<Void> handleUpdate(PaymentUpdateRequest request, Map<String, String> rawParams) {
        PaymentUpdateRequest effectiveRequest = mergeRequest(request, rawParams);

        logger.info("Received payment update request: {}", effectiveRequest);

        if (effectiveRequest.getActId() == null) {
            logger.warn("Payment update request is missing act identifier. Raw params: {}", rawParams);
            return ResponseEntity.badRequest().build();
        }

        boolean paid = Boolean.TRUE.equals(effectiveRequest.getPaid());
        LocalDate paymentDate = paid ? effectiveRequest.getPaymentDate() : null;

        userBonusService.updatePaymentStatus(effectiveRequest.getActId(), paid, paymentDate);

        return ResponseEntity.ok().build();
    }

    private PaymentUpdateRequest mergeRequest(PaymentUpdateRequest original, Map<String, String> rawParams) {
        PaymentUpdateRequest effective = Optional.ofNullable(original).orElseGet(PaymentUpdateRequest::new);

        if (effective.getActId() == null) {
            extractFirstPresent(rawParams, "actId", "act_id")
                    .flatMap(this::parseLongSafely)
                    .ifPresent(effective::setActId);
        }

        if (effective.getPaid() == null) {
            extractFirstPresent(rawParams, "paid", "is_paid")
                    .flatMap(this::parseBooleanSafely)
                    .ifPresent(effective::setPaid);
        }

        if (effective.getPaymentDate() == null) {
            extractFirstPresent(rawParams, "paymentDate", "payment_date")
                    .flatMap(this::parseDateSafely)
                    .ifPresent(effective::setPaymentDate);
        }

        return effective;
    }

    private Optional<String> extractFirstPresent(Map<String, String> rawParams, String... keys) {
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

    private Optional<Boolean> parseBooleanSafely(String candidate) {
        String normalized = candidate.toLowerCase(Locale.ROOT);
        if (normalized.equals("1") || normalized.equals("true") || normalized.equals("yes")) {
            return Optional.of(Boolean.TRUE);
        }
        if (normalized.equals("0") || normalized.equals("false") || normalized.equals("no")) {
            return Optional.of(Boolean.FALSE);
        }
        logger.warn("Unable to parse payment flag from '{}'", candidate);
        return Optional.empty();
    }

    private Optional<LocalDate> parseDateSafely(String candidate) {
        List<DateTimeFormatter> formatters = new ArrayList<>();
        formatters.add(ISO_DATE_FORMATTER);
        formatters.add(RUS_DATE_FORMATTER);

        for (DateTimeFormatter formatter : formatters) {
            try {
                return Optional.of(LocalDate.parse(candidate, formatter));
            } catch (DateTimeParseException ignored) {
                // try the next formatter
            }
        }

        logger.warn("Unable to parse payment date from '{}'", candidate);
        return Optional.empty();
    }
}
