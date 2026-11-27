package org.example.cutting.repository;

import org.example.cutting.model.TrayRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class TrayRepositoryImpl implements TrayRepository {

    // the current weight of a tray is the sum of the parts in it
    private static final String SELECT_TRAY = """
            SELECT t.id, t.part_type, t.max_weight_kg, t.closed, COALESCE(SUM(p.weight_kg), 0) AS current_weight_kg
            FROM tray t
            LEFT JOIN part p ON p.tray_id = t.id
            """;
    private static final String GROUP_BY = " GROUP BY t.id, t.part_type, t.max_weight_kg, t.closed";

    private static final RowMapper<TrayRecord> ROW_MAPPER = (rs, rowNum) -> new TrayRecord(
            rs.getInt("id"),
            rs.getString("part_type"),
            rs.getDouble("max_weight_kg"),
            rs.getDouble("current_weight_kg"),
            rs.getBoolean("closed")
    );

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public TrayRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<TrayRecord> findById(int id) {
        String sql = SELECT_TRAY + " WHERE t.id = ?" + GROUP_BY;
        return jdbcTemplate.query(sql, ROW_MAPPER, id).stream().findFirst();
    }

    @Override
    public Optional<TrayRecord> findOpenByPartType(String partType) {
        String sql = SELECT_TRAY + " WHERE t.part_type = ? AND t.closed = FALSE" + GROUP_BY + " ORDER BY t.id";
        return jdbcTemplate.query(sql, ROW_MAPPER, partType).stream().findFirst();
    }

    @Override
    public TrayRecord create(String partType, double maxWeightKg) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO tray (part_type, max_weight_kg, closed, forwarded) VALUES (?, ?, FALSE, FALSE)",
                    new String[]{"id"});
            ps.setString(1, partType);
            ps.setDouble(2, maxWeightKg);
            return ps;
        }, keyHolder);

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        return new TrayRecord(id, partType, maxWeightKg, 0, false);
    }

    @Override
    public void close(int id) {
        jdbcTemplate.update("UPDATE tray SET closed = TRUE WHERE id = ?", id);
    }

    @Override
    public List<Integer> findClosedNotForwardedIds() {
        return jdbcTemplate.queryForList(
                "SELECT id FROM tray WHERE closed = TRUE AND forwarded = FALSE ORDER BY id", Integer.class);
    }

    @Override
    public void markForwarded(int id) {
        jdbcTemplate.update("UPDATE tray SET forwarded = TRUE WHERE id = ?", id);
    }

    @Override
    public List<Integer> findIdsByAnimalRegNo(String regNo) {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT tray_id FROM part WHERE animal_reg_no = ? ORDER BY tray_id", Integer.class, regNo);
    }
}
