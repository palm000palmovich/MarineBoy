package com.example.demo.dto;

import com.example.demo.enums.GameStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionCacheDto implements Serializable {
    private Long sessionId;
    private String playerOneNickname;
    private String playerTwoNickname;
    @Enumerated(EnumType.STRING)
    private GameStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private String winnerNickname;
    private Map<String, FieldCacheDto> playerFields = new HashMap<>();

    @Override
    public String toString() {
        return "GameSessionCacheDto{" +
                "sessionId=" + sessionId +
                ", playerOneNickname='" + playerOneNickname + '\'' +
                ", playerTwoNickname='" + playerTwoNickname + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", finishedAt=" + finishedAt +
                ", winnerNickname='" + winnerNickname + '\'' +
                '}';
    }
}
