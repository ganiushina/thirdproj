package ru.alta.thirdproj.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.UserBonus;
import ru.alta.thirdproj.repositories.BonusRepositoryImpl;
import ru.alta.thirdproj.repositories.iUserBonusRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserBonusServiceImpl implements  iUserBonusRepository  {

    private BonusRepositoryImpl userBonusProvider;

    private List<UserBonus> userBonuses;
    private HashMap<String,Object> mapMoney = new HashMap<>();
    private HashMap<String,Object> mapSum = new HashMap<>();
    private HashMap<String,Object> mapCandidate = new HashMap<>();
    private HashMap<String,Object> mapCompany = new HashMap<>();

    private HashMap<String,Object> mapAct = new HashMap<>();

    private List<String> employers = new ArrayList<>();
    private List<String> department = new ArrayList<>();
    private List<Integer> employersId = new ArrayList<>();

    public HashMap<String, Object> getMapAct() {
        return mapAct;
    }

    @Autowired
    public void setUserBonusProvider(BonusRepositoryImpl userBonusProvider){
        this.userBonusProvider = userBonusProvider;
    }


    public HashMap<String, Object> getMapMoney() {
        return mapMoney;
    }

    public HashMap<String, Object> getMapSum() {
        return mapSum;
    }

    public HashMap<String, Object> getMapCandidate() {
        return mapCandidate;
    }

    public HashMap<String, Object> getMapCompany() {
        return mapCompany;
    }

    public List<HashMap<String, Object>> findAll(LocalDate date1, LocalDate date2, Integer userId, Integer departmentId){

        userBonuses = userBonusProvider.getUserBonuses(date1, date2, userId, departmentId);

        List<HashMap<String, Object>> entities = getHashMapsUserBonus(userBonuses);

        return entities;

    }

    private List<HashMap<String, Object>> getHashMapsUserBonus(List<UserBonus> userBonuses) {

        List<HashMap<String, Object>> entities = new ArrayList<>();
        int i = 0;

        for (UserBonus n : userBonuses) {
            HashMap<String,Object> map = new HashMap<>();
            ArrayList<Double> moneyByCandidate = new ArrayList<>();
            ArrayList<Double> userSumList = new ArrayList<>();
            ArrayList<String> candidateName = new ArrayList<>();
            ArrayList<String> companyName = new ArrayList<>();

            map.put("fio", n.getFio());
            map.put("userId", n.getUserId());
            map.put("position", n.getPosition());
            map.put("department", n.getDepartment());

            map.put("moneyAll", n.getMoneyAll());

            map.put("sumTotal", n.getSumTotal());
            map.put("percent", n.getPercent());
            map.put("month", n.getMonth());
            map.put("year", n.getYear());


            if (entities.isEmpty()){
                for (int j = 0; j < userBonuses.size() ; j++) {
                    if (userBonuses.get(j).getFio().equals(n.getFio())){
                        moneyByCandidate.add(userBonuses.get(j).getMoneyByCandidate()) ;
                        userSumList.add(userBonuses.get(j).getSumUser());
                        candidateName.add(userBonuses.get(j).getCandidateName());
                        companyName.add(userBonuses.get(j).getCompanyName());

                    }
                }
                map.put("moneyByCandidate", moneyByCandidate);
                map.put("sumUser", userSumList);
                map.put("candidateName", candidateName);
                map.put("companyName", companyName);
                map.put("actList", n.getActList());

                mapMoney.put(n.getFio(), moneyByCandidate);
                mapSum.put(n.getFio(), userSumList);
                mapCandidate.put(n.getFio(), candidateName);
                mapCompany.put(n.getFio(), companyName);

                entities.add(map);
            } else

            if (!entities.get(i).get("fio").equals(n.getFio())) {
                for (int j = 0; j < userBonuses.size() ; j++) {
                    if (userBonuses.get(j).getFio().equals(n.getFio())){
                        moneyByCandidate.add(userBonuses.get(j).getMoneyByCandidate()) ;
                        userSumList.add(userBonuses.get(j).getSumUser());
                        candidateName.add(userBonuses.get(j).getCandidateName());
                        companyName.add(userBonuses.get(j).getCompanyName());
                        mapAct.put(n.getFio(), getExtraBonus((int) userBonuses.get(j).getUserId()));
                    }
                }

                map.put("moneyByCandidate", moneyByCandidate);
                map.put("sumUser", userSumList);
                map.put("candidateName", candidateName);
                map.put("companyName", companyName);
                map.put("actList", n.getActList());
                mapMoney.put(n.getFio(), moneyByCandidate);
                mapSum.put(n.getFio(), userSumList);
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


    public List<HashMap<String, Object>> findByFioAndDepartment(String userName,  String departmentName){

        userBonuses = userBonusProvider.findByFioAndDepartment(userName,  departmentName);
        List<HashMap<String, Object>> entities = getHashMapsUserBonus(userBonuses);
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

    private List<Integer> setEmployersId(int empId){
        employersId.add(empId);
        return employersId;
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

    public List<Integer> getEmployersId(){
        List<Integer> uniqueEmployersId =
                employersId
                        .stream()
                        .distinct()
                        .collect(Collectors.toList());
        return uniqueEmployersId;
    }

    @Override
    public List<UserBonus> getUserBonuses(LocalDate date1, LocalDate date12, Integer userId, Integer departmentId) {
        return null;
    }

    public double getAllMoney(HashMap<String, Object> mapMoney) {

        ArrayList<Double> doubleArrayList = new ArrayList<>();
        mapMoney.forEach((k, v) -> {
            if (v instanceof ArrayList) {
                ArrayList<Double> theV = (ArrayList<Double>) v;
                for (int i = 0; i < theV.size(); i++) {
                    doubleArrayList.add(theV.get(i).doubleValue());

                }

            }
        });

        return   doubleArrayList.stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public List<Act> getExtraBonus(Integer employerId){
        return userBonusProvider.getExtraAct(employerId);
    }

}
