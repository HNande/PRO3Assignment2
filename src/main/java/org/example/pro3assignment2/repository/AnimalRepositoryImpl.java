package org.example.pro3assignment2.repository;

import org.example.pro3assignment2.model.AnimalRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class AnimalRepositoryImpl implements AnimalRepository {

    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<AnimalRecord> ROW_MAPPER = (rs, rowNum) -> new AnimalRecord(
            rs.getString("reg_no"),
            rs.getString("species"),
            rs.getDouble("weight_kg"),
            rs.getDate("arrival_date").toLocalDate(),
            rs.getString("origin")
    );

    @Autowired
    public AnimalRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AnimalRecord> findByProductId(int productId) {
        // product -> trays -> parts -> animals
        String sql = """
                SELECT DISTINCT a.reg_no, a.species, a.weight_kg, a.arrival_date, a.origin
                FROM animal a
                JOIN part p ON p.animal_reg_no = a.reg_no
                JOIN product_tray pt ON pt.tray_id = p.tray_id
                WHERE pt.product_id = ?
                ORDER BY a.reg_no
                """;

        return jdbcTemplate.query(sql, ROW_MAPPER, productId);
    }

    @Override
    public Optional<AnimalRecord> findByRegistrationNumber(String regNo) {
        String sql = "SELECT reg_no, species, weight_kg, arrival_date, origin FROM animal WHERE reg_no = ?";

        List<AnimalRecord> results = jdbcTemplate.query(sql, ROW_MAPPER, regNo);
        return results.stream().findFirst();
    }

    @Override
    public List<AnimalRecord> findByArrivalDate(LocalDate date) {
        String sql = "SELECT reg_no, species, weight_kg, arrival_date, origin FROM animal WHERE arrival_date = ? ORDER BY reg_no";

        return jdbcTemplate.query(sql, ROW_MAPPER, date);
    }

    @Override
    public List<AnimalRecord> findByOrigin(String origin) {
        String sql = "SELECT reg_no, species, weight_kg, arrival_date, origin FROM animal WHERE origin = ? ORDER BY reg_no";

        return jdbcTemplate.query(sql, ROW_MAPPER, origin);
    }

    @Override
    public void save(AnimalRecord animal) {
        String sql = "INSERT INTO animal (reg_no, species, weight_kg, arrival_date, origin) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                animal.regNo(),
                animal.species(),
                animal.weightKg(),
                animal.arrivalDate(),
                animal.origin()
        );
    }
}
