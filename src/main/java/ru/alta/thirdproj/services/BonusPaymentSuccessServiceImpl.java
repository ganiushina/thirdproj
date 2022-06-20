package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.EmployerNew;
import ru.alta.thirdproj.entites.PaymentSuccess;
import ru.alta.thirdproj.repositories.IBonusPaymentSuccess;

import java.util.*;

@Service
public class BonusPaymentSuccessServiceImpl {

    private IBonusPaymentSuccess bonusPaymentSuccess;

    @Autowired
    public void setBonusPaymentSuccess(IBonusPaymentSuccess bonusPaymentSuccess){
        this.bonusPaymentSuccess = bonusPaymentSuccess;
    }


    public boolean addPayment(int userId, int employerId, Double paymentSum, int actId, String candidate, int projectId){

        PaymentSuccess paymentSuccess = new PaymentSuccess();

        if (!findByActId(userId, actId, candidate, paymentSum).isEmpty()) {
            return false;
        }

        paymentSuccess.setActId(actId);
        paymentSuccess.setCandidate(candidate);
        paymentSuccess.setEmployerId(employerId);
        paymentSuccess.setPaymentDate(new Date());
        paymentSuccess.setPaymentSum(paymentSum);
        paymentSuccess.setPaymentRealSum(paymentSum);
        paymentSuccess.setUserId(userId);
        paymentSuccess.setProjectId(projectId);

        bonusPaymentSuccess.save(paymentSuccess);

        return true;
    }

    public Optional<PaymentSuccess> findByActId(int userId, int actId, String candidate, Double summ){
        return Optional.ofNullable(bonusPaymentSuccess.findOneByAct(userId, actId, candidate, summ));

    }

    public void findEmployer(String val, int userId, List<EmployerNew> employerNewList) {


       List<EmployerNew> paidList = findPaidByActId(employerNewList);


        if (val != null) {

            List<String> arrOfEmployerBonus = Arrays.asList(val.split(","));

            for (int k = 0; k < arrOfEmployerBonus.size(); k++) {

                int manId ;
                Double paymentSum ;
                int actId ;
                String candidateName ;
                int projectId ;
                int size;

                String source = arrOfEmployerBonus.get(k);
                paymentSum = Double.valueOf(source.substring(0, source.indexOf("/")));
                source = source.replace(paymentSum + "/", "");
                manId = Integer.parseInt(source.substring(0, source.indexOf("/")));
                source = source.replace(manId + "/", "");
                actId = Integer.parseInt(source.substring(0, source.indexOf("/")));
                source = source.replace(actId + "/", "");
                candidateName = source.substring(0, source.indexOf("/"));
                source = source.replace(candidateName + "/", "");
                projectId = Integer.parseInt(source.substring(0, source.length() - 1));
                size = paidList.size();
                paidList = findByActIdInList(manId, actId, candidateName, paymentSum, paidList);
                if (size == paidList.size()) addPayment(userId, manId, paymentSum, actId, candidateName, projectId);
            }
            if (!paidList.isEmpty()) {
                for (int i = 0; i < paidList.size(); i++) {
                    for (int j = 0; j < paidList.get(i).getActList().size(); j++) {
                        bonusPaymentSuccess.deletePayment(paidList.get(i).getManId(), paidList.get(i).getActList().get(j).getBonus(),
                                paidList.get(i).getActList().get(j).getId(), paidList.get(i).getActList().get(j).getCandidate());
                    }

                }
            }

        } else bonusPaymentSuccess.deletePaymentAll();
        paidList.clear();
    }




    public List<EmployerNew> findByActIdInList(int userId, int actId, String candidate, Double summ, List<EmployerNew> employerNewList){


        Iterator<EmployerNew> employerNewIterator = employerNewList.iterator();

        while(employerNewIterator.hasNext()) {

            EmployerNew nextEmployerNew = employerNewIterator.next();

            Iterator<Act> actIterator = nextEmployerNew.getActList().iterator();

            while (actIterator.hasNext()){
                Act actNext = actIterator.next();
                    if (nextEmployerNew.getManId() == userId
                            && actNext.getId() == actId
                            && actNext.getCandidate().equals(candidate)
                            && actNext.getBonus().equals(summ)){
                        actIterator.remove();
                    }
            }

            if (nextEmployerNew.getActList().size() == 0){
                employerNewIterator.remove();
            }

        }
            return employerNewList;
        }


    public void updatePayment(int userId, int employerId, Date paymentDate, Double paymentRealSum, int actId, String candidate, Double summ){

        if (findByActId(userId, actId, candidate, summ) != null) {
            bonusPaymentSuccess.updatePayment(userId, employerId, paymentDate, paymentRealSum, actId, candidate);
        }
    }

    public void deletePayment(int userId, int employerId, Date paymentDate, Double paymentRealSum, int actId, String candidate, Double summ){

        if (findByActId(userId, actId, candidate, summ) != null) {
            bonusPaymentSuccess.deletePayment(employerId, paymentRealSum, actId, candidate);
        }
    }

    public List<EmployerNew> findPaidByActId(List<EmployerNew> employerList) {

        Iterator<EmployerNew> employerNewIterator = employerList.iterator();//создаем итератор

        while(employerNewIterator.hasNext()) {//до тех пор, пока в списке есть элементы

            EmployerNew nextEmployerNew = employerNewIterator.next();//получаем следующий элемент

            Iterator<Act> actIterator = nextEmployerNew.getActList().iterator();//создаем итератор

            while (actIterator.hasNext()){
                Act actNext = actIterator.next();
                if (!actNext.isPaid()){
                    actIterator.remove();
                }
            }
            if (nextEmployerNew.getActList().size() == 0){
                employerNewIterator.remove();
            }
        }

        return employerList;
    }


}


