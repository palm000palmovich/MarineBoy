package com.example.demo.controller;

import com.example.demo.dto.GameResultDto;
import com.example.demo.service.GameProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/game/process")
public class GameProcessController {
    private final GameProcessService gameProcessService;
    private Logger logger = LoggerFactory.getLogger(GameProcessController.class);

    @PostMapping(path = "/make-move/{sessionId}/{nickName}/{x}/{y}")
    public ResponseEntity<GameResultDto> makeMove(@PathVariable("sessionId") Long sessId,
                                                  @PathVariable("nickName")  String nickName,
                                                  @PathVariable("x") int x,
                                                  @PathVariable("y") int y) {
        try {
            return ResponseEntity.ok(gameProcessService.makeMove(sessId, nickName, x, y));
        } catch (RuntimeException exep) {
            logger.error(exep.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
