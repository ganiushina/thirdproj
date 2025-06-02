package ru.alta.thirdproj.entites;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class UserBonusMain {

    private int userId;

    private String fio;

    private String position;

    private String department;

    private Double moneyAll;
    private String sumTotalRUB;

    private Double sumTotal;
    private String moneyAllRUB;
    private List<UserBonusDetail> userBonusDetails;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserBonusMain that = (UserBonusMain) o;
        return userId == that.userId && Objects.equals(fio, that.fio) && Objects.equals(position, that.position) && Objects.equals(department, that.department) && Objects.equals(moneyAll, that.moneyAll)
                && Objects.equals(sumTotal, that.sumTotal)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, fio, position, department, moneyAll, sumTotal,
                moneyAllRUB,  sumTotalRUB);
    }
}
