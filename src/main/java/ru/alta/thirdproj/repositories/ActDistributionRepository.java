package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;
import ru.alta.thirdproj.entites.ActBuhInfo;
import ru.alta.thirdproj.entites.ActDistributionRow;
import ru.alta.thirdproj.entites.Employer;

import java.util.List;

@Component
public class ActDistributionRepository {

    private final Sql2o sql2o;

    public ActDistributionRepository(@Autowired Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    // ─── Список актов ────────────────────────────────────────────────────────

    private static final String SELECT_ALL_ACTS =
            "SELECT ab.id, LEFT(ab.act_num, 11) AS act_num, " +
            "       CONVERT(date, ab.date_act) AS date_act, " +
            "       ab.company_name, ab.total_no_nds, ab.candidate, ab.project_name, " +
            "       COUNT(pb.id) AS rows_count " +
            "FROM act_buh ab " +
            "LEFT JOIN project_buh pb ON pb.act_id = ab.id " +
            "WHERE ab.company_name NOT LIKE 'АЛЬТА ПЕРСОНАЛ ООО' " +
            "  AND ab.company_name NOT LIKE 'АЛЬТА КОНСАЛТ ООО' " +
            "GROUP BY ab.id, ab.act_num, ab.date_act, ab.company_name, " +
            "         ab.total_no_nds, ab.candidate, ab.project_name " +
            "ORDER BY ab.date_act DESC";

    public List<ActBuhInfo> getAllActs() {
        try (Connection con = sql2o.open()) {
            return con.createQuery(SELECT_ALL_ACTS, false)
                    .executeAndFetch((ResultSetHandler<ActBuhInfo>) rs -> {
                        ActBuhInfo a = new ActBuhInfo();
                        a.setId(rs.getInt("id"));
                        a.setActNum(rs.getString("act_num"));
                        java.sql.Date d = rs.getDate("date_act");
                        if (d != null) a.setDateAct(d.toLocalDate());
                        a.setCompanyName(rs.getString("company_name"));
                        a.setTotalNoNds(rs.getDouble("total_no_nds"));
                        a.setCandidate(rs.getString("candidate"));
                        a.setProjectName(rs.getString("project_name"));
                        a.setDistributionRowsCount(rs.getInt("rows_count"));
                        return a;
                    });
        }
    }

    // ─── Конкретный акт ──────────────────────────────────────────────────────

    private static final String SELECT_ACT_BY_ID =
            "SELECT ab.id, LEFT(ab.act_num, 11) AS act_num, " +
            "       CONVERT(date, ab.date_act) AS date_act, " +
            "       ab.company_name, ab.total_no_nds, ab.candidate, ab.project_name, " +
            "       0 AS rows_count " +
            "FROM act_buh ab WHERE ab.id = :actId";

    public ActBuhInfo getActById(int actId) {
        try (Connection con = sql2o.open()) {
            return con.createQuery(SELECT_ACT_BY_ID, false)
                    .addParameter("actId", actId)
                    .executeAndFetch((ResultSetHandler<ActBuhInfo>) rs -> {
                        ActBuhInfo a = new ActBuhInfo();
                        a.setId(rs.getInt("id"));
                        a.setActNum(rs.getString("act_num"));
                        java.sql.Date d = rs.getDate("date_act");
                        if (d != null) a.setDateAct(d.toLocalDate());
                        a.setCompanyName(rs.getString("company_name"));
                        a.setTotalNoNds(rs.getDouble("total_no_nds"));
                        a.setCandidate(rs.getString("candidate"));
                        a.setProjectName(rs.getString("project_name"));
                        return a;
                    })
                    .stream().findFirst().orElse(null);
        }
    }

    // ─── Строки project_buh по акту ──────────────────────────────────────────

    private static final String SELECT_ROWS_BY_ACT =
            "SELECT pb.id, pb.act_id, " +
            "       pb.responsible_user_name, pb.responsible_user_id, " +
            "       pb.resecher_name, pb.resecher_id, " +
            "       pb.summ_responsible_user, pb.summ_resecher, " +
            "       pb.depatment, pb.depatment_id, " +
            "       pb.percent_responsible_user_complicity, pb.percent_resecher_complicity, " +
            "       pb.teame_leader_name, pb.teame_leader_id, " +
            "       pb.city_responsible_user, pb.city_responsible_user_id, " +
            "       pb.percent_responsible_user_by_candidate_percent, " +
            "       pb.percent_reseacher_by_candidate_percent, " +
            "       pb.depatment_resecher, pb.depatment_resecher_id " +
            "FROM project_buh pb " +
            "WHERE pb.act_id = :actId " +
            "ORDER BY pb.id";

    public List<ActDistributionRow> getRowsByActId(int actId) {
        try (Connection con = sql2o.open()) {
            return con.createQuery(SELECT_ROWS_BY_ACT, false)
                    .addParameter("actId", actId)
                    .executeAndFetch((ResultSetHandler<ActDistributionRow>) rs -> {
                        ActDistributionRow r = new ActDistributionRow();
                        r.setId(rs.getInt("id"));
                        r.setActId(rs.getInt("act_id"));
                        r.setConsultantName(rs.getString("responsible_user_name"));
                        r.setConsultantId(rs.getInt("responsible_user_id"));
                        r.setResearcherName(rs.getString("resecher_name"));
                        int resId = rs.getInt("resecher_id");
                        if (!rs.wasNull()) r.setResearcherId(resId);
                        r.setConsultantSum(rs.getDouble("summ_responsible_user"));
                        r.setResearcherSum(rs.getDouble("summ_resecher"));
                        r.setDepartment(rs.getString("depatment"));
                        int depId = rs.getInt("depatment_id");
                        if (!rs.wasNull()) r.setDepartmentId(depId);
                        r.setConsultantPercent(rs.getDouble("percent_responsible_user_complicity"));
                        r.setResearcherPercent(rs.getDouble("percent_resecher_complicity"));
                        r.setTeamLeaderName(rs.getString("teame_leader_name"));
                        int tlId = rs.getInt("teame_leader_id");
                        if (!rs.wasNull()) r.setTeamLeaderId(tlId);
                        r.setCity(rs.getString("city_responsible_user"));
                        int cityId = rs.getInt("city_responsible_user_id");
                        if (!rs.wasNull()) r.setCityId(cityId);
                        r.setConsultantPercentOfTotal(rs.getDouble("percent_responsible_user_by_candidate_percent"));
                        r.setResearcherPercentOfTotal(rs.getDouble("percent_reseacher_by_candidate_percent"));
                        r.setDepartmentResearcher(rs.getString("depatment_resecher"));
                        int depResId = rs.getInt("depatment_resecher_id");
                        if (!rs.wasNull()) r.setDepartmentResearcherId(depResId);
                        return r;
                    });
        }
    }

    // ─── Добавить строку ─────────────────────────────────────────────────────

    private static final String INSERT_ROW =
            "INSERT INTO project_buh (" +
            "  act_id, responsible_user_name, responsible_user_id, " +
            "  resecher_name, resecher_id, " +
            "  summ_responsible_user, summ_resecher, " +
            "  depatment, depatment_id, " +
            "  percent_responsible_user_complicity, percent_resecher_complicity, " +
            "  teame_leader_name, teame_leader_id, " +
            "  city_responsible_user, city_responsible_user_id, " +
            "  percent_responsible_user_by_candidate_percent, " +
            "  percent_reseacher_by_candidate_percent, " +
            "  depatment_resecher, depatment_resecher_id" +
            ") VALUES (" +
            "  :actId, :consultantName, :consultantId, " +
            "  :researcherName, :researcherId, " +
            "  :consultantSum, :researcherSum, " +
            "  :department, :departmentId, " +
            "  :consultantPercent, :researcherPercent, " +
            "  :teamLeaderName, :teamLeaderId, " +
            "  :city, :cityId, " +
            "  :consultantPercentOfTotal, " +
            "  :researcherPercentOfTotal, " +
            "  :departmentResearcher, :departmentResearcherId" +
            ")";

    @Transactional
    public void addRow(ActDistributionRow row) {
        try (Connection con = sql2o.beginTransaction()) {
            con.createQuery(INSERT_ROW, false)
                    .addParameter("actId",                  row.getActId())
                    .addParameter("consultantName",         row.getConsultantName())
                    .addParameter("consultantId",           row.getConsultantId())
                    .addParameter("researcherName",         row.getResearcherName())
                    .addParameter("researcherId",           row.getResearcherId())
                    .addParameter("consultantSum",          row.getConsultantSum())
                    .addParameter("researcherSum",          row.getResearcherSum())
                    .addParameter("department",             row.getDepartment())
                    .addParameter("departmentId",           row.getDepartmentId())
                    .addParameter("consultantPercent",      row.getConsultantPercent())
                    .addParameter("researcherPercent",      row.getResearcherPercent())
                    .addParameter("teamLeaderName",         row.getTeamLeaderName())
                    .addParameter("teamLeaderId",           row.getTeamLeaderId())
                    .addParameter("city",                   row.getCity())
                    .addParameter("cityId",                 row.getCityId())
                    .addParameter("consultantPercentOfTotal",  row.getConsultantPercentOfTotal())
                    .addParameter("researcherPercentOfTotal",  row.getResearcherPercentOfTotal())
                    .addParameter("departmentResearcher",   row.getDepartmentResearcher())
                    .addParameter("departmentResearcherId", row.getDepartmentResearcherId())
                    .executeUpdate();
            con.commit();
        }
    }

    // ─── Обновить строку ─────────────────────────────────────────────────────

    private static final String UPDATE_ROW =
            "UPDATE project_buh SET " +
            "  responsible_user_name = :consultantName, " +
            "  responsible_user_id   = :consultantId, " +
            "  resecher_name         = :researcherName, " +
            "  resecher_id           = :researcherId, " +
            "  summ_responsible_user = :consultantSum, " +
            "  summ_resecher         = :researcherSum, " +
            "  depatment             = :department, " +
            "  depatment_id          = :departmentId, " +
            "  percent_responsible_user_complicity = :consultantPercent, " +
            "  percent_resecher_complicity         = :researcherPercent, " +
            "  teame_leader_name     = :teamLeaderName, " +
            "  teame_leader_id       = :teamLeaderId, " +
            "  city_responsible_user = :city, " +
            "  city_responsible_user_id = :cityId, " +
            "  percent_responsible_user_by_candidate_percent = :consultantPercentOfTotal, " +
            "  percent_reseacher_by_candidate_percent        = :researcherPercentOfTotal, " +
            "  depatment_resecher    = :departmentResearcher, " +
            "  depatment_resecher_id = :departmentResearcherId " +
            "WHERE id = :id";

    @Transactional
    public void updateRow(ActDistributionRow row) {
        try (Connection con = sql2o.beginTransaction()) {
            con.createQuery(UPDATE_ROW, false)
                    .addParameter("id",                     row.getId())
                    .addParameter("consultantName",         row.getConsultantName())
                    .addParameter("consultantId",           row.getConsultantId())
                    .addParameter("researcherName",         row.getResearcherName())
                    .addParameter("researcherId",           row.getResearcherId())
                    .addParameter("consultantSum",          row.getConsultantSum())
                    .addParameter("researcherSum",          row.getResearcherSum())
                    .addParameter("department",             row.getDepartment())
                    .addParameter("departmentId",           row.getDepartmentId())
                    .addParameter("consultantPercent",      row.getConsultantPercent())
                    .addParameter("researcherPercent",      row.getResearcherPercent())
                    .addParameter("teamLeaderName",         row.getTeamLeaderName())
                    .addParameter("teamLeaderId",           row.getTeamLeaderId())
                    .addParameter("city",                   row.getCity())
                    .addParameter("cityId",                 row.getCityId())
                    .addParameter("consultantPercentOfTotal",  row.getConsultantPercentOfTotal())
                    .addParameter("researcherPercentOfTotal",  row.getResearcherPercentOfTotal())
                    .addParameter("departmentResearcher",   row.getDepartmentResearcher())
                    .addParameter("departmentResearcherId", row.getDepartmentResearcherId())
                    .executeUpdate();
            con.commit();
        }
    }

    // ─── Удалить строку ──────────────────────────────────────────────────────

    private static final String DELETE_ROW =
            "DELETE FROM project_buh WHERE id = :id";

    @Transactional
    public void deleteRow(int id) {
        try (Connection con = sql2o.beginTransaction()) {
            con.createQuery(DELETE_ROW, false)
                    .addParameter("id", id)
                    .executeUpdate();
            con.commit();
        }
    }

    // ─── Справочник сотрудников (для выпадающих списков) ─────────────────────

    private static final String SELECT_EMPLOYEES =
            "SELECT m.man_id, m.man_fio, up.dep_id, d.dep_name, p.pos_name " +
            "FROM man m " +
            "JOIN user_position up ON up.user_id = m.man_id " +
            "JOIN depatment d ON d.id = up.dep_id " +
            "JOIN position p ON p.id = up.position_id " +
            "ORDER BY m.man_fio";

    public List<Employer> getEmployees() {
        try (Connection con = sql2o.open()) {
            return con.createQuery(SELECT_EMPLOYEES, false)
                    .executeAndFetch((ResultSetHandler<Employer>) rs -> {
                        Employer e = new Employer();
                        e.setManId(rs.getInt("man_id"));
                        e.setManFIO(rs.getString("man_fio"));
                        return e;
                    });
        }
    }
}
