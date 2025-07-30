package com.example.demo.controller;

import com.example.demo.service.SaveGameAccountService;
import gorb.vars.dto.NewUserInfoRequest;
import gorb.vars.dto.NewUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/saveevas")
@RequiredArgsConstructor
public class SaveGameAccountController {
    private final SaveGameAccountService saveGameAccountService;

    @PostMapping(path = "/game-account")
    public ResponseEntity<NewUserInfoResponse> saveGameAcc(@RequestBody(required = true) NewUserInfoRequest request,
                                                           @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(saveGameAccountService.saveAccount(request));
    }
}
