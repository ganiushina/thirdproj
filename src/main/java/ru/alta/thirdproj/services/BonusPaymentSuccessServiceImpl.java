package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.EmployerNew;
import ru.alta.thirdproj.entites.PaymentSuccess;
import ru.alta.thirdproj.repositories.IBonusPaymentSuccess;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BonusPaymentSuccessServiceImpl {

    private IBonusPaymentSuccess bonusPaymentSuccess;

    @Autowired
    public void setBonusPaymentSuccess(IBonusPaymentSuccess bonusPaymentSuccess){
        this.bonusPaymentSuccess = bonusPaymentSuccess;
    }


    public boolean addPayment(int userId, int employerId, Double paymentSum, int actId, String candidate,
                              int projectId, int monthKPI, int type, UUID paymentBuhId){

        PaymentSuccess paymentSuccess = new PaymentSuccess();
//        if (!findByActId(userId, actId, candidate, paymentSum).isEmpty()) {
//            return false;
//        }

        paymentSuccess.setActId(actId);
        paymentSuccess.setCandidate(candidate);
        paymentSuccess.setEmployerId(employerId);
        paymentSuccess.setPaymentDate(new Date());
        paymentSuccess.setPaymentSum(paymentSum);
        paymentSuccess.setPaymentRealSum(paymentSum);
        paymentSuccess.setUserId(userId);
        paymentSuccess.setProjectId(projectId);
        paymentSuccess.setMonthKPI(monthKPI);
        paymentSuccess.setType(type);
        paymentSuccess.setPaymentBuhId(paymentBuhId);
        bonusPaymentSuccess.save(paymentSuccess);

        return true;
    }

    public Optional<PaymentSuccess> findByActId(int userId, int actId, String candidate, Double summ){
        return Optional.ofNullable(bonusPaymentSuccess.findOneByAct(userId, actId, candidate, summ));

    }

    public List<EmployerNew> findByActIdInList(int userId, int actId, String candidate,
                                               Double summ, List<EmployerNew> employerNewList) {

        Iterator<EmployerNew> employerNewIterator = employerNewList.iterator();
    //    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("MMMMM");

        boolean brk = false;


        while (employerNewIterator.hasNext()) {

            EmployerNew nextEmployerNew = employerNewIterator.next();

            Iterator<Act> actIterator = nextEmployerNew.getActList().iterator();

                while (actIterator.hasNext()) {
                    Act actNext = actIterator.next();
                    String month = actNext.getDatePayment();

                  //  if (actId != 0) {
                        if (nextEmployerNew.getManId() == userId
                            && actNext.getId() == actId
                            && actNext.getCandidate().equals(candidate)
                            && actNext.getBonus().equals(summ)) {
                        actIterator.remove();
                            brk = true;
                        break;
                    }
                }


                if (nextEmployerNew.getActList().size() == 0) {
                    employerNewIterator.remove();
                }
            if (brk == true) break;

        }
        return employerNewList;
    }


    public void updatePayment(int userId, int employerId, Date paymentDate, Double paymentRealSum, int actId, String candidate, Double summ){

        if (findByActId(userId, actId, candidate, summ) != null) {
            bonusPaymentSuccess.updatePayment(userId, employerId, paymentDate, paymentRealSum, actId, candidate);
        }
    }

    public void deletePayment(int userId, int employerId, LocalDate paymentDate, Double paymentRealSum, int actId, String candidate, Double summ, UUID paymentBuhId){

        if (findByActId(userId, actId, candidate, summ) != null) {
            bonusPaymentSuccess.deletePayment(employerId, paymentRealSum, actId, candidate, paymentBuhId);
        }
    }

    public List<EmployerNew> findPaidByActId(List<EmployerNew> employerList) {

        Iterator<EmployerNew> employerNewIterator = employerList.iterator();//создаем итератор

        while(employerNewIterator.hasNext()) {//до тех пор, пока в списке есть элементы

            EmployerNew nextEmployerNew = employerNewIterator.next();//получаем следующий элемент

            Iterator<Act> actIterator = nextEmployerNew.getActList().iterator();//создаем итератор

            while (actIterator.hasNext()) {
                Act actNext = actIterator.next();
                if (actNext.getPaid() == 0) { // удаляем только неоплаченные
                    actIterator.remove();
                }
            }

//            while (actIterator.hasNext()){
//                Act actNext = actIterator.next();
//                if (!actNext.isPaid()){
//                    actIterator.remove();
//                }
//            }
            if (nextEmployerNew.getActList().size() == 0){
                employerNewIterator.remove();
            }
        }

        return employerList;
    }

    public int findCountActId(List<EmployerNew> employerList) {

        int cnt = 0;

        Iterator<EmployerNew> employerNewIterator = employerList.iterator();//создаем итератор

        while(employerNewIterator.hasNext()) {//до тех пор, пока в списке есть элементы

            EmployerNew nextEmployerNew = employerNewIterator.next();//получаем следующий элемент

            Iterator<Act> actIterator = nextEmployerNew.getActList().iterator();//создаем итератор

            cnt += nextEmployerNew.getActList().size();

        }

        return cnt;
    }


}


