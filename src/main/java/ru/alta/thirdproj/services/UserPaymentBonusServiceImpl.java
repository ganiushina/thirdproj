package ru.alta.thirdproj.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.UserBonus;
import ru.alta.thirdproj.entites.UserPaymentBonus;
import ru.alta.thirdproj.repositories.BonusPaymentRepositoryImpl;
import ru.alta.thirdproj.repositories.BonusRepositoryImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserPaymentBonusServiceImpl {

    private BonusPaymentRepositoryImpl userBonusPaymentRepository;

    private List<UserPaymentBonus> userPaymentBonuses;
    HashMap<String,Object> mapActNum = new HashMap<>();
    HashMap<String,Object> mapBonus = new HashMap<>();
    HashMap<String,Object> mapCandidate = new HashMap<>();
    HashMap<String,Object> mapCompany = new HashMap<>();
    private List<String> employers = new ArrayList<>();

    public HashMap<String, Object> getMapActNum() {
        return mapActNum;
    }

    public HashMap<String, Object> getMapBonus() {
        return mapBonus;
    }

    public HashMap<String, Object> getMapCandidate() {
        return mapCandidate;
    }

    public HashMap<String, Object> getMapCompany() {
        return mapCompany;
    }

    private List<String> department = new ArrayList<>();

    @Autowired
    public void setUserBonusProvider(BonusPaymentRepositoryImpl userBonusPaymentRepository){
        this.userBonusPaymentRepository = userBonusPaymentRepository;
    }

    public List<HashMap<String, Object>> findAll(LocalDate date1, LocalDate date2, Integer userId, Integer departmentId){

        userPaymentBonuses = userBonusPaymentRepository.getUserBonuses(date1, date2, userId, departmentId);
        List<HashMap<String, Object>> entities = getHashMapsUserBonusPayment(userPaymentBonuses);

        return entities;

    }

    public List<HashMap<String, Object>> findByUserFIOAnfDepartment(String userName, String department){

        userPaymentBonuses = userBonusPaymentRepository.findByFioAndDepartment(userName,  department);

        List<HashMap<String, Object>> entities = getHashMapsUserBonusPayment(userPaymentBonuses);
        return entities;
    }

    private List<HashMap<String, Object>> getHashMapsUserBonusPayment(List<UserPaymentBonus> userPaymentBonuses){

        List<HashMap<String, Object>> entities = new ArrayList<>();

        int i = 0;

        for (UserPaymentBonus n : userPaymentBonuses) {
            HashMap<String,Object> map = new HashMap<>();
            ArrayList<String> actNums = new ArrayList<>();
            ArrayList<Double> bonus = new ArrayList<>();
            ArrayList<String> candidateName = new ArrayList<>();
            ArrayList<String> companyName = new ArrayList<>();
            map.put("fio", n.getFio());
            map.put("position", n.getPosition());
            map.put("department", n.getDepartment());

            map.put("allBonus", n.getAllBonus());

            map.put("percent", n.getPercent());
            map.put("monthNumAct", n.getMonthNumAct());
            map.put("yearAct", n.getYearAct());

            map.put("dateForPay", n.getDateForPay());
            map.put("paymentDate", n.getPaymentDate());

            if (entities.isEmpty()){
                for (int j = 0; j < userPaymentBonuses.size() ; j++) {
                    if (userPaymentBonuses.get(j).getFio().equals(n.getFio())){
                        actNums.add(userPaymentBonuses.get(j).getActNum()) ;
                        bonus.add(userPaymentBonuses.get(j).getBonus());
                        candidateName.add(userPaymentBonuses.get(j).getCandidateName());
                        companyName.add(userPaymentBonuses.get(j).getCompanyName());

                    }
                }
                map.put("actNum", actNums);
                map.put("bonus", bonus);
                map.put("candidateName", candidateName);
                map.put("companyName", companyName);

                mapActNum.put(n.getFio(), actNums);
                mapBonus.put(n.getFio(), bonus);
                mapCandidate.put(n.getFio(), candidateName);
                mapCompany.put(n.getFio(), companyName);

                entities.add(map);
            } else

            if (!entities.get(i).get("fio").equals(n.getFio())) {
                for (int j = 0; j < userPaymentBonuses.size() ; j++) {
                    if (userPaymentBonuses.get(j).getFio().equals(n.getFio())){
                        actNums.add(userPaymentBonuses.get(j).getActNum()) ;
                        bonus.add(userPaymentBonuses.get(j).getBonus());
                        candidateName.add(userPaymentBonuses.get(j).getCandidateName());
                        companyName.add(userPaymentBonuses.get(j).getCompanyName());

                    }
                }
                map.put("actNum", actNums);
                map.put("bonus", bonus);
                map.put("candidateName", candidateName);
                map.put("companyName", companyName);

                mapActNum.put(n.getFio(), actNums);
                mapBonus.put(n.getFio(), bonus);
                mapCandidate.put(n.getFio(), candidateName);
                mapCompany.put(n.getFio(), companyName);

                entities.add(map);
                i++;

            }
            setEmployers(n.getFio());
            setDep(n.getDepartment());

        }

        return entities;

    }

    private List<String> setDep(String name){
        department.add(name);
        return department;
    }

    private List<String> setEmployers(String name){
        employers.add(name);
        return employers;
    }

    public List<String> getDepartment() {
        List<String> uniqueDepartment =
                department
                        .stream()
                        .distinct()
                        .collect(Collectors.toList());
        return uniqueDepartment;
    }


    public List<String> getEmployers(){
        List<String> uniqueEmployers =
                employers
                        .stream()
                        .distinct()
                        .collect(Collectors.toList());

        return uniqueEmployers;
    }




}
