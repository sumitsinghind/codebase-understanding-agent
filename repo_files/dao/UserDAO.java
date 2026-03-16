package com.memorymaze.dao;

import com.memorymaze.dto.LeaderboardDto;
import com.memorymaze.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> rowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("ID"));
        user.setUsername(rs.getString("USERNAME"));
        user.setPassword(rs.getString("PASSWORD"));
        user.setRole(rs.getString("ROLE"));
        user.setBanned(rs.getBoolean("BANNED"));
        user.setScore(rs.getInt("SCORE"));
        user.setGamesPlayed(rs.getInt("GAMES_PLAYED")); // Correct mapping
        user.setRegion(rs.getString("REGION"));
        user.setCreatedAt(rs.getTimestamp("CREATED_AT") != null
                ? rs.getTimestamp("CREATED_AT").toLocalDateTime()
                : null);
        user.setCurrentLevel(rs.getInt("CURRENT_LEVEL"));
        user.setLevelScore(rs.getInt("LEVEL_SCORE"));
        user.setTotalScore(rs.getInt("TOTAL_SCORE"));
        user.setMoves(rs.getInt("MOVES"));
        user.setTimeSpent(rs.getInt("TIME_SPENT"));
        user.setGameInProgress(rs.getBoolean("GAME_IN_PROGRESS"));
        return user;
    };

    public void updateRegion(String username, String region) {
        String sql = "UPDATE users SET REGION = ? WHERE USERNAME = ?";
        jdbcTemplate.update(sql, region, username);
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE USERNAME = ?";
        List<User> users = jdbcTemplate.query(sql, rowMapper, username);
        return users.isEmpty() ? null : users.get(0);
    }

    public List<User> findAllUsers() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (USERNAME, PASSWORD, ROLE, BANNED, SCORE, GAMES_PLAYED, REGION) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int rows = jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.isBanned(),
                user.getScore() != null ? user.getScore() : 0,
                user.getGamesPlayed() != null ? user.getGamesPlayed() : 0,
                user.getRegion());
        return rows > 0;
    }

    public void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE ID = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void updateUser(User user) {
        boolean updatePassword = user.getPassword() != null && !user.getPassword().isBlank();

        StringBuilder sql = new StringBuilder("UPDATE users SET ");
        List<Object> params = new ArrayList<>();

        if (updatePassword) {
            sql.append("`PASSWORD` = ?, ");
            params.add(user.getPassword());
        }

        sql.append("`ROLE` = ?, `BANNED` = ?, `SCORE` = ?, `GAMES_PLAYED` = ?, `REGION` = ?, ");
        sql.append(
                "`CURRENT_LEVEL` = ?, `LEVEL_SCORE` = ?, `TOTAL_SCORE` = ?, `MOVES` = ?, `TIME_SPENT` = ?, `GAME_IN_PROGRESS` = ? ");
        sql.append("WHERE `USERNAME` = ?");

        params.add(user.getRole());
        params.add(user.isBanned());
        params.add(user.getScore());
        params.add(user.getGamesPlayed());
        params.add(user.getRegion());
        params.add(user.getCurrentLevel());
        params.add(user.getLevelScore());
        params.add(user.getTotalScore());
        params.add(user.getMoves());
        params.add(user.getTimeSpent());
        params.add(user.getGameInProgress());
        params.add(user.getUsername());

        // DEBUG LOGS
        System.out.println("Updating user in DB:");
        System.out.println("SQL: " + sql);
        System.out.println("Params: " + params);
        System.out.println("GAMES_PLAYED (param index): " + user.getGamesPlayed());

        int rows = jdbcTemplate.update(sql.toString(), params.toArray());
        System.out.println("Rows affected: " + rows);
    }

    public User loginUser(String username, String rawPassword) {
        User user = findByUsername(username);
        if (user != null && org.mindrot.jbcrypt.BCrypt.checkpw(rawPassword, user.getPassword())) {
            return user;
        }
        return null;
    }

    public List<LeaderboardDto> findTopScorersDto() {
        String sql = "SELECT USERNAME, REGION, TOTAL_SCORE AS SCORE, GAMES_PLAYED, CURRENT_LEVEL AS level, TIME_SPENT, MOVES FROM users WHERE BANNED = 0 ORDER BY TOTAL_SCORE DESC LIMIT 10";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new LeaderboardDto(
                rs.getString("USERNAME"),
                rs.getString("REGION"),
                rs.getInt("SCORE"),
                rs.getInt("GAMES_PLAYED"),
                rs.getInt("level"),
                rs.getInt("TIME_SPENT"),
                rs.getInt("MOVES")));
    }

    public List<User> findTopScorers() {
        String sql = "SELECT * FROM users WHERE BANNED = 0 ORDER BY TOTAL_SCORE DESC LIMIT 10";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
