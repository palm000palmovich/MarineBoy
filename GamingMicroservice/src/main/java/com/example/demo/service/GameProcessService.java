package com.example.demo.service;

import com.example.demo.component.GameProcessUtils;
import com.example.demo.component.RedisCacheUtils;
import com.example.demo.dto.GameResultDto;
import com.example.demo.dto.LastShotResult;
import com.example.demo.dto.ShipDistribution;
import com.example.demo.enums.GameStatus;
import com.example.demo.exceptions.UncorrectUserException;
import com.example.demo.model.GameSession;
import com.example.demo.model.Gamer;
import com.example.demo.model.GamingField;
import com.example.demo.repository.GameSessionRepository;
import com.example.demo.repository.GamingFieldRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Service
@RequiredArgsConstructor
//TODO тебя надо жестко оптимизировать
public class GameProcessService {
    private final GameSessionRepository gameSessionRepository;
    private final GamingFieldRepository gamingFieldRepository;
    private final GameProcessUtils gameProcessUtils1;
    private final RedisCacheUtils redisCacheUtils;

    @Value("${spring.cache.redis.verySimpleShit.time-to-lived}")
    private short ttlForLastMove;
    @Value("${spring.cache.redis.simpleShit.time-to-lived}")
    private Long ttlForGameObjects;

    private Logger logger = LoggerFactory.getLogger(GameProcessService.class);

    public GameResultDto makeMove(Long sessionId, String nickname, int x, int y) {
        //Последний ход.
        String lastMoveFullKey = "lastMoveInSession::" + sessionId;
        if (redisCacheUtils.hasKey(lastMoveFullKey)) {
            LastShotResult lastShotResult = redisCacheUtils.getValue(lastMoveFullKey,
                    LastShotResult.class);
            logger.info("Последний ход из кеша: {}", lastShotResult.toString());
            if (lastShotResult.getMoveResult().name().equals("MISS")
                && lastShotResult.getNickName().equals(nickname)) {
                throw new UncorrectUserException(nickname);
            }
        }

        GameSession gameSession = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Game session is not found!"));

        if (!gameSession.getStatus().equals(GameStatus.IN_PROGRESS)) {
            throw new RuntimeException("Game is not in progress");
        }

        Gamer currentPlayer = findPlayerInGame(gameSession, nickname);
        Gamer opponent = getOpponent(gameSession, currentPlayer);
        logger.info("Ход игрока {} на ({},{})", nickname, x, y);

        GamingField opponentField = gamingFieldRepository.findByGamerAndGameSession(opponent, gameSession)
                .orElseThrow(() -> new RuntimeException("Opponent field not found"));

        ShipDistribution distribution = new Gson().fromJson(opponentField.getFieldData(),
                ShipDistribution.class);
        String[][] field = distribution.getField();

        logger.info("Выстрел по клетке ({},{}), содержимое: {}", x, y, field[x][y]);

        GameResultDto gameResultDto = gameProcessUtils1.processShot(gameSession,
                currentPlayer, opponentField, distribution, x, y);
        LastShotResult lastShotResult = new LastShotResult();
        lastShotResult.setNickName(nickname);
        lastShotResult.setMoveResult(gameResultDto.getMoveResultMessage());

        if (redisCacheUtils.hasKey(lastMoveFullKey)) {
            redisCacheUtils.deleteValue(lastMoveFullKey);
            cacheLastMove(lastMoveFullKey, lastShotResult);
        } else {
            cacheLastMove(lastMoveFullKey, lastShotResult);
        }

        return gameResultDto;
    }

    /**
     * Для переключения на следующего игрока в случае промаха
     * @param fullKey
     * @param lastShotResult
     */
    private void cacheLastMove(String fullKey, LastShotResult lastShotResult) {
        logger.info("Попытка кеширования последнего хода сессии.");
        try {
            redisCacheUtils.putValue(fullKey, lastShotResult, ttlForLastMove);
            logger.info("Последний ход успешно кеширован.");
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
        }

    }

    private Gamer getOpponent(GameSession gameSession, Gamer currentPlayer) {
        if (gameSession.getPlayerOne().equals(currentPlayer)) {
            return gameSession.getPlayerTwo();
        } else {
            return gameSession.getPlayerOne();
        }
    }

    private Gamer findPlayerInGame(GameSession gameSession, String nickname) {
        if (gameSession.getPlayerOne().getNickname().equals(nickname)) {
            return gameSession.getPlayerOne();
        } else if (gameSession.getPlayerTwo() != null && gameSession.getPlayerTwo().getNickname().equals(nickname)) {
            return gameSession.getPlayerTwo();
        }
        throw new RuntimeException("Player not found in this game session");
    }

    //TODO добавить, чтобы переключалось на другого игрока в случае промаха

}
