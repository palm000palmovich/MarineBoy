package com.example.demo.repository;

import com.example.demo.enums.GameStatus;
import com.example.demo.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    List<GameSession> findByStatus(GameStatus status);
}
