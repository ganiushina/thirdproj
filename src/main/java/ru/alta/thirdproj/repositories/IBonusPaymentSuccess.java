package ru.alta.thirdproj.repositories;

import ru.alta.thirdproj.entites.PaymentSuccess;

import java.util.Date;
import java.util.List;

public interface IBonusPaymentSuccess {

    void save(PaymentSuccess paymentSuccess);
    PaymentSuccess findOneByAct(int userId, int actId, String candidate, Double summ);
    void updatePayment(int userId, int employerId, Date paymentDate, Double paymentRealSum, int actId, String candidate);
    void deletePayment(int employerId, Double paymentRealSum, int actId, String candidate);
    void deletePaymentAll();

}
