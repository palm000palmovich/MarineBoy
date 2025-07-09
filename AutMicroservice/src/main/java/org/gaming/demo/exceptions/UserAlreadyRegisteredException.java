package org.gaming.demo.exceptions;

import org.gaming.demo.dto.RegisterDto;

public class UserAlreadyRegisteredException extends RuntimeException{
    public UserAlreadyRegisteredException(String userName){
        super("login " + userName + " уже зарегистрирован");
    }
}
