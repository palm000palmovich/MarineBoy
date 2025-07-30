package com.example.demo.service;

import com.example.demo.component.RedisCacheUtils;
import com.example.demo.model.Gamer;
import com.example.demo.repository.GamerRepository;
import gorb.vars.dto.NewUserInfoRequest;
import gorb.vars.dto.NewUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class SaveGameAccountService {
    private final GamerRepository gamerRepository;
    private final RedisCacheUtils redisCacheUtils;
    private final String accountKey = "GAME::ACCOUNT::";
    private Logger logger = LoggerFactory.getLogger(SaveGameAccountService.class);

    @Value("${spring.cache.redis.compositeShit.time-to-lived}")
    private Long ttlForAccount;

    public NewUserInfoResponse saveAccount(NewUserInfoRequest req) {
        logger.info("Попытка сохранения в бд нового пользователя {}", req.getNickName());
        NewUserInfoResponse newUserInfoResponse = new NewUserInfoResponse();

        if (checkAvailability(req)) {
            logger.info("Такой игровой акк уже создан.");
            newUserInfoResponse.setOk(false);
            return newUserInfoResponse;
        }

        try {
            Gamer gamer = new Gamer();
            gamer.setNickname(req.getNickName());
            gamerRepository.save(gamer);
            logger.info("Новый игрок успешно сохранен в БД.");
            cacheRequest(req);
            newUserInfoResponse.setOk(true);
        } catch (Exception exep) {
            logger.error("Проблема сохранения нового геймера: {}", exep.getMessage());
            newUserInfoResponse.setOk(false);
        }

        return newUserInfoResponse;
    }

    private boolean checkAvailability(NewUserInfoRequest req) {
        if (redisCacheUtils.hasKey(accountKey + req.getNickName())) {
            return true;
        } else if (gamerRepository.checkAccountsAvailability(req.getNickName())) {
            redisCacheUtils.putValue(accountKey + req.getNickName(), req, ttlForAccount);
            return true;
        }
        return false;
    }

    private void cacheRequest(NewUserInfoRequest req) {
        logger.info("Попытка кеширования инфы по аккаунту.");
        try {
            redisCacheUtils.putValue(accountKey + req.getNickName(),
                    req, ttlForAccount);
            logger.info("Сущность успешно кеширована.");
        } catch (RuntimeException ex) {
            logger.error(ex.getMessage());
        }
    }
}
