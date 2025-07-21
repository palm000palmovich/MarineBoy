package com.example.demo.component;

import com.example.demo.dto.GameResultDto;
import com.example.demo.dto.Point;
import com.example.demo.dto.ShipDistribution;
import com.example.demo.enums.MoveResult;
import com.example.demo.exceptions.WrongCoordinatesException;
import com.example.demo.model.GameSession;
import com.example.demo.model.Gamer;
import com.example.demo.model.GamingField;
import com.example.demo.repository.GameSessionRepository;
import com.example.demo.repository.GamingFieldRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;


@Component
@RequiredArgsConstructor
//TODO тебя надо жестко оптимизировать
public class GameProcessUtils {
    private final GameSessionRepository gameSessionRepository;
    private final GamingFieldRepository gamingFieldRepository;

    private static final String SHIP_CELL = "*";
    private static final String HIT_CELL = "X";
    private static final String MISS_CELL = "M";

    private final Logger logger = LoggerFactory.getLogger(GameProcessUtils.class);

    public GameResultDto processShot(GameSession gameSession, Gamer currentPlayer,
                                     GamingField opponentField, ShipDistribution distribution,
                                     int x, int y) {
        if (!isWithinBounds(x, y)) {
            throw new WrongCoordinatesException(x, y);
        }

        String[][] field = distribution.getField();
        validateCellNotAttacked(field, x, y);

        if (isMiss(field, x, y)) {
            return handleMiss(opponentField, distribution, field, x, y);
        } else {
            return handleHit(gameSession, currentPlayer, opponentField, distribution, field, x, y);
        }
    }

    private GameResultDto handleHit(GameSession gameSession, Gamer currentPlayer,
                                    GamingField opponentField, ShipDistribution distribution,
                                    String[][] field, int x, int y) {
        field[x][y] = HIT_CELL;

        Set<Point> shipCells = findShipCells(field, x, y);
        boolean isShipDestroyed = isShipFullyDestroyed(field, shipCells);

        saveFieldState(opponentField, distribution, field);

        if (isShipDestroyed) {
            if (isAllShipsDestroyed(field)) {
                declareWinner(gameSession, currentPlayer);
                return new GameResultDto(MoveResult.WIN, true, currentPlayer.getNickname());
            }
            return new GameResultDto(MoveResult.KILL, false, null);
        }
        return new GameResultDto(MoveResult.HIT, false, null);
    }

    private GameResultDto handleMiss(GamingField opponentField, ShipDistribution distribution,
                                     String[][] field, int x, int y) {
        field[x][y] = MISS_CELL;
        saveFieldState(opponentField, distribution, field);
        logger.info("Miss at ({},{})", x, y);
        return new GameResultDto(MoveResult.MISS, false, null);
    }


    private Set<Point> findShipCells(String[][] field, int startX, int startY) {
        Set<Point> shipCells = new HashSet<>();
        Deque<Point> stack = new ArrayDeque<>();
        stack.push(new Point(startX, startY));

        while (!stack.isEmpty()) {
            Point current = stack.pop();
            if (shipCells.contains(current)) continue;

            int x = current.getX();
            int y = current.getY();

            if (isWithinBounds(x, y) &&
                    (SHIP_CELL.equals(field[x][y]) || HIT_CELL.equals(field[x][y]))) {
                shipCells.add(current);

                // Добавляем соседние клетки для проверки
                stack.push(new Point(x + 1, y));
                stack.push(new Point(x - 1, y));
                stack.push(new Point(x, y + 1));
                stack.push(new Point(x, y - 1));
            }
        }
        return shipCells;
    }

    private boolean isShipFullyDestroyed(String[][] field, Set<Point> shipCells) {
        return shipCells.stream().allMatch(p -> HIT_CELL.equals(field[p.getX()][p.getY()]));
    }

    private boolean isAllShipsDestroyed(String[][] field) {
        for (String[] row : field) {
            for (String cell : row) {
                if (SHIP_CELL.equals(cell)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void saveFieldState(GamingField field, ShipDistribution distribution, String[][] fieldData) {
        distribution.setField(fieldData);
        field.setFieldData(new Gson().toJson(distribution));
        gamingFieldRepository.save(field);
    }

    private void declareWinner(GameSession gameSession, Gamer winner) {
        gameSession.finishGame(winner);
        gameSessionRepository.save(gameSession);
    }


    private void validateCellNotAttacked(String[][] field, int x, int y) {
        if (HIT_CELL.equals(field[x][y]) || MISS_CELL.equals(field[x][y])) {
            throw new IllegalStateException("Cell already attacked");
        }
    }

    private boolean isMiss(String[][] field, int x, int y) {
        return !SHIP_CELL.equals(field[x][y]);
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }
}
