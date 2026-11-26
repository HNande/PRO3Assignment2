package org.example.packing.repository;

import org.example.packing.model.PartRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class TrayRepositoryImpl implements TrayRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public TrayRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean exists(int trayId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tray WHERE id = ?", Integer.class, trayId);
        return count != null && count > 0;
    }

    @Override
    public void save(int trayId, String partType, List<PartRecord> parts) {
        jdbcTemplate.update("INSERT INTO tray (id, part_type) VALUES (?, ?)", trayId, partType);
        for (PartRecord part : parts) {
            jdbcTemplate.update("INSERT INTO part (id, part_type, weight_kg, tray_id) VALUES (?, ?, ?, ?)",
                    part.id(), part.partType(), part.weightKg(), trayId);
        }
    }
}
