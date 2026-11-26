package org.example.packing.repository;

import org.example.packing.model.PartRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class PartRepositoryImpl implements PartRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public PartRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PartRecord> findAvailable(String partType, int limit) {
        String sql = """
                SELECT id, part_type, weight_kg, tray_id
                FROM part
                WHERE part_type = ? AND product_id IS NULL
                ORDER BY tray_id, id
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new PartRecord(
                rs.getInt("id"),
                rs.getString("part_type"),
                rs.getDouble("weight_kg"),
                rs.getInt("tray_id")
        ), partType, limit);
    }

    @Override
    public void assignToProduct(int partId, int productId) {
        jdbcTemplate.update("UPDATE part SET product_id = ? WHERE id = ?", productId, partId);
    }
}
