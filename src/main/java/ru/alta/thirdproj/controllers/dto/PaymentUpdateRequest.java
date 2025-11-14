package ru.alta.thirdproj.controllers.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentUpdateRequest {

    @JsonAlias({"actId", "act_id"})
    private Long actId;

    @JsonAlias({"paid", "is_paid"})
    private Boolean paid;

    @JsonAlias({"paymentDate", "payment_date"})
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate paymentDate;

    public void setAct_id(Long actId) {
        this.actId = actId;
    }

    public void setIs_paid(Boolean paid) {
        this.paid = paid;
    }

    public void setPayment_date(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
}
