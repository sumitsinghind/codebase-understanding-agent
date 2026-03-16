package com.memorymaze.service;

import com.memorymaze.dao.GameSessionRepository;
import com.memorymaze.dao.UserDAO;
import com.memorymaze.exception.GameSessionException;
import com.memorymaze.model.GameSession;
import com.memorymaze.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final UserDAO userDAO;

    @Autowired
    public GameSessionService(GameSessionRepository gameSessionRepository, UserDAO userDAO) {
        this.gameSessionRepository = gameSessionRepository;
        this.userDAO = userDAO;
    }

    public GameSession findSessionByIdAndUsername(int sessionId, String username) {
        return gameSessionRepository.findByIdAndUsername(sessionId, username)
                .orElseThrow(() -> new GameSessionException("Session not found for user: " + username));
    }

    @Transactional
    public GameSession startNewSession(String username) {
        GameSession session = new GameSession();
        session.setUsername(username);
        session.setScore(0);
        session.setMoves(0);
        session.setTimeSeconds(0);
        session.setElapsedTime(0L);
        session.setStatus(GameSession.GameStatus.IN_PROGRESS);
        session.setStartTime(LocalDateTime.now());
        session.setLastResumeTime(LocalDateTime.now());
        session.setLevel(1);
        return gameSessionRepository.save(session);
    }

    @Transactional
    public void pauseSession(int sessionId, String username) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GameSessionException("Session not found"));

        if (session.getStatus() == GameSession.GameStatus.PAUSED) {
            return;
        }

        validateSession(session, username, GameSession.GameStatus.IN_PROGRESS);

        LocalDateTime now = LocalDateTime.now();
        Duration activeDuration = Duration.between(session.getLastResumeTime(), now);
        session.setElapsedTime(session.getElapsedTime() + activeDuration.getSeconds());

        session.setStatus(GameSession.GameStatus.PAUSED);
        session.setLastPauseTime(now);
        gameSessionRepository.save(session);
    }

    @Transactional
    public void resumeSession(int sessionId, String username) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GameSessionException("Session not found"));

        if (session.getStatus() == GameSession.GameStatus.IN_PROGRESS) {
            return;
        }

        validateSession(session, username, GameSession.GameStatus.PAUSED);

        session.setStatus(GameSession.GameStatus.IN_PROGRESS);
        session.setLastResumeTime(LocalDateTime.now());
        session.setLastPauseTime(null);
        gameSessionRepository.save(session);
    }

    private void validateSession(GameSession session, String username, GameSession.GameStatus requiredStatus) {
        if (session == null) {
            throw new GameSessionException("Session not found");
        }
        if (!session.getUsername().equals(username)) {
            throw new GameSessionException("Unauthorized access to session");
        }
        if (requiredStatus != null && session.getStatus() != requiredStatus) {
            throw new GameSessionException("Session must be in " + requiredStatus + " state");
        }
    }

    @Transactional
    public void updateScore(int sessionId, String username, int score) {
        GameSession session = findSessionByIdAndUsername(sessionId, username);
        if (session.getStatus() != GameSession.GameStatus.IN_PROGRESS) {
            throw new GameSessionException("Cannot update score: session is not in progress");
        }
        session.setScore(score);
        gameSessionRepository.save(session);
    }

    @Transactional
    public void completeSession(int sessionId, String username, int finalScore, int timeSeconds, int moves) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GameSessionException("Session not found"));
        validateSession(session, username, null);

        if (session.getStatus() == GameSession.GameStatus.IN_PROGRESS) {
            Duration activeDuration = Duration.between(session.getLastResumeTime(), LocalDateTime.now());
            session.setElapsedTime(session.getElapsedTime() + activeDuration.getSeconds());
        }

        session.setScore(finalScore);
        session.setTimeSeconds(timeSeconds);
        session.setMoves(moves);
        session.setStatus(GameSession.GameStatus.COMPLETED);
        // Ensure session.setLevel(...) is set before this!
        gameSessionRepository.save(session);

        User user = userDAO.findByUsername(username);
        if (user != null) {
            int currentTotalScore = user.getTotalScore() != null ? user.getTotalScore() : 0;
            int currentGames = user.getGamesPlayed() != null ? user.getGamesPlayed() : 0;
            int currentTime = user.getTimeSpent() != null ? user.getTimeSpent() : 0;
            int currentMoves = user.getMoves() != null ? user.getMoves() : 0;

            // Cumulative stats
            user.setTotalScore(currentTotalScore + finalScore);
            user.setGamesPlayed(currentGames + 1);
            user.setTimeSpent(currentTime + timeSeconds);
            user.setMoves(currentMoves + moves);

            // Latest game stats
            user.setScore(finalScore);
            user.setCurrentLevel(session.getLevel());

            userDAO.updateUser(user);

            System.out.println("Updating user: "
                    + "username=" + user.getUsername()
                    + ", SCORE=" + user.getScore()
                    + ", TOTAL_SCORE=" + user.getTotalScore()
                    + ", GAMES_PLAYED=" + user.getGamesPlayed()
                    + ", CURRENT_LEVEL=" + user.getCurrentLevel()
                    + ", MOVES=" + user.getMoves()
                    + ", TIME_SPENT=" + user.getTimeSpent());
        }
    }

    public List<GameSession> getLeaderboardSorted(int limit) {
        List<GameSession> sessions = gameSessionRepository
                .findLatestCompletedSessionsForAllUsers(GameSession.GameStatus.COMPLETED);
        return sessions.stream()
                .sorted(Comparator.comparingInt(GameSession::getScore).reversed()
                        .thenComparingInt(GameSession::getTimeSeconds)
                        .thenComparingInt(GameSession::getMoves))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<GameSession> getUserSessions(String username) {
        return gameSessionRepository.findByUsername(username);
    }
}
