package org.example.pro3assignment2.repository;

import org.example.pro3assignment2.model.AnimalRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AnimalRepositoryImpl implements AnimalRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public AnimalRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AnimalRecord> findByProductId(long productId) {
        // product -> trays -> parts -> animals
        String sql = """
                SELECT DISTINCT a.reg_no, a.species, a.weight_kg, a.arrival_date
                FROM animal a
                JOIN part p ON p.animal_reg_no = a.reg_no
                JOIN product_tray pt ON pt.tray_id = p.tray_id
                WHERE pt.product_id = ?
                ORDER BY a.reg_no
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new AnimalRecord(
                rs.getString("reg_no"),
                rs.getString("species"),
                rs.getDouble("weight_kg"),
                rs.getDate("arrival_date").toLocalDate()
        ), productId);
    }
}
