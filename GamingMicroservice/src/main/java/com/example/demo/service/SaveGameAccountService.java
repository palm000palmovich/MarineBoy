package com.example.demo.service;

import com.example.demo.model.Gamer;
import com.example.demo.repository.GamerRepository;
import gorb.vars.dto.NewUserInfoRequest;
import gorb.vars.dto.NewUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class SaveGameAccountService {
    private final GamerRepository gamerRepository;
    private Logger logger = LoggerFactory.getLogger(SaveGameAccountService.class);

    public NewUserInfoResponse saveAccount(NewUserInfoRequest req) {
        logger.info("Попытка сохранения в бд нового пользователя {}", req.getNickName());

        NewUserInfoResponse newUserInfoResponse = new NewUserInfoResponse();
        try {
            Gamer gamer = new Gamer();
            gamer.setNickname(req.getNickName());
            gamerRepository.save(gamer);
            logger.info("Новый игрок успешно сохранен.");
            newUserInfoResponse.setOk(true);
        } catch (Exception exep) {
            logger.error("Проблема сохранения нового геймера: {}", exep.getMessage());
            newUserInfoResponse.setOk(false);
        }

        return newUserInfoResponse;
    }
}
