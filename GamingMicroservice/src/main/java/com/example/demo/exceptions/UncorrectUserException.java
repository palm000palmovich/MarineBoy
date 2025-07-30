package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.LOCKED)
public class UncorrectUserException extends RuntimeException {
  public UncorrectUserException(String nickName) {
      super("Пользователь с nickname" + nickName + " уже сделал ход.");
  }
}
