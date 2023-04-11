package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.alta.thirdproj.entites.PaymentSuccess;

import java.util.Date;

@Component
public class BonusPaymentSuccessRepositoryImpl implements IBonusPaymentSuccess {

    private final Sql2o sql2o;


    private static final String SELECT_BONUS_PAYMENT_QUERY
            = "insert INTO paymentSuccess values (:user_id ,\n" +
            ":employer_id ,\n" +
            ":payment_date ,\n" +
            ":payment_summ, " +
            ":act_id ,\n" +
            ":candidate ,\n" +
            ":project_id,\n" +
            ":payment_real_summ," +
            ":month_kpi," +
            ":payment_type)";
    private static final String SELECT_ID_BONUS_PAYMENT
            = "SELECT user_id, employer_id, payment_date, payment_summ, act_id, candidate, project_id, payment_real_summ " +
            "FROM paymentSuccess ps WHERE ps.user_id = :user_id and ps.act_id = :act_id and ps.candidate = :candidate and ps.payment_summ = :payment_summ";


    private static final String UPDATE_BONUS_PAYMENT =
    "UPDATE [dbo].[paymentSuccess]  SET  employer_id = :employer_id,  payment_date = :payment_date ,payment_real_summ = :payment_real_summ" +
            "  WHERE ps.user_id = :user_id and ps.act_id = :act_id and ps.candidate = :candidate";

    private static final String DELETE_BONUS_PAYMENT =
            "DELETE paymentSuccess WHERE employer_id = :user_id and act_id = :act_id and candidate = :candidate and payment_summ = :payment_summ";


    private static final String DELETE_BONUS_PAYMENT_KPI = "DELETE paymentSuccess WHERE employer_id = :user_id and candidate = :candidate \n" +
            "\t\t\tand payment_summ = :payment_summ";


    private static final String DELETE_BONUS_PAYMENT_ALL =
            "DELETE paymentSuccess";

    public BonusPaymentSuccessRepositoryImpl(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }


    @Override
    @Transactional
    public void save(PaymentSuccess paymentSuccess)
    {
        try (Connection connection = sql2o.open()) {
             connection.createQuery(SELECT_BONUS_PAYMENT_QUERY, false)
                    .addParameter("user_id", paymentSuccess.getUserId())
                    .addParameter("employer_id", paymentSuccess.getEmployerId() )
                    .addParameter("payment_date", paymentSuccess.getPaymentDate())
                    .addParameter("payment_summ", paymentSuccess.getPaymentSum())
                    .addParameter("act_id", paymentSuccess.getActId())
                    .addParameter("candidate", paymentSuccess.getCandidate())
                    .addParameter("project_id", paymentSuccess.getProjectId())
                    .addParameter("payment_real_summ", paymentSuccess.getPaymentRealSum())
                    .addParameter("month_kpi", paymentSuccess.getMonthKPI())
                    .addParameter("payment_type", paymentSuccess.getType())
                    .executeUpdate();
            connection.commit();

        }
    }

    @Override
    public PaymentSuccess findOneByAct(int userId, int actId, String candidate, Double summ) {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_ID_BONUS_PAYMENT)
                    .throwOnMappingFailure(false)
                    .addParameter("user_id", userId)
                    .addParameter("act_id", actId)
                    .addParameter("candidate", candidate)
                    .addParameter("payment_summ", summ)
                    .setColumnMappings(PaymentSuccess.COLUMN_MAPPINGS)
                    .executeAndFetchFirst(PaymentSuccess.class);
        }
    }



    @Transactional
    @Override
    public void deletePayment(int employerId, Double paymentSum, int actId, String candidate) {
        try (Connection connection = sql2o.open()) {
            connection.createQuery(DELETE_BONUS_PAYMENT, false)
                    .addParameter("user_id", employerId)
                    .addParameter("payment_summ", paymentSum)
                    .addParameter("act_id", actId)
                    .addParameter("candidate", candidate)
                    .executeUpdate();
            connection.commit();

        }
    }

    @Transactional
    @Override
    public void deletePaymentKPI(int employerId, Double paymentSum, String candidate) {
        try (Connection connection = sql2o.open()) {
            connection.createQuery(DELETE_BONUS_PAYMENT_KPI, false)
                    .addParameter("user_id", employerId)
                    .addParameter("payment_summ", paymentSum)
                    .addParameter("candidate", candidate)
                    .executeUpdate();
            connection.commit();

        }
    }

    @Transactional
    @Override
    public void deletePaymentAll() {
        try (Connection connection = sql2o.open()) {
            connection.createQuery(DELETE_BONUS_PAYMENT_ALL, false)
                    .executeUpdate();
            connection.commit();
        }
    }


    @Transactional
    @Override
    public void updatePayment(int userId, int employerId, Date paymentDate, Double paymentRealSum, int actId, String candidate)
    {
        try (Connection connection = sql2o.open()) {
            connection.createQuery(UPDATE_BONUS_PAYMENT, false)
                    .addParameter("user_id", userId)
                    .addParameter("employer_id", employerId)
                    .addParameter("payment_date", paymentDate)
                    .addParameter("payment_real_summ", paymentRealSum)
                    .addParameter("act_id", actId)
                    .addParameter("candidate", candidate)
                    .executeUpdate();
            connection.commit();

        }
    }
}
