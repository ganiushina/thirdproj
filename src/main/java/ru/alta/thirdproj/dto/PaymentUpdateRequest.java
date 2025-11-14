package ru.alta.thirdproj.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentUpdateRequest {

    @JsonProperty("actId")
    @JsonAlias({"act_id", "ActId"})
    private Long actId;

    @JsonProperty("paid")
    @JsonAlias({"is_paid", "checked"})
    private Boolean paid;

    @JsonProperty("paymentDate")
    @JsonAlias({"payment_date"})
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;

    public Long getActId() {
        return actId;
    }

    public void setActId(Long actId) {
        this.actId = actId;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
}
