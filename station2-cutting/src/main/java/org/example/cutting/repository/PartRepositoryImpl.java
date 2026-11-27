package org.example.cutting.repository;

import org.example.cutting.model.PartRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Repository
public class PartRepositoryImpl implements PartRepository {

    private static final RowMapper<PartRecord> ROW_MAPPER = (rs, rowNum) -> new PartRecord(
            rs.getInt("id"),
            rs.getString("part_type"),
            rs.getDouble("weight_kg"),
            rs.getString("animal_reg_no"),
            rs.getInt("tray_id")
    );

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public PartRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PartRecord save(String partType, double weightKg, String animalRegNo, int trayId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO part (part_type, weight_kg, animal_reg_no, tray_id) VALUES (?, ?, ?, ?)",
                    new String[]{"id"});
            ps.setString(1, partType);
            ps.setDouble(2, weightKg);
            ps.setString(3, animalRegNo);
            ps.setInt(4, trayId);
            return ps;
        }, keyHolder);

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        return new PartRecord(id, partType, weightKg, animalRegNo, trayId);
    }

    @Override
    public List<PartRecord> findByTrayId(int trayId) {
        return jdbcTemplate.query(
                "SELECT id, part_type, weight_kg, animal_reg_no, tray_id FROM part WHERE tray_id = ? ORDER BY id",
                ROW_MAPPER, trayId);
    }

    @Override
    public List<String> findAnimalRegNosByTrayIds(List<Integer> trayIds) {
        if (trayIds.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(", ", Collections.nCopies(trayIds.size(), "?"));
        String sql = "SELECT DISTINCT animal_reg_no FROM part WHERE tray_id IN (" + placeholders + ") ORDER BY animal_reg_no";
        return jdbcTemplate.queryForList(sql, String.class, trayIds.toArray());
    }
}
