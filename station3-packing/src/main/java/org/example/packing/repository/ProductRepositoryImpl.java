package org.example.packing.repository;

import org.example.packing.model.ProductRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ProductRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int create(String type) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO product (product_type) VALUES (?)", new String[]{"id"});
            ps.setString(1, type);
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    @Override
    public void addTray(int productId, int trayId) {
        jdbcTemplate.update("INSERT INTO product_tray (product_id, tray_id) VALUES (?, ?)", productId, trayId);
    }

    @Override
    public Optional<ProductRecord> findById(int productId) {
        List<String> types = jdbcTemplate.queryForList(
                "SELECT product_type FROM product WHERE id = ?", String.class, productId);
        if (types.isEmpty()) {
            return Optional.empty();
        }
        List<Integer> partIds = jdbcTemplate.queryForList(
                "SELECT id FROM part WHERE product_id = ? ORDER BY id", Integer.class, productId);
        return Optional.of(new ProductRecord(productId, types.get(0), findTrayIds(productId), partIds));
    }

    @Override
    public List<Integer> findTrayIds(int productId) {
        return jdbcTemplate.queryForList(
                "SELECT tray_id FROM product_tray WHERE product_id = ? ORDER BY tray_id", Integer.class, productId);
    }

    @Override
    public List<ProductRecord> findByTrayIds(List<Integer> trayIds) {
        if (trayIds.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(", ", Collections.nCopies(trayIds.size(), "?"));
        String sql = "SELECT DISTINCT product_id FROM product_tray WHERE tray_id IN (" + placeholders + ") ORDER BY product_id";
        List<Integer> productIds = jdbcTemplate.queryForList(sql, Integer.class, trayIds.toArray());

        return productIds.stream()
                .map(this::findById)
                .flatMap(Optional::stream)
                .toList();
    }
}
