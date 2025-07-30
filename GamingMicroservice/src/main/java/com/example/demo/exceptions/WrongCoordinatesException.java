package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WrongCoordinatesException extends RuntimeException {
    public WrongCoordinatesException(int x, int y) {
        super("Некорректные координаты: " + "(" + x + "; " + y + ")");
    }
}
