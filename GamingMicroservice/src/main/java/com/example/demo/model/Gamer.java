package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gamers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Gamer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nickname")
    private String nickname;
    @OneToMany(mappedBy = "playerOne", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameSession> hostedGames = new ArrayList<>();
    @OneToMany(mappedBy = "playerTwo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameSession> joinedGames = new ArrayList<>();
}
