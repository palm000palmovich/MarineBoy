package com.example.demo.service;

import com.example.demo.model.Gamer;
import com.example.demo.repository.GamerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GamerService implements UserDetailsService {
    private final GamerRepository gamerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Gamer gamer = gamerRepository.findByNickname(username)
                .orElseThrow(() -> new UsernameNotFoundException("Gamer not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(gamer.getNickname())
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .password("")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
