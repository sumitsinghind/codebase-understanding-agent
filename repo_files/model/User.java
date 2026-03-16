package com.memorymaze.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private boolean banned;
    private String region;

    private Integer score;
    @Column(name = "games_played")
    private Integer gamesPlayed;

    // --- New fields for level/progress ---
    @Column(name = "current_level")
    private Integer currentLevel = 1;

    @Column(name = "level_score")
    private Integer levelScore = 0;

    @Column(name = "total_score")
    private Integer totalScore = 0;

    private Integer moves = 0;

    @Column(name = "time_spent")
    private Integer timeSpent = 0;

    @Column(name = "game_in_progress")
    private Boolean gameInProgress = false;

    public User() {
    }

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // --- Getters and Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(Integer gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    // --- New fields for level/progress ---

    public Integer getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(Integer currentLevel) {
        this.currentLevel = currentLevel;
    }

    public Integer getLevelScore() {
        return levelScore;
    }

    public void setLevelScore(Integer levelScore) {
        this.levelScore = levelScore;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getMoves() {
        return moves;
    }

    public void setMoves(Integer moves) {
        this.moves = moves;
    }

    public Integer getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }

    public Boolean getGameInProgress() {
        return gameInProgress;
    }

    public void setGameInProgress(Boolean gameInProgress) {
        this.gameInProgress = gameInProgress;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", banned=" + banned +
                ", region='" + region + '\'' +
                ", score=" + score +
                ", gamesPlayed=" + gamesPlayed +
                ", currentLevel=" + currentLevel +
                ", levelScore=" + levelScore +
                ", totalScore=" + totalScore +
                ", moves=" + moves +
                ", timeSpent=" + timeSpent +
                ", gameInProgress=" + gameInProgress +
                '}';
    }
}
