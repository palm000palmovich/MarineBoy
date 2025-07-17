package com.example.demo.controller;

import com.example.demo.annotations.ValidationField;
import com.example.demo.dto.ShipDistribution;
import com.example.demo.enums.GameType;
import com.example.demo.model.GameSession;
import com.example.demo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private Logger logger = LoggerFactory.getLogger(GameController.class);

    @PostMapping("/create")
    public ResponseEntity<GameSession> createGame(@RequestParam String nickname, @RequestParam GameType type) {
        return ResponseEntity
                .ok(gameService.createGame(nickname, type));
    }

    @PostMapping("/{sessionId}/join")
    public ResponseEntity<GameSession> joinGame(@PathVariable Long sessionId,
                                                @RequestParam String nickname) {
        try {
            return ResponseEntity
                    .ok(gameService.joinGame(sessionId, nickname));
        } catch (RuntimeException exep) {
            logger.error(exep.getMessage());
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/{sessionId}/setup")
    public ResponseEntity<?> setupField(@PathVariable Long sessionId,
                                        @RequestParam String nickname,
                                        @ValidationField @RequestBody ShipDistribution shipDistribution) {
        gameService.setupField(sessionId, nickname, shipDistribution);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<GameSession>> getAvailableGames() {
        List<GameSession> availableGames = gameService.getAvailableGames();
        return ResponseEntity.ok(availableGames);
    }
}
