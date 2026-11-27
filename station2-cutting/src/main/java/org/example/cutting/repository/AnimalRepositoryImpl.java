package org.example.cutting.repository;

import org.example.cutting.model.AnimalRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AnimalRepositoryImpl implements AnimalRepository {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public AnimalRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean exists(String regNo) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM animal WHERE reg_no = ?", Integer.class, regNo);
        return count != null && count > 0;
    }

    @Override
    public void save(AnimalRecord animal) {
        jdbcTemplate.update("INSERT INTO animal (reg_no, species) VALUES (?, ?)", animal.regNo(), animal.species());
    }
}
