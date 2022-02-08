package ru.alta.thirdproj.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.alta.thirdproj.entites.UserBonus;


public class UserBonusSpecification {
    public static Specification<UserBonus> fioContains(String fio) {
        return (Specification<UserBonus>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(root.get("fio"), "%" + fio + "%");
    }
    public static Specification<UserBonus> departmentContains(String department) {
        return (Specification<UserBonus>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(root.get("department"), "%" + department + "%");
    }

//    public static Specification<Product> priceGreaterThanOrEq(double value) {
//        return (Specification<Product>) (root, criteriaQuery, criteriaBuilder) -> {
//            return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), value);
//        };
//    }
//
//    public static Specification<Product> priceLesserThanOrEq(double value) {
//        return (Specification<Product>) (root, criteriaQuery, criteriaBuilder) -> {
//            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), value);
//        };
//    }

}
