package ru.alta.thirdproj.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.alta.thirdproj.entites.BonusGap;
import ru.alta.thirdproj.entites.BonusPosition;
import ru.alta.thirdproj.entites.BonusSchemeEntry;
import ru.alta.thirdproj.entites.BonusSchemeLimit;
import ru.alta.thirdproj.entites.BonusSchemeLimitView;
import ru.alta.thirdproj.entites.BonusSchemeName;
import ru.alta.thirdproj.entites.BonusSchemeBdmLimit;

import java.util.List;

@Component
public class BonusSchemeRepository {

    private final Sql2o sql2o;

    private static final String SELECT_GAPS = "SELECT gap_id, gap_percent, scheme_date FROM gap";
    private static final String SELECT_POSITIONS = "SELECT id, pos_name FROM position";
    private static final String SELECT_LIMITS = "SELECT id, scheme_id, limits, position_id, gap_id, date_scheme FROM scheme_limits";
    private static final String SELECT_SCHEMES_BDN = "SELECT id, limits, position_id, gap_id, division_id, scheme_limits_date  FROM scheme_limits_bdm";
    private static final String SELECT_SCHEMES_NAME = "SELECT id, scheme_name FROM scheme";
    private static final String SELECT_LIMIT_DETAILS = "SELECT p.pos_name, s.scheme_name, sl.limits, g.gap_percent, sl.date_scheme " +
            "FROM scheme_limits sl " +
            "JOIN gap g ON g.gap_id = sl.gap_id " +
            "JOIN position p ON p.id = sl.position_id " +
            "JOIN scheme s ON s.id = sl.scheme_id " +
            "JOIN (SELECT scheme_id, position_id, MAX(date_scheme) AS max_date FROM scheme_limits GROUP BY scheme_id, position_id) latest " +
            "  ON latest.scheme_id = sl.scheme_id AND latest.position_id = sl.position_id AND latest.max_date = sl.date_scheme " +
            "ORDER BY p.pos_name, s.scheme_name, sl.limits";
    private static final String INSERT_SCHEME = "INSERT INTO scheme_limits(scheme_id, limits, position_id, gap_id, date_scheme)\n" +
            "     VALUES (:scheme_id, :limits, :position_id, :gap_id, :date_scheme)";

    @Autowired
    public BonusSchemeRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public List<BonusGap> findAllGaps() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_GAPS, false)
                    .setColumnMappings(BonusGap.COLUMN_MAPPINGS)
                    .executeAndFetch(BonusGap.class);
        }
    }

    public List<BonusPosition> findAllPositions() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_POSITIONS, false)
                    .setColumnMappings(BonusPosition.COLUMN_MAPPINGS)
                    .executeAndFetch(BonusPosition.class);
        }
    }

    public List<BonusSchemeLimit> findAllLimits() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_LIMITS, false)
                    .setColumnMappings(BonusSchemeLimit.COLUMN_MAPPINGS)
                    .executeAndFetch(BonusSchemeLimit.class);
        }
    }

    public List<BonusSchemeLimitView> findAllLimitDetails() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_LIMIT_DETAILS, false)
                    .setColumnMappings(BonusSchemeLimitView.COLUMN_MAPPINGS)
                    .executeAndFetch(BonusSchemeLimitView.class);
        }
    }

    public List<BonusSchemeBdmLimit> findAllBdmSchemes() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_SCHEMES_BDN, false)
                    .setColumnMappings(BonusSchemeBdmLimit.COLUMN_MAPPINGS)
                    .executeAndFetch(BonusSchemeBdmLimit.class);
        }
    }

    public List<BonusSchemeName> findAllSchemeNames() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery(SELECT_SCHEMES_NAME, false)
                    .setColumnMappings(BonusSchemeName.COLUMN_MAPPINGS)
                    .executeAndFetch(BonusSchemeName.class);
        }
    }

    public void saveSchemeEntry(BonusSchemeEntry entry) {
        try (Connection connection = sql2o.beginTransaction()) {
            connection.createQuery(INSERT_SCHEME)
                    .addParameter("scheme_id", entry.getSchemeId())
                    .addParameter("limits", entry.getLimits())
                    .addParameter("position_id", entry.getPositionId())
                    .addParameter("gap_id", entry.getGapId())
                    .addParameter("date_scheme", entry.getDateScheme())
                    .executeUpdate();

            connection.commit();
        }
    }
}
