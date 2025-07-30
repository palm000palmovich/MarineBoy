package com.example.demo.model;

import com.example.demo.enums.GameStatus;
import com.example.demo.enums.GameType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "player_one_id", nullable = false)
    @JsonBackReference
    private Gamer playerOne;
    @ManyToOne
    @JoinColumn(name = "player_two_id")
    @JsonBackReference
    private Gamer playerTwo;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GameStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private GameType type;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    @ManyToOne
    @JoinColumn(name = "winner_id")
    @JsonBackReference
    private Gamer winner;

    public void startGame() {
        this.status = GameStatus.IN_PROGRESS;
    }

    public void finishGame(Gamer winner) {
        this.status = GameStatus.FINISHED;
        this.winner = winner;
        this.finishedAt = LocalDateTime.now();
    }
}
