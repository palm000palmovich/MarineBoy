package com.example.demo.service;

import com.example.demo.component.RedisCacheUtils;
import com.example.demo.dto.*;
import com.example.demo.enums.GameStatus;
import com.example.demo.enums.MoveResult;
import com.example.demo.exceptions.SessionNotFoundException;
import com.example.demo.exceptions.UncorrectUserException;
import com.example.demo.exceptions.WrongCoordinatesException;
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

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GameProcessService {
    private final GameSessionRepository gameSessionRepository;
    private final GamingFieldRepository gamingFieldRepository;
    private final RedisCacheUtils redisCacheUtils;

    @Value("${spring.cache.redis.verySimpleShit.time-to-lived}")
    private short ttlForLastMove;
    @Value("${spring.cache.redis.simpleShit.time-to-lived}")
    private Long ttlForGameObjects;

    private Logger logger = LoggerFactory.getLogger(GameProcessService.class);

    public GameResultDto makeMove(Long sessionId, String nickname, int x, int y) {
        String lastMoveKey = "lastMoveInSession::" + sessionId;
        String gameSessionKey = "game::session::" + sessionId;

        //Последний ход.
        if (redisCacheUtils.hasKey(lastMoveKey)) {
            LastShotResult lastShotResult = redisCacheUtils.getValue(lastMoveKey,
                    LastShotResult.class);
            logger.info("Последний ход из кеша: {}", lastShotResult.toString());
            if ((lastShotResult.getMoveResult().name().equals("MISS")   //(последний выстрел - промах
                && lastShotResult.getNickName().equals(nickname)) //И мазила опять пытается пальнуть)
                    || (!lastShotResult.getMoveResult().name().equals("MISS") //ИЛИ (последний выстрел - попадание/убийство
                    && !lastShotResult.getNickName().equals(nickname))) {   //И палит оппонент))
                throw new UncorrectUserException(nickname);  //WARNING nahoy!
            }
        }

        //Поиск сессии КЕШ/БД
        GameSessionCacheDto gameSessionCacheDto;
        if (redisCacheUtils.hasKey(gameSessionKey)) {
            gameSessionCacheDto = redisCacheUtils.getValue(gameSessionKey, GameSessionCacheDto.class);
            logger.info("Найденная сессия в кеше: {}", gameSessionCacheDto.toString());
        } else {
            logger.info("Игровая сессия не найдена в кеше, поиск в БД.");
            gameSessionCacheDto = loadFromDatabaseAndCache(sessionId);
        }

        if (!gameSessionCacheDto.getStatus().toString().equals("IN_PROGRESS")) {
            throw new RuntimeException("Game is not in progress!");
        }

        //Определяем оппонента.
        String opponentNickname = gameSessionCacheDto.getPlayerOneNickname()
                .equals(nickname) ? gameSessionCacheDto.getPlayerTwoNickname() :
                gameSessionCacheDto.getPlayerOneNickname();
        logger.info("Оппонент: {}", opponentNickname);

        //Поле оппонента
        FieldCacheDto opponentFieldDto = gameSessionCacheDto.getPlayerFields().get(opponentNickname);
        String[][] opponentField = opponentFieldDto.getField();

        //Обработка хода
        GameResultDto result = processShot(gameSessionCacheDto, nickname,
                opponentNickname, opponentField, x, y);

        //Обновление кеша
        redisCacheUtils.putValue(gameSessionKey, gameSessionCacheDto, ttlForGameObjects);
        LastShotResult lastShot = new LastShotResult();
        lastShot.setNickName(nickname);
        lastShot.setMoveResult(result.getMoveResultMessage());
        redisCacheUtils.putValue(lastMoveKey, lastShot, ttlForLastMove);


        if (result.isGameOver() || result.getMoveResultMessage() == MoveResult.KILL) {
            saveToDatabase(sessionId, gameSessionCacheDto);
            // Очищаем кэш после завершения
            if (result.isGameOver()) {
                redisCacheUtils.deleteValue(gameSessionKey);
                redisCacheUtils.deleteValue(lastMoveKey);
            }
        }
        return result;
    }

    private GameSessionCacheDto loadFromDatabaseAndCache(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        GamingField field1 = gamingFieldRepository.findByGamerAndGameSession(session.getPlayerOne(), session)
                .orElseThrow(() -> new RuntimeException("Field 1 not found"));
        GamingField field2 = gamingFieldRepository.findByGamerAndGameSession(session.getPlayerTwo(), session)
                .orElseThrow(() -> new RuntimeException("Field 2 not found"));

        ShipDistribution dist1 = new Gson().fromJson(field1.getFieldData(), ShipDistribution.class);
        ShipDistribution dist2 = new Gson().fromJson(field2.getFieldData(), ShipDistribution.class);

        GameSessionCacheDto dto = new GameSessionCacheDto();
        dto.setSessionId(session.getId());
        dto.setPlayerOneNickname(session.getPlayerOne().getNickname());
        dto.setPlayerTwoNickname(session.getPlayerTwo().getNickname());
        dto.setStatus(session.getStatus());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setFinishedAt(session.getFinishedAt());
        dto.setWinnerNickname(session.getWinner() != null ? session.getWinner().getNickname() : null);

        //nickName: field - такая логика
        Map<String, FieldCacheDto> fields = new HashMap<>();
        fields.put(session.getPlayerOne().getNickname(), new FieldCacheDto(field1.getId(), dist1.getField()));
        fields.put(session.getPlayerTwo().getNickname(), new FieldCacheDto(field2.getId(), dist2.getField()));
        dto.setPlayerFields(fields);

        logger.info("Сессия из БД: {}", dto.toString());
        try {
            redisCacheUtils.putValue("game::session::" + sessionId, dto, ttlForGameObjects);
            logger.info("Сессия была кеширована.");
        } catch (RuntimeException exception) {
            logger.error("Проблема кеширования: {}", exception.getMessage());
        }
        return dto;
    }

    private GameResultDto processShot(GameSessionCacheDto cacheDto, String nickname,
                                              String opponentNickname, String[][] field,
                                              int x, int y) {
        logger.info("{} шарахнул в ({}, {})", nickname, x, y);
        if (x < 0 || x >= 10 || y < 0 || y >= 10) {
            throw new WrongCoordinatesException(x, y);
        }
        if ("X".equals(field[x][y]) || "M".equals(field[x][y])) {
            throw new IllegalStateException("Cell already attacked");
        }

        if ("*".equals(field[x][y])) {
            field[x][y] = "X";
            Set<Point> shipCells = findShipCells(field, x, y);
            boolean destroyed = shipCells.stream().allMatch(p -> "X".equals(field[p.getX()][p.getY()]));

            if (destroyed) {
                boolean allDestroyed = isAllShipsDestroyed(field);
                if (allDestroyed) {
                    cacheDto.setStatus(GameStatus.FINISHED);
                    cacheDto.setWinnerNickname(nickname);
                    cacheDto.setFinishedAt(LocalDateTime.now());
                    return new GameResultDto(MoveResult.WIN, true, nickname);
                }
                return new GameResultDto(MoveResult.KILL, false, null);
            }
            return new GameResultDto(MoveResult.HIT, false, null);
        } else {
            field[x][y] = "M";
            return new GameResultDto(MoveResult.MISS, false, null);
        }
    }

    private Set<Point> findShipCells(String[][] field, int x, int y) {
        Set<Point> cells = new HashSet<>();
        Deque<Point> stack = new ArrayDeque<>();
        stack.push(new Point(x, y));
        while (!stack.isEmpty()) {
            Point p = stack.pop();
            if (cells.contains(p)) continue;
            int px = p.getX(), py = p.getY();
            if (px >= 0 && px < 10 && py >= 0 && py < 10 &&
                    ("*".equals(field[px][py]) || "X".equals(field[px][py]))) {
                cells.add(p);
                stack.push(new Point(px+1, py));
                stack.push(new Point(px-1, py));
                stack.push(new Point(px, py+1));
                stack.push(new Point(px, py-1));
            }
        }
        return cells;
    }

    private boolean isAllShipsDestroyed(String[][] field) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if ("*".equals(field[i][j])) return false;
            }
        }
        return true;
    }

    private void saveToDatabase(Long sessionId, GameSessionCacheDto cacheDto) {
        GameSession session = gameSessionRepository.findById(sessionId).orElseThrow();

        // Обновляем статус и победителя
        session.setStatus(cacheDto.getStatus());
        session.setFinishedAt(cacheDto.getFinishedAt());
        if (cacheDto.getWinnerNickname() != null) {
            Gamer winner = session.getPlayerOne().getNickname( ).equals(cacheDto.getWinnerNickname()) ?
                    session.getPlayerOne() : session.getPlayerTwo();
            session.setWinner(winner);
        }

        // Сохраняем обновлённые поля
        for (Map.Entry<String, FieldCacheDto> entry : cacheDto.getPlayerFields().entrySet()) {
            Gamer player = session.getPlayerOne().getNickname().equals(entry.getKey()) ?
                    session.getPlayerOne() : session.getPlayerTwo();
            GamingField field = gamingFieldRepository.findByGamerAndGameSession(player, session).orElseThrow();
            ShipDistribution dist = new ShipDistribution();
            dist.setField(entry.getValue().getField());
            field.setFieldData(new Gson().toJson(dist));
            gamingFieldRepository.save(field);
        }

        // Сохраняем сессию только если игра закончилась
        if (cacheDto.getStatus() == GameStatus.FINISHED) {
            gameSessionRepository.save(session);
        }
    }

}
