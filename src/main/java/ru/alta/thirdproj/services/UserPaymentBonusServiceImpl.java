package ru.alta.thirdproj.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.expression.Lists;
import ru.alta.thirdproj.entites.Act;
import ru.alta.thirdproj.entites.Employer;
import ru.alta.thirdproj.entites.EmployerNew;
import ru.alta.thirdproj.entites.UserPaymentBonus;
import ru.alta.thirdproj.repositories.BonusPaymentRepositoryImpl;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;


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

    public List<HashMap<String, Object>> findAll(LocalDate date1, LocalDate date2){

        userPaymentBonuses = userBonusPaymentRepository.getUserBonuses(date1, date2);
        List<HashMap<String, Object>> entities = getHashMapsUserBonusPayment(userPaymentBonuses);

        return entities;

    }

    public List<EmployerNew> getEmployerList (LocalDate date1, LocalDate date2){
        return userBonusPaymentRepository.userBonusPaymentList(date1,date2);
    }

    public String getAllMoney(List<EmployerNew> employerNews) {
        ArrayList<Double> doubleArrayList = new ArrayList<>();
        NumberFormat formatter = NumberFormat.getCurrencyInstance();

        for (int i = 0; i < employerNews.size(); i++) {
            for (int j = 0; j < employerNews.get(i).getActList().size(); j++) {
                doubleArrayList.add(employerNews.get(i).getActList().get(j).getBonus());
            }
        }


        double doublesSum = doubleArrayList.stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        return formatter.format(doublesSum);
    }

    public String getMoneyByDate(List<EmployerNew> employerNews) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("MMMMM");

        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        Currency currency = Currency.getInstance("RUB");
        format.setCurrency(currency);
//        NumberFormat formatter = NumberFormat.getInstance("ru");
//        Currency.getInstance("ru");


        List<Act> actList2 = new ArrayList<>();

        for (EmployerNew e : employerNews) {
            List<Act> actList = e.getActList();
            for (Act a : actList){
                actList2.add(a);
            }
        }


        SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat("d");



        List<Act> result1 = actList2.stream().sorted((o1, o2)->o1.getDateForPay().
                compareTo(o2.getDateForPay())).
                collect(Collectors.toList());


        Map<String, Double> map5 = actList2.stream()
                .collect(Collectors.groupingBy(Act::getDateForPay,
                        Collectors.summingDouble(Act::getBonus)));


        StringBuilder stringBuilder = new StringBuilder();


        for (Map.Entry<String, Double> entry : map5.entrySet()) {
            Date date = null;
            try {
                date = formatter2.parse(entry.getKey());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            stringBuilder.append(simpleDateFormat1.format(date) + " " + simpleDateFormat.format(date) + " : " + format.format(entry.getValue()) + " ");
        }

        return stringBuilder.toString();
    }



    public String getAllPaymentMoney(List<EmployerNew> employerNews) {
        ArrayList<Double> doubleArrayList = new ArrayList<>();

        NumberFormat formatter = NumberFormat.getCurrencyInstance();

        for (int i = 0; i < employerNews.size(); i++) {
            for (int j = 0; j < employerNews.get(i).getActList().size(); j++) {
                if (employerNews.get(i).getActList().get(j).isPaid()) {
                    doubleArrayList.add(employerNews.get(i).getActList().get(j).getBonus());
                }
            }
        }

        double doublesSum = doubleArrayList.stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        return formatter.format(doublesSum);
    }

    public String getAllNotPaymentMoney(List<EmployerNew> employerNews) {
        ArrayList<Double> doubleArrayList = new ArrayList<>();

        NumberFormat formatter = NumberFormat.getCurrencyInstance();

        for (int i = 0; i < employerNews.size(); i++) {
            for (int j = 0; j < employerNews.get(i).getActList().size(); j++) {
                if (!employerNews.get(i).getActList().get(j).isPaid()) {
                    doubleArrayList.add(employerNews.get(i).getActList().get(j).getBonus());
                }
            }
        }

        double doublesSum = doubleArrayList.stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        return formatter.format(doublesSum);
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

            map.put("department", n.getDepartment());

            map.put("allBonus", n.getAllBonus());

            map.put("percent", n.getPercent());
            map.put("dateAct", n.getDateAct());

            map.put("dateForPay", n.getDateForPay());
            map.put("paymentDate", n.getPaymentDate());

            if (entities.isEmpty()){
                for (int j = 0; j < userPaymentBonuses.size() ; j++) {
                    if (userPaymentBonuses.get(j).getFio().equals(n.getFio())){
                        actNums.add(userPaymentBonuses.get(j).getActNum()) ;
                        bonus.add(userPaymentBonuses.get(j).getBonus());
                        candidateName.add(userPaymentBonuses.get(j).getCandidateName()  + " - " + userPaymentBonuses.get(j).getBonus());
                        companyName.add(userPaymentBonuses.get(j).getCompanyName());

                    }
                }
                map.put("actNum", actNums);
                map.put("bonus", bonus );
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
                        candidateName.add(userPaymentBonuses.get(j).getCandidateName()  + " - " + userPaymentBonuses.get(j).getBonus());
                        companyName.add(userPaymentBonuses.get(j).getCompanyName());

                    }
                }
                map.put("actNum", actNums);
                map.put("bonus", bonus );
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
