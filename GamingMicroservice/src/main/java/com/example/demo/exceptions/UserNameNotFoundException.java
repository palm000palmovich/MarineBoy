package com.example.demo.exceptions;

public class UserNameNotFoundException extends RuntimeException {
    public UserNameNotFoundException(String userName) {
        super(userName + " не найден.");
    }
}
