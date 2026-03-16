package com.memorymaze.controller;

import com.memorymaze.model.GameSession;
import com.memorymaze.service.GameSessionService;
import com.memorymaze.service.UserService;
import com.memorymaze.dto.GameSessionResponse;
import com.memorymaze.dto.LeaderboardDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameSessionService gameSessionService;
    private final UserService userService;

    @Autowired
    public GameController(GameSessionService gameSessionService, UserService userService) {
        this.gameSessionService = gameSessionService;
        this.userService = userService;
    }

    @PostMapping("/start")
    public ResponseEntity<GameSession> startGame(Authentication authentication) {
        String username = authentication.getName();
        GameSession session = gameSessionService.startNewSession(username);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/pause")
    public ResponseEntity<String> pauseGame(@RequestParam int sessionId, Authentication authentication) {
        try {
            String username = authentication.getName();
            gameSessionService.pauseSession(sessionId, username);
            return ResponseEntity.ok("Game paused");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Pause failed: " + e.getMessage());
        }
    }

    @PostMapping("/resume")
    public ResponseEntity<String> resumeGame(
            @RequestParam int sessionId,
            Authentication authentication) {
        String username = authentication.getName();
        try {
            gameSessionService.resumeSession(sessionId, username);
            return ResponseEntity.ok("Game resumed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid session: " + e.getMessage());
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<String> submitGame(
            @RequestParam int sessionId,
            @RequestParam int finalScore,
            @RequestParam int timeSeconds,
            @RequestParam int moves,
            Authentication authentication) {
        String username = authentication.getName();
        try {
            // Only call this—do not double-update stats
            gameSessionService.completeSession(sessionId, username, finalScore, timeSeconds, moves);
            return ResponseEntity.ok("Game completed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Submission failed: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateScore(
            @RequestParam int sessionId,
            @RequestParam int score,
            Authentication authentication) {
        String username = authentication.getName();
        try {
            gameSessionService.updateScore(sessionId, username, score);
            return ResponseEntity.ok("Score updated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Score update failed: " + e.getMessage());
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardDto>> getLeaderboard() {
        List<LeaderboardDto> leaderboard = userService.getTopScorers();
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/my-sessions")
    public ResponseEntity<List<GameSession>> getMySessions(Authentication authentication) {
        String username = authentication.getName();
        List<GameSession> sessions = gameSessionService.getUserSessions(username);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<GameSessionResponse> getSessionById(
            @PathVariable int sessionId,
            Authentication authentication) {
        String username = authentication.getName();
        GameSession session = gameSessionService.findSessionByIdAndUsername(sessionId, username);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        GameSessionResponse response = new GameSessionResponse(
                session.getId(),
                session.getStatus().name(),
                session.getScore(),
                session.getStartTime(),
                session.getLastPauseTime(),
                session.getTimeSeconds(),
                session.getMoves());
        return ResponseEntity.ok(response);
    }
}
