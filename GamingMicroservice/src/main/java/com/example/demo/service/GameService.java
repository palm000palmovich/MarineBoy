package com.example.demo.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class GameService {
    @Autowired
    private GamerRepository gamerRepository;
    @Autowired
    private GameSessionRepository gameSessionRepository;
    @Autowired
    private GamingFieldRepository gamingFieldRepository;

    public GameSession createGame(String userName, GameType type) {
        Gamer gamer = gamerRepository.findByNickname(userName) //TODO тут из jwt я достаю ник
                .orElseThrow(() -> new UserNameNotFoundException(userName));

        GameSession gameSession = new GameSession();
        gameSession.setPlayerOne(gamer);
        gameSession.setType(type);
        gameSession.setStatus(GameStatus.WAITING_FOR_PLAYER);
        gameSession.setCreatedAt(LocalDateTime.now());
        gameSessionRepository.save(gameSession); //TODO еще добавляю в кеш

        /**
         * Пустое игровое поле для первого игрока.
         */
        GamingField field = new GamingField();
        field.setGamer(gamer);
        field.setGameSession(gameSession);
        field.setFieldData("{}"); // Пустое поле
        gamingFieldRepository.save(field);

        return gameSession;
    }

    public GameSession joinGame(Long sessionId, String nickname) {
        GameSession gameSession = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId)); //TODO проверяю кеш сперва

        if (gameSession.getPlayerTwo() != null || !gameSession.getStatus().equals(GameStatus.WAITING_FOR_PLAYER)) {
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

        return gameSession;
    }

    public void setupField(Long sessionId, String nickname, String fieldData) {
        GameSession gameSession = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Game session not found"));  //TODO кеш

        Gamer gamer = findPlayerInGame(gameSession, nickname);
        GamingField field = gamingFieldRepository.findByGamerAndGameSession(gamer, gameSession)
                .orElseThrow(() -> new RuntimeException("Field not found"));

        field.setFieldData(fieldData);   //TODO тут не в тупую должно заполняться поле, а сделать условие.
        gamingFieldRepository.save(field);
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
