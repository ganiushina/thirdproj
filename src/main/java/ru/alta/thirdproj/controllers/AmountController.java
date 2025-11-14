package ru.alta.thirdproj.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.alta.thirdproj.dto.PaymentUpdateRequest;
import ru.alta.thirdproj.services.UserBonusServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/amount")
public class AmountController {

    private static final Logger logger = LoggerFactory.getLogger(AmountController.class);

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter RUS_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final UserBonusServiceImpl userBonusService;
    private final ObjectMapper objectMapper;

    public AmountController(UserBonusServiceImpl userBonusService, ObjectMapper objectMapper) {
        this.userBonusService = userBonusService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/updatePaymentStatus")
    public ResponseEntity<Void> updatePaymentStatus(HttpServletRequest request,
                                                    @RequestParam MultiValueMap<String, String> rawParams) {
        PaymentUpdateRequest payload = readBody(request);
        mergeParameters(payload, rawParams);

        Long actId = payload.getActId();
        if (actId == null) {
            logger.warn("Запрос на обновление выплаты не содержит идентификатора акта. Параметры: {}", rawParams);
            return ResponseEntity.badRequest().build();
        }

        boolean paid = Boolean.TRUE.equals(payload.getPaid());
        LocalDate paymentDate = paid ? payload.getPaymentDate() : null;

        logger.info("Обновление статуса выплаты: actId={}, paid={}, paymentDate={}", actId, paid, paymentDate);

        userBonusService.updatePaymentStatus(actId, paid, paymentDate);
        return ResponseEntity.ok().build();
    }

    private PaymentUpdateRequest readBody(HttpServletRequest request) {
        if (request == null) {
            return new PaymentUpdateRequest();
        }

        try {
            String rawBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            if (!StringUtils.hasText(rawBody)) {
                return new PaymentUpdateRequest();
            }

            MediaType mediaType = resolveMediaType(request.getContentType());
            if (mediaType == null || MediaType.APPLICATION_JSON.includes(mediaType) || rawBody.trim().startsWith("{")) {
                try {
                    return objectMapper.readValue(rawBody, PaymentUpdateRequest.class);
                } catch (IOException ex) {
                    logger.warn("Не удалось распарсить тело запроса как JSON: {}", rawBody, ex);
                }
            }
        } catch (IOException ex) {
            logger.warn("Ошибка чтения тела запроса при обновлении выплаты", ex);
        }

        return new PaymentUpdateRequest();
    }

    private void mergeParameters(PaymentUpdateRequest target, MultiValueMap<String, String> rawParams) {
        if (target == null) {
            return;
        }

        if (target.getActId() == null) {
            readFirst(rawParams, "actId", "act_id", "ActId")
                    .flatMap(this::parseLongSafely)
                    .ifPresent(target::setActId);
        }

        if (target.getPaid() == null) {
            readFirst(rawParams, "paid", "is_paid", "checked")
                    .flatMap(this::parseBooleanSafely)
                    .ifPresent(target::setPaid);
        }

        if (target.getPaymentDate() == null) {
            readFirst(rawParams, "paymentDate", "payment_date")
                    .flatMap(this::parseDateSafely)
                    .ifPresent(target::setPaymentDate);
        }
    }

    private MediaType resolveMediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return null;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException ex) {
            logger.debug("Некорректный content-type '{}': {}", contentType, ex.getMessage());
            return null;
        }
    }

    private Optional<String> readFirst(MultiValueMap<String, String> rawParams, String... keys) {
        if (CollectionUtils.isEmpty(rawParams) || keys == null) {
            return Optional.empty();
        }
        for (String key : keys) {
            String value = rawParams.getFirst(key);
            if (StringUtils.hasText(value)) {
                return Optional.of(value);
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

    private Optional<Boolean> parseBooleanSafely(String value) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "1":
            case "true":
            case "on":
            case "yes":
            case "y":
            case "checked":
                return Optional.of(Boolean.TRUE);
            case "0":
            case "false":
            case "off":
            case "no":
            case "n":
                return Optional.of(Boolean.FALSE);
            default:
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
