package org.example.pro3assignment2.repository;

import org.example.pro3assignment2.model.ProductRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ProductRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ProductRecord> findByAnimalRegNo(String regNo) {
        // animal -> parts -> trays -> products
        String sql = """
                SELECT DISTINCT pr.id, pr.product_type
                FROM product pr
                JOIN product_tray pt ON pt.product_id = pr.id
                JOIN part p ON p.tray_id = pt.tray_id
                WHERE p.animal_reg_no = ?
                ORDER BY pr.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ProductRecord(
                rs.getLong("id"),
                rs.getString("product_type")
        ), regNo);
    }
}
