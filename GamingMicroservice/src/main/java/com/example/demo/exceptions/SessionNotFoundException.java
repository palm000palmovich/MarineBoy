package com.example.demo.exceptions;

public class SessionNotFoundException extends RuntimeException
{
    public SessionNotFoundException(Long sessId) {
        super("Сессия с id " + sessId + " не найдена.");
    }
}
