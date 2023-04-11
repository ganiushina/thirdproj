package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.EmployerNew;
import ru.alta.thirdproj.entites.PaymentSuccess;
import ru.alta.thirdproj.repositories.IBonusPaymentSuccess;

import java.text.SimpleDateFormat;
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
                              int projectId, String monthKPI, int type){

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
        bonusPaymentSuccess.save(paymentSuccess);

        return true;
    }

    public Optional<PaymentSuccess> findByActId(int userId, int actId, String candidate, Double summ){
        return Optional.ofNullable(bonusPaymentSuccess.findOneByAct(userId, actId, candidate, summ));

    }

    public void findEmployer(String val, int userId, List<EmployerNew> employerNewList) {


       List<EmployerNew> paidList = findPaidByActId(employerNewList);

       SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("MMMMM");

        myBreakLabel:
        if (val != null) {
            List<String> arrOfEmployerBonus = Arrays.asList(val.split(","));
//            List<String> result = arrOfEmployerBonus.stream()
//                    .filter(lang -> lang.contains("false"))
//                    .collect(Collectors.toList());

            int type = 0;
            for (int k = 0; k < arrOfEmployerBonus.size(); k++) {

                int manId ;
                Double paymentSum ;
                int actId ;
                String candidateName ;
                String monthKPI = "";
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

                size = findCountActId(paidList);

                if (candidateName.contains("KPI")) {
                    monthKPI = candidateName.replace("KPI - ", "");
                    type = 1;
                    projectId = 0;
               //     paidList = findByActIdInList(manId, actId, monthKPI, paymentSum, paidList);
                }
                else {
                    source = source.replace(candidateName + "/", "");
                    projectId = Integer.parseInt(source.substring(0, source.length() - 1));
                    type = 2;
              //      paidList = findByActIdInList(manId, actId, candidateName, paymentSum, paidList);
                }
                paidList = findByActIdInList(manId, actId, candidateName, paymentSum, paidList);

                if (size == findCountActId(paidList)) {
                    addPayment(userId, manId, paymentSum, actId, candidateName, projectId, monthKPI, type);
                  //  break myBreakLabel;
                }

            }
            if (!paidList.isEmpty()) {
                if (paidList.get(0).getActList().get(0).getId() != 0)
                bonusPaymentSuccess.deletePayment(paidList.get(0).getManId(), Double.valueOf(paidList.get(0).getActList().get(0).getBonus()),
                        paidList.get(0).getActList().get(0).getId(), paidList.get(0).getActList().get(0).getCandidate());
                if (paidList.get(0).getActList().get(0).getId() == 0)
                    bonusPaymentSuccess.deletePaymentKPI(paidList.get(0).getManId(), Double.valueOf(paidList.get(0).getActList().get(0).getBonus()),
                            paidList.get(0).getActList().get(0).getCandidate());
            }

        } else bonusPaymentSuccess.deletePaymentAll();
        paidList.clear();
    }

    public void findActInList(String val, int userId, List<EmployerNew> employerNewList) {

        List<EmployerNew> paidList = findPaidByActId(employerNewList);

        int cntOld = findCountActId(paidList);
        int cntNew;

        int manId ;
        Double paymentSum ;
        int actId ;
        String candidateName ;
        String monthKPI = "";
        int projectId ;
        int type;

        List<EmployerNew> newList = new ArrayList<>();


        if (val != null) {
            List<String> arrOfEmployerBonus = Arrays.asList(val.split(","));
            cntNew = arrOfEmployerBonus.size();

            if (cntOld < cntNew) {
                List<String> result = arrOfEmployerBonus.stream()
                        .filter(lang -> lang.contains("false"))
                        .collect(Collectors.toList());

                String source = result.get(0);
                paymentSum = Double.valueOf(source.substring(0, source.indexOf("/")));
                source = source.replace(paymentSum + "/", "");
                manId = Integer.parseInt(source.substring(0, source.indexOf("/")));
                source = source.replace(manId + "/", "");
                actId = Integer.parseInt(source.substring(0, source.indexOf("/")));
                source = source.replace(actId + "/", "");
                candidateName = source.substring(0, source.indexOf("/"));

                if (candidateName.contains("KPI")) {
                    monthKPI = candidateName.replace("KPI - ", "");
                    type = 1;
                    projectId = 0;
                }
                else {
                    source = source.replace(candidateName + "/", "");
                    projectId = Integer.parseInt(source.substring(0, source.indexOf("/")));
                    type = 2;
                }
                addPayment(userId, manId, paymentSum, actId, candidateName, projectId, monthKPI, type);
            }
            else {
                for (int k = 0; k < arrOfEmployerBonus.size(); k++) {

                    String source = arrOfEmployerBonus.get(k);
                    paymentSum = Double.valueOf(source.substring(0, source.indexOf("/")));
                    source = source.replace(paymentSum + "/", "");
                    manId = Integer.parseInt(source.substring(0, source.indexOf("/")));
                    source = source.replace(manId + "/", "");
                    actId = Integer.parseInt(source.substring(0, source.indexOf("/")));
                    source = source.replace(actId + "/", "");
                    candidateName = source.substring(0, source.indexOf("/"));

                    newList = findByActIdInList(manId, actId, candidateName, paymentSum, paidList);
                }

                bonusPaymentSuccess.deletePayment(newList.get(0).getManId(), Double.valueOf(newList.get(0).getActList().get(0).getBonus()),
                        newList.get(0).getActList().get(0).getId(), newList.get(0).getActList().get(0).getCandidate());

            }

        } else bonusPaymentSuccess.deletePaymentAll();
        paidList.clear();
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
//                }
//                    if (actId == 0){
//                        if (nextEmployerNew.getManId() == userId
//                                && actNext.equals(candidate.toLowerCase())
//                                && actNext.getBonus().equals(summ)) {
//                            actIterator.remove();
//                            brk = true;
//                            break;
//                        }
//                    }

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


