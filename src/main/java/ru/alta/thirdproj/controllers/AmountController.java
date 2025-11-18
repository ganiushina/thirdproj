package ru.alta.thirdproj.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alta.thirdproj.dto.PaymentUpdateRequest;
import ru.alta.thirdproj.services.UserBonusServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
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
    private final ObjectMapper objectMapper;

    public AmountController(UserBonusServiceImpl userBonusService, ObjectMapper objectMapper) {
        this.userBonusService = userBonusService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/updatePaymentStatus")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(HttpServletRequest request) {
        if (request != null) {
            // Force Servlet containers to parse form parameters before we touch the input stream.
            request.getParameterMap();
        }

        PaymentUpdateRequest payload = readBody(request);

        mergeParameters(payload, request);

        Long actId = payload.getActId();
        if (actId == null) {
            logger.warn("Запрос на обновление выплаты не содержит идентификатора акта. Параметры: {}", request != null ? request.getParameterMap() : null);
            return ResponseEntity.badRequest().build();
        }

        boolean paid = Boolean.TRUE.equals(payload.getPaid());
        LocalDate paymentDate = paid ? payload.getPaymentDate() : null;
        if (paid && paymentDate == null) {
            paymentDate = LocalDate.now();
        }

        logger.info("Обновление статуса выплаты: actId={}, paid={}, paymentDate={}", actId, paid, paymentDate);

        userBonusService.updatePaymentStatus(actId, paid, paymentDate);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("actId", actId);
        response.put("paid", paid);
        response.put("paymentDate", paymentDate);

        return ResponseEntity.ok(response);
    }

    private PaymentUpdateRequest readBody(HttpServletRequest request) {
        if (request == null) {
            return new PaymentUpdateRequest();
        }

        try {
            MediaType mediaType = resolveMediaType(request.getContentType());
            boolean likelyJson = mediaType != null && MediaType.APPLICATION_JSON.includes(mediaType);
            boolean hasFormParameters = request.getParameterMap() != null && !request.getParameterMap().isEmpty();

            if (!likelyJson && hasFormParameters) {
                return new PaymentUpdateRequest();
            }

            String rawBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8).trim();
            if (!StringUtils.hasText(rawBody)) {
                return new PaymentUpdateRequest();
            }

            if (likelyJson || rawBody.startsWith("{") || rawBody.startsWith("[")) {
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

    private void mergeParameters(PaymentUpdateRequest target, HttpServletRequest request) {
        if (target == null || request == null) {
            return;
        }

        if (target.getActId() == null) {
            readFirst(request, "actId", "act_id", "ActId", "id")
                    .flatMap(this::parseLongSafely)
                    .ifPresent(target::setActId);
        }

        if (target.getPaid() == null) {
            readFirst(request, "paid", "is_paid", "checked", "value")
                    .flatMap(this::parseBooleanSafely)
                    .ifPresent(target::setPaid);
        }

        if (target.getPaymentDate() == null) {
            readFirst(request, "paymentDate", "payment_date", "paymentRealDate", "payment_real_date", "date")
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

    private Optional<String> readFirst(HttpServletRequest request, String... keys) {
        if (request == null || keys == null) {
            return Optional.empty();
        }
        for (String key : keys) {
            String[] values = request.getParameterValues(key);
            if (values != null) {
                for (String value : values) {
                    if (StringUtils.hasText(value)) {
                        return Optional.of(value);
                    }
                }
            }

            Object attribute = request.getAttribute(key);
            if (attribute != null) {
                String attributeValue = attribute.toString();
                if (StringUtils.hasText(attributeValue)) {
                    return Optional.of(attributeValue);
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
