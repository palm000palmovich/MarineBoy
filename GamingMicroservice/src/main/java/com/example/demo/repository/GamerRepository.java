package com.example.demo.repository;

import com.example.demo.model.Gamer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GamerRepository extends JpaRepository<Gamer, Long> {
    Optional<Gamer> findByNickname(String nickname);
}
