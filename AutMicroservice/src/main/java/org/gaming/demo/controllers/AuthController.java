package org.gaming.demo.controllers;

import gorb.vars.dto.NewUserInfoResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.gaming.demo.component.JwtUtil;
import org.gaming.demo.dto.JwtResponse;
import org.gaming.demo.dto.LoginDto;
import org.gaming.demo.dto.RegisterDto;
import org.gaming.demo.exceptions.SendingToGamingServiceException;
import org.gaming.demo.exceptions.UserAlreadyRegisteredException;
import org.gaming.demo.model.User;
import org.gaming.demo.service.ToGamingMmService;
import org.gaming.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping(path = "/auth")
@AllArgsConstructor
public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final ToGamingMmService toGamingMmService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody LoginDto login) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getUserName(), login.getPassword())
            );
        } catch (Exception e) {
            logger.error("Ошибка при входе: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Incorrect username or password");
        }

        final User user = userService.loadUserByUsername(login.getUserName());
        final JwtResponse jwtResponse = new JwtResponse(jwtUtil.generateToken(user));

        NewUserInfoResponse response;
        try {
            response = toGamingMmService.sendUserInfo(login.getUserName());
            logger.info("Полученный ответ от второго сервиса: {}", response.toString());
        } catch (SendingToGamingServiceException exep) {
            logger.error(exep.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something shit with sending to 2nd service.");
        }
        if (!response.isOk()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Проблема сохранения игрового аккаунта");
        }
        logger.info("Полученный jwt: {}", jwtResponse.toString());
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDto registerDto) {
        try {
            return ResponseEntity.ok(userService.registerNewUser(registerDto));
        } catch (UserAlreadyRegisteredException exception){
            logger.error(exception.getMessage());
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }


}
