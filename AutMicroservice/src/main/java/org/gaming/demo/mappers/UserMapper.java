package org.gaming.demo.mappers;

import org.gaming.demo.dto.UserFullInfo;
import org.gaming.demo.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User dtoToEntity(UserFullInfo userFullInfo) {
        User user = new User();
        user.setId(userFullInfo.getUserId());
        user.setFirstName(userFullInfo.getFirstName());
        user.setSecondName(userFullInfo.getSecondName());
        user.setMiddleName(userFullInfo.getMiddleName());
        user.setUserId(userFullInfo.getUserId());
        user.setUserName(userFullInfo.getUserName());
        user.setPassword(userFullInfo.getPassword());
        user.setRole(userFullInfo.getRole());
        return user;
    }

    public UserFullInfo entityToDto(User user) {
        UserFullInfo userFullInfo = new UserFullInfo();
        userFullInfo.setPrimaryId(user.getId());
        userFullInfo.setFirstName(user.getFirstName());
        userFullInfo.setSecondName(user.getSecondName());
        userFullInfo.setMiddleName(user.getMiddleName());
        userFullInfo.setUserId(user.getUserId());
        userFullInfo.setUserName(user.getUsername());
        userFullInfo.setPassword(user.getPassword());
        userFullInfo.setRole(user.getRole());

        return userFullInfo;
    }
}
