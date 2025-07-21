package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMoveDto {
    private Long sessionId;
    private String nickName;
    private int x;
    private int y;
}
