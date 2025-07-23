package com.example.demo.service;

import com.example.demo.dto.ShipDistribution;
import com.example.demo.enums.GameStatus;
import com.example.demo.enums.GameType;
import com.example.demo.exceptions.SessionNotFoundException;
import com.example.demo.exceptions.UserNameNotFoundException;
import com.example.demo.model.GameSession;
import com.example.demo.model.Gamer;
import com.example.demo.model.GamingField;
import com.example.demo.repository.GameSessionRepository;
import com.example.demo.repository.GamerRepository;
import com.example.demo.repository.GamingFieldRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class GameSetupService {
    private final GamerRepository gamerRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GamingFieldRepository gamingFieldRepository;

    private Logger logger = LoggerFactory.getLogger(GameSetupService.class);

    public GameSession createGame(String userName, GameType type) {
        logger.info("Создание игровой сессии: userName - {}, gameType - {}",
                userName, type);
        Gamer gamer = gamerRepository.findByNickname(userName) //TODO тут из jwt я достаю ник
                .orElseThrow(() -> new UserNameNotFoundException(userName));

        logger.info("Найденный игрок: {}", gamer.toString());
        GameSession gameSession = new GameSession();
        gameSession.setPlayerOne(gamer);
        gameSession.setType(type);
        gameSession.setStatus(GameStatus.WAITING_FOR_PLAYER);
        gameSession.setCreatedAt(LocalDateTime.now());
        GameSession savedSession = gameSessionRepository.save(gameSession); //TODO еще добавляю в кеш
        logger.info("Новая игровая сессия успешно создана.");

        /**
         * Пустое игровое поле для первого игрока.
         */
        GamingField field = new GamingField();
        field.setGamer(gamer);
        field.setGameSession(gameSession);
        field.setFieldData("{}"); // Пустое поле
        gamingFieldRepository.save(field);
        logger.info("Пустое игровое поле успешно создано.");
        return gameSession;
    }

    public GameSession joinGame(Long sessionId, String nickname) {
        logger.info("Попытка присоединения к игровой сессии: sessId {}, " +
                "nickName {}", sessionId, nickname);
        GameSession gameSession = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId)); //TODO проверяю кеш сперва

        logger.info("Найденная сессия: {}", gameSession.toString());
        if (gameSession.getPlayerTwo() != null ||
                !gameSession.getStatus().equals(GameStatus.WAITING_FOR_PLAYER)) {
            throw new RuntimeException("Игровая сессия уже заполнена!");
        }

        Gamer gamer = gamerRepository.findByNickname(nickname)    //TODO кеш
                .orElseThrow(() -> new UserNameNotFoundException(nickname));

        gameSession.setPlayerTwo(gamer);
        gameSession.startGame();
        gameSessionRepository.save(gameSession);

        /**
         * Пустое игровое поле для второго игрока
         */
        GamingField field = new GamingField();
        field.setGamer(gamer);
        field.setGameSession(gameSession);
        field.setFieldData("{}");
        gamingFieldRepository.save(field);
        logger.info("Игрок {} успешно подключился к игровой сессии с id {}", nickname, sessionId);
        return gameSession;
    }

    public List<GameSession> getAvailableGames() {
        return gameSessionRepository.getAvailableSessions();
    }

    public void setupField(Long sessionId, String nickname, ShipDistribution shipDistribution) {
        logger.info("Попытка создания поля пользователя {} в игровой сессии {}",
                nickname, sessionId);
        GameSession gameSession = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Game session not found"));  //TODO кеш

        Gamer gamer = findPlayerInGame(gameSession, nickname);
        GamingField field = gamingFieldRepository.findByGamerAndGameSession(gamer, gameSession)
                .orElseThrow(() -> new RuntimeException("Field not found"));

        String serializedField = new Gson().toJson(shipDistribution);
        field.setFieldData(serializedField);
        gamingFieldRepository.save(field);
        logger.info("Поле успешно создано.");
    }

    public ShipDistribution getPlayersField(Long sessionId, String nickName) {
        Gamer gamer = gamerRepository.findByNickname(nickName)
                .orElseThrow(() -> new UserNameNotFoundException(nickName));
        GamingField gamingField = gamingFieldRepository.findFieldByGamerIdAndSessionId(gamer.getId(), sessionId)
                .orElseThrow(() -> new RuntimeException("Поле не найдено."));

        return new Gson().fromJson(gamingField.getFieldData(), ShipDistribution.class);
    }


    private Gamer findPlayerInGame(GameSession gameSession, String nickname) {
        if (gameSession.getPlayerOne().getNickname().equals(nickname)) {
            return gameSession.getPlayerOne();
        } else if (gameSession.getPlayerTwo() != null && gameSession.getPlayerTwo().getNickname().equals(nickname)) {
            return gameSession.getPlayerTwo();
        }
        throw new RuntimeException("Player not found in this game session");
    }
}
