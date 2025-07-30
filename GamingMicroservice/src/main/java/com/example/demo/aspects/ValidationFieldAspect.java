package com.example.demo.aspects;

import com.example.demo.dto.ShipDistribution;
import com.example.demo.exceptions.FieldValidationException;
import org.aspectj.lang.ProceedingJoinPoint;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.*;

@Aspect
@Component
public class ValidationFieldAspect {
    private final int FIELD_SIZE = 10;
    private Logger logger = LoggerFactory.getLogger(ValidationFieldAspect.class);

    @Around("execution(* *(.., @com.example.demo.annotations.ValidationField (*), ..))")
    public Object validateField(ProceedingJoinPoint joinPoint)
            throws Throwable {
        logger.info("Аспект перехватил поле на проверку валидности.");
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof ShipDistribution) {
                ShipDistribution shipDistribution = (ShipDistribution) arg;
                validateShipDistribution(shipDistribution);
            }
        }

        return joinPoint.proceed();
    }

    private void validateShipDistribution(ShipDistribution shipDistribution) {
        String[][] field = shipDistribution.getField();

        /**
         * Размер поля
         */
        if (field.length != FIELD_SIZE
                || Arrays.stream(field).anyMatch(row -> row.length != FIELD_SIZE)) {
            logger.error("Проблема с размером поля.");
            throw new FieldValidationException("Размер поля не совпадает с требованиями.");
        } else {
            logger.info("Field size: OK.");
        }

        /**
         * Количество кораблей
         */
        Map<Integer, Integer> shipCounts = countShips(field);
        if (!isValidShipCount(shipCounts)) {
            logger.error("Проблема с количеством кораблей.");
            throw new FieldValidationException("Количество кораблей не совпадает с требованиями.");
        } else {
            logger.info("Ship count: OK.");
        }

        /**
         * Расстояние между кораблями
         */
        if (!hasRequiredDistance(field)) {
            logger.error("Проблема с расстановкой кораблей.");
            throw new FieldValidationException("Корабли расположены слишком близко друг к другу.");
        } else {
            logger.info("Ship distance: OK.");
        }
    }

    private Map<Integer, Integer> countShips(String[][] field) {
        Map<Integer, Integer> shipCounts = new HashMap<>();
        boolean[][] visited = new boolean[FIELD_SIZE][FIELD_SIZE];
        for (int x = 0; x < FIELD_SIZE; x++) {
            for (int y = 0; y < FIELD_SIZE; y++) {
                if ("*".equals(field[x][y]) && !visited[x][y]) {
                    List<int[]> shipCoordinates = detectShip(field, x, y, visited);
                    shipCounts.put(shipCoordinates.size(),
                            shipCounts.getOrDefault(shipCoordinates.size(), 0) + 1);
                }
            }
        }

        return shipCounts;
    }

    private List<int[]> detectShip(String[][] field, int startX, int startY, boolean[][] visited) {
        List<int[]> shipCoordinates = new ArrayList<>();
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startX, startY});
        visited[startX][startY] = true;

        while (!queue.isEmpty()) {
            int[] coord = queue.poll();
            int x = coord[0];
            int y = coord[1];
            shipCoordinates.add(coord);

            // Проверяем соседние клетки
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                if (isWithinBounds(nx, ny) && "*".equals(field[nx][ny]) && !visited[nx][ny]) {
                    visited[nx][ny] = true;
                    queue.add(new int[]{nx, ny});
                }
            }
        }

        return shipCoordinates;
    }

    private boolean isValidShipCount(Map<Integer, Integer> shipCounts) {
        return shipCounts.getOrDefault(1, 0) == 4 &&
                shipCounts.getOrDefault(2, 0) == 3 &&
                shipCounts.getOrDefault(3, 0) == 2 &&
                shipCounts.getOrDefault(4, 0) == 1;
    }


    private boolean hasRequiredDistance(String[][] field) {
        int[][] directions = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1}, // Вертикаль и горизонталь
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1} // Диагонали
        };

        boolean[][] shipCells = new boolean[FIELD_SIZE][FIELD_SIZE];

        for (int x = 0; x < FIELD_SIZE; x++) {
            for (int y = 0; y < FIELD_SIZE; y++) {
                if ("*".equals(field[x][y])) {
                    shipCells[x][y] = true;
                }
            }
        }

        for (int x = 0; x < FIELD_SIZE; x++) {
            for (int y = 0; y < FIELD_SIZE; y++) {
                if (shipCells[x][y]) {
                    for (int[] dir : directions) {
                        int nx = x + dir[0];
                        int ny = y + dir[1];

                        if (isWithinBounds(nx, ny) && shipCells[nx][ny]) {
                            boolean isSameShip = false;

                            if (Math.abs(dir[0]) + Math.abs(dir[1]) == 1) {
                                isSameShip = true;
                            } else {
                                if ((isWithinBounds(x, ny) && shipCells[x][ny]) ||
                                        (isWithinBounds(nx, y) && shipCells[nx][y])) {
                                    isSameShip = true;
                                }
                            }
                            if (!isSameShip) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < FIELD_SIZE && y >= 0 && y < FIELD_SIZE;
    }

}
