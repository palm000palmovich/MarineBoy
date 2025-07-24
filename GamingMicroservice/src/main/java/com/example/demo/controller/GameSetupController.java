package com.example.demo.controller;

import com.example.demo.annotations.ValidationField;
import com.example.demo.dto.ShipDistribution;
import com.example.demo.enums.GameType;
import com.example.demo.model.GameSession;
import com.example.demo.service.GameSetupService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/gameSetup")
@RequiredArgsConstructor
public class GameSetupController {
    private final GameSetupService gameService;
    private Logger logger = LoggerFactory.getLogger(GameSetupController.class);

    @PostMapping("/create-GameSession")
    public ResponseEntity<GameSession> createGame(Authentication authentication, @RequestParam GameType type) {
        String nickname = authentication.getName();
        return ResponseEntity
                .ok(gameService.createGame(nickname, type));
    }

    @PostMapping("/{sessionId}/join")
    public ResponseEntity<GameSession> joinGame(@PathVariable("sessionId") Long sessionId,
                                                Authentication authentication) {
        String nickname = authentication.getName();
        try {
            return ResponseEntity
                    .ok(gameService.joinGame(sessionId, nickname));
        } catch (RuntimeException exep) {
            logger.error(exep.getMessage());
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/{sessionId}/create-Field")
    public ResponseEntity<?> setupField(@PathVariable Long sessionId,
                                        Authentication authentication,
                                        @ValidationField @RequestBody ShipDistribution shipDistribution) {
        String nickname = authentication.getName();
        gameService.setupField(sessionId, nickname, shipDistribution);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/availableGames")
    public ResponseEntity<List<GameSession>> getAvailableGames() {
        List<GameSession> availableGames = gameService.getAvailableGames();
        return ResponseEntity.ok(availableGames);
    }

    @GetMapping(path = "/gamerField/{sessionId}/{nickName}")
    public ResponseEntity<ShipDistribution> getGamersField(@PathVariable("sessionId") Long sessionId,
                                                           @PathVariable("nickName") String nickName) {
        try {
            return ResponseEntity.ok(gameService.getPlayersField(sessionId, nickName));
        } catch (RuntimeException exep) {
            logger.error(exep.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
