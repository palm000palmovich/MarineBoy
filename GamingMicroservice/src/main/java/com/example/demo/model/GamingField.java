package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gaming_fields")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GamingField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "gamer_id", nullable = false)
    private Gamer gamer;
    @Column(name = "field_data", columnDefinition = "TEXT")
    private String fieldData;
    @ManyToOne
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;
}
