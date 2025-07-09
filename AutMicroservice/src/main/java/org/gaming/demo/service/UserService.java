package org.gaming.demo.service;

import lombok.AllArgsConstructor;

import lombok.RequiredArgsConstructor;
import org.gaming.demo.component.RedisCacheUtils;
import org.gaming.demo.dto.RegisterDto;
import org.gaming.demo.dto.UserFullInfo;
import org.gaming.demo.enums.UserRole;
import org.gaming.demo.exceptions.UserAlreadyRegisteredException;
import org.gaming.demo.mappers.UserMapper;
import org.gaming.demo.model.User;
import org.gaming.demo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final RedisCacheUtils redisCacheUtils;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${spring.cache.redis.time-to-lived}")
    private Long cacheTtl;

    @Override
    public User loadUserByUsername(String userName) {
        String fullKey = "user::" + userName;

        User loggedUser;
        if (redisCacheUtils.hasKey(fullKey)) {
            UserFullInfo userFullInfo = redisCacheUtils
                    .getValue(fullKey, UserFullInfo.class);
            logger.info("Юзер с username {} найден в кеше: {}",
                    userName, userFullInfo.toString());
            loggedUser = userMapper.dtoToEntity(userFullInfo);
            return loggedUser;
        }

        loggedUser = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с " +
                        "username " + userName + " не найден."));

        return loggedUser;
    }

    public User registerNewUser(RegisterDto registerDto) {
        logger.info("Попытка регистрации нового пользователя.");
        /**
         * Проверка наличия такой учетки.
         */
        if (redisCacheUtils.hasKey("user::" + registerDto.getUserName())
            || userRepository.findByUserName(registerDto.getUserName()).isPresent()) {
            throw new UserAlreadyRegisteredException(registerDto.getUserName());
        }

        User userForSaving = new User();
        userForSaving.setFirstName(registerDto.getFirstName());
        userForSaving.setSecondName(registerDto.getSecondName());
        userForSaving.setMiddleName(registerDto.getMiddleName());
        userForSaving.setUserName(registerDto.getUserName());
        userForSaving.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        userForSaving.setRole(UserRole.USER);
        /**
         * Поиск последнего клиента для порядка сквозного айдишника.
         */
        if (userRepository.count() != 0) {
            logger.info("БД имеет записи.");
            UserFullInfo lastUser;
            if (redisCacheUtils.hasKey("lastRegisteredUser")) {
                logger.info("Последний пользователь есть в кеше.");
                lastUser = redisCacheUtils.getValue("lastRegisteredUser",
                        UserFullInfo.class);
                redisCacheUtils.deleteValue("lastRegisteredUser");
            } else {
                User maybeYouArePresent = userRepository.getLatRegisteredUser().get();
                lastUser = userMapper.entityToDto(maybeYouArePresent);
            }
            userForSaving.setUserId(lastUser.getUserId()+1);
            User savedUser = userRepository.save(userForSaving);
            logger.info("Пользователь успешно сохранен.");
            redisCacheUtils.putValue("lastRegisteredUser",
                    userMapper.entityToDto(savedUser),
                    cacheTtl);
            logger.info("Последний пользователь был кеширован.");
            return savedUser;
        }
        logger.info("БД пока пуста.");
        userForSaving.setUserId(1L);
        User savedUser = userRepository.save(userForSaving);
        logger.info("Новый пользователь был сохранен в БД.");
        redisCacheUtils.putValue("lastRegisteredUser",
                userMapper.entityToDto(savedUser),
                cacheTtl);
        logger.info("Последний пользователь был кеширован.");
        cacheUserFullInfo(savedUser);
        return savedUser;
    }

    private void cacheUserFullInfo(User userForCache) {
        UserFullInfo userFullInfo = userMapper.entityToDto(userForCache);
        logger.info("Попытка кеширования полной инфы по юзеру.");
        String fullKey = "user::" + userFullInfo.getUserName();
        try {
            redisCacheUtils.putValue(fullKey, userFullInfo, cacheTtl);
            logger.info("Объект был кеширован.");
        } catch (RuntimeException e) {
            logger.error("Ошибка кеширования: {}", e.getMessage());
        }

    }
}
