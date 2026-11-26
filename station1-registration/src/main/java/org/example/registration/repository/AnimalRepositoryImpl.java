package org.example.registration.repository;

import org.example.registration.model.AnimalRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class AnimalRepositoryImpl implements AnimalRepository {

    private static final String COLUMNS = "reg_no, species, weight_kg, arrival_date, origin";

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
    public Optional<AnimalRecord> findByRegistrationNumber(String regNo) {
        String sql = "SELECT " + COLUMNS + " FROM animal WHERE reg_no = ?";

        List<AnimalRecord> results = jdbcTemplate.query(sql, ROW_MAPPER, regNo);
        return results.stream().findFirst();
    }

    @Override
    public List<AnimalRecord> findByRegistrationNumbers(List<String> regNos) {
        if (regNos.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(", ", Collections.nCopies(regNos.size(), "?"));
        String sql = "SELECT " + COLUMNS + " FROM animal WHERE reg_no IN (" + placeholders + ") ORDER BY reg_no";

        return jdbcTemplate.query(sql, ROW_MAPPER, regNos.toArray());
    }

    @Override
    public List<AnimalRecord> findByArrivalDate(LocalDate date) {
        String sql = "SELECT " + COLUMNS + " FROM animal WHERE arrival_date = ? ORDER BY reg_no";

        return jdbcTemplate.query(sql, ROW_MAPPER, date);
    }

    @Override
    public List<AnimalRecord> findByOrigin(String origin) {
        String sql = "SELECT " + COLUMNS + " FROM animal WHERE origin = ? ORDER BY reg_no";

        return jdbcTemplate.query(sql, ROW_MAPPER, origin);
    }

    @Override
    public void save(AnimalRecord animal) {
        String sql = "INSERT INTO animal (" + COLUMNS + ", forwarded) VALUES (?, ?, ?, ?, ?, FALSE)";

        jdbcTemplate.update(sql,
                animal.regNo(),
                animal.species(),
                animal.weightKg(),
                animal.arrivalDate(),
                animal.origin()
        );
    }

    @Override
    public List<AnimalRecord> findNotForwarded() {
        String sql = "SELECT " + COLUMNS + " FROM animal WHERE forwarded = FALSE ORDER BY reg_no";

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public void markForwarded(String regNo) {
        jdbcTemplate.update("UPDATE animal SET forwarded = TRUE WHERE reg_no = ?", regNo);
    }
}
