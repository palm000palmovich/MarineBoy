package com.example.demo.dto;

import com.example.demo.enums.MoveResult;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LastShotResult {
    private String nickName;
    @Enumerated(EnumType.STRING)
    private MoveResult moveResult;
}
