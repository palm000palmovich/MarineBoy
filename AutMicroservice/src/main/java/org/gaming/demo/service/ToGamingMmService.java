package org.gaming.demo.service;

import gorb.vars.dto.NewUserInfoRequest;
import gorb.vars.dto.NewUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.gaming.demo.component.JwtUtil;
import org.gaming.demo.exceptions.SendingToGamingServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
@RequiredArgsConstructor
public class ToGamingMmService {
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private Logger logger = LoggerFactory.getLogger(ToGamingMmService.class);

    private static final String GAMING_SERVICE_URL = "http://localhost:8081/saveevas/game-account";

    public NewUserInfoResponse sendUserInfo(String nickName) {
        String serviceToken = jwtUtil.generateServiceToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + serviceToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        NewUserInfoRequest newUserInfoRequest = new NewUserInfoRequest(nickName);
        logger.info("Данные для отправки: {}", newUserInfoRequest.toString());

        HttpEntity<NewUserInfoRequest> request = new HttpEntity<>(
                newUserInfoRequest, headers);

        ResponseEntity<NewUserInfoResponse> response;
        try {
            response = restTemplate.postForEntity(
                    GAMING_SERVICE_URL,
                    request,
                    NewUserInfoResponse.class);
        } catch (Exception e) {
            throw new SendingToGamingServiceException(e.getMessage());
        }
        return response.getBody();
    }
}
