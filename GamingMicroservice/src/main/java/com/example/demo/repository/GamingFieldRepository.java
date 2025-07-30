package com.example.demo.repository;

import com.example.demo.model.GameSession;
import com.example.demo.model.Gamer;
import com.example.demo.model.GamingField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GamingFieldRepository extends JpaRepository<GamingField, Long>  {
    Optional<GamingField> findByGamerAndGameSession(Gamer gamer, GameSession gameSession);

    @Query(value = "select * from gaming_fields gf where gf.gamer_id = :gamerId and gf.game_session_id = :sessionId",
            nativeQuery = true)
    Optional<GamingField> findFieldByGamerIdAndSessionId(@Param("gamerId") Long gamerId,
                                                         @Param("sessionId") Long sessionId);
}
