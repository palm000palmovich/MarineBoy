package com.example.demo.repository;

import com.example.demo.model.GameSession;
import com.example.demo.model.Gamer;
import com.example.demo.model.GamingField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GamingFieldRepository extends JpaRepository<GamingField, Long>  {
    Optional<GamingField> findByGamerAndGameSession(Gamer gamer, GameSession gameSession);
}
