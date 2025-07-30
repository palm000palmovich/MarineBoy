package com.example.demo.repository;

import com.example.demo.model.Gamer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GamerRepository extends JpaRepository<Gamer, Long> {
    Optional<Gamer> findByNickname(String nickname);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM gamers g WHERE g.nickname = :nickname)", nativeQuery = true)
    boolean checkAccountsAvailability(@Param("nickname") String nickName);
}
