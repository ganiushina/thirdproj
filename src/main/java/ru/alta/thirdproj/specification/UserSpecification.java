package ru.alta.thirdproj.specification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.entites.UserBonus;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class UserSpecification implements Specification<UserBonus> {

    private SearchCriteria criteria;

    @Override
    public Predicate toPredicate
            (Root<UserBonus> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        if (criteria.getOperation().equalsIgnoreCase(">")) {
            return builder.greaterThanOrEqualTo(
                    root.<String> get(criteria.getKey()), criteria.getValue().toString());
        }
        else if (criteria.getOperation().equalsIgnoreCase("<")) {
            return builder.lessThanOrEqualTo(
                    root.<String> get(criteria.getKey()), criteria.getValue().toString());
        }
        else if (criteria.getOperation().equalsIgnoreCase(":")) {
            if (root.get(criteria.getKey()).getJavaType() == String.class) {
                return builder.like(
                        root.<String>get(criteria.getKey()), "%" + criteria.getValue() + "%");
            } else {
                return builder.equal(root.get(criteria.getKey()), criteria.getValue());
            }
        }
        return null;
    }

//    public static Specification<UserBonus> userNameEquals(String userName) {
//        return (Specification<UserBonus>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(root.get("fio"), "%" + userName + "%");
//    }
//
//    public static Specification<UserBonus> userDepartmentEquals(String userDepartment) {
//        return (Specification<UserBonus>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(root.get("department"), "%" + userDepartment + "%");
//    }

}
