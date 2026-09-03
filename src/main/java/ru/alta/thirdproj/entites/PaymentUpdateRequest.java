package ru.alta.thirdproj.entites;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class PaymentUpdateRequest {
    private Integer actId;
    private Integer employerId;
    private String candidate;
    private Integer projects;
    private Double bonus;
    private boolean paid;
    private String datePayment;
    private UUID paymentBuhId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate paymentRealDate; // Используем LocalDate для SQL Server
    // Проверка валидности запроса
//    public boolean isValid() {
//        return actId != null && employerId != null && bonus != null && paid != null;
//    }


    @Override
    public String toString() {
        return String.format(
                "PaymentUpdateRequest[actId=%d, employerId=%d, candidate=%s, bonus=%.2f, paid=%b, datePayment=%s, paymentRealDate=%s, paymentBuhId=%s]",
                actId, employerId, candidate, bonus, paid, datePayment, paymentRealDate, paymentBuhId
        );
    }
}
