//package ru.alta.thirdproj.repositories;
//
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//import ru.alta.thirdproj.entites.Employer;
//import ru.alta.thirdproj.entites.UserBonus;
//
//import java.util.List;
//
//@Repository
//public interface IEmployRepositoryTest extends JpaRepository<Employer, Long> {
//
//
//    @Query(value = "SELECT DISTINCT man_id ,man_fio from man m " +
//            "        JOIN login l ON l.login_user_id = m.man_id" +
//            "       JOIN dbo.userplanByMonth ubm ON ubm.user_id = l.login_user_id AND ubm.userplan_month = DATEPART(mm, GETDATE()) AND ubm.userplan_year = DATEPART(yy, GETDATE())\\n\" +\n" +
//            "       where l.login_active = 1", nativeQuery = true)
//    List<Employer> getAllRecords ();
//
//}
