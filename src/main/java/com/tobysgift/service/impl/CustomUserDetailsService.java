package com.tobysgift.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tobysgift.model.Role;
import com.tobysgift.model.User;
import com.tobysgift.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // per cercare l'utente per email invece che per username
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con email: " + email));

        // per verificare che l'utente sia attivo
        if (!user.isActive()) {
            throw new UsernameNotFoundException("Utente disabilitato: " + email);
        }

        // per aggiornare  l'ultimo accesso
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // per construire le autorizzazioni dell'utente
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.name()));
        }

        // per restituire l'oggetto UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),    // email come username per Spring Security
                user.getPassword(),
                user.isActive(),
                true,
                true,
                true,
                authorities
        );
    }
}
