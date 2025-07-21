package com.example.demo.dto;

import com.example.demo.enums.MoveResult;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameResultDto {
    @Enumerated(EnumType.STRING)
    private MoveResult moveResultMessage;
    private boolean isGameOver;
    private String winnerNickName;
}
