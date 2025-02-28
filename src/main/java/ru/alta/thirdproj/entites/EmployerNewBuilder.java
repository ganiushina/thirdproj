package ru.alta.thirdproj.entites;


import lombok.Data;

import java.util.List;

public final class EmployerNewBuilder {

    private int manId;
    private String manFIO;
    private String userDepartment;
    private List<Act> actList;
    private Double allBonus;
    private List<Integer> percent;
    private String allBonusRUB;

    private EmployerNewBuilder(){
    }

    public static EmployerNewBuilder anEmployer(){
        return new EmployerNewBuilder();
    }

    public EmployerNewBuilder withManId(int manId){
        this.manId = manId;
        return this;
    }

    public EmployerNewBuilder withManFio(String manFio){
        this.manFIO = manFio;
        return this;
    }
    public EmployerNewBuilder withUserDepartment(String userDepartment){
        this.userDepartment = userDepartment;
        return this;
    }

    public EmployerNewBuilder withActList(List<Act> actList){
        this.actList = actList;
        return this;
    }
    public EmployerNewBuilder withAllBonus(Double allBonus){
        this.allBonus = allBonus;
        return this;
    }

    public EmployerNewBuilder withPercent( List<Integer> percent){
        this.percent = percent;
        return this;
    }

    public EmployerNewBuilder withAllBonusRUB(String allBonusRUB){
        this.allBonusRUB = allBonusRUB;
        return this;
    }

    public EmployerNew build(){
        EmployerNew employerNew = new EmployerNew();
        employerNew.setManFIO(manFIO);
        employerNew.setManId(manId);
        employerNew.setPercent(percent);
        employerNew.setActList(actList);
        employerNew.setAllBonus(allBonus);
        employerNew.setAllBonusRUB(allBonusRUB);
        employerNew.setUserDepartment(userDepartment);
        return employerNew;
    }


}
