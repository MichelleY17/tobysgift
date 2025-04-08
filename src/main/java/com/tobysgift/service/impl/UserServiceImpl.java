package com.tobysgift.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tobysgift.model.Role;
import com.tobysgift.model.User;
import com.tobysgift.repository.UserRepository;
import com.tobysgift.service.UserService;


@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    @Transactional
    public User register(User user) {
        // per verificare se l'email è già in uso
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email già in uso");
        }
        
        // per verificare se l'username è già in uso
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Nome utente già in uso");
        }
        
        //per codificare la password nel database
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // per impostare il   ruolo predefinito USER
        user.addRole(Role.USER);
        
        // èr impostare  la data di creazione
        user.setCreatedAt(LocalDateTime.now());
        
        // per impostare lo stato attivo
        user.setActive(true);
        
        // finalmente per salvare l'utente
        return userRepository.save(user);
    }
    
    // @Override
    // @Transactional
    // public User updateProfile(User user) {
    //     User existingUser = userRepository.findById(user.getId())
    //             .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        
    //     // Aggiorna solo i campi del profilo, non le credenziali
    //     existingUser.setNome(user.getNome());
    //     existingUser.setCognome(user.getCognome());
    //     existingUser.setIndirizzo(user.getIndirizzo());
    //     existingUser.setTelefono(user.getTelefono());
        
    //     return userRepository.save(existingUser);
    // }
    @Override
    @Transactional
    public boolean updateUserProfile(String email, String nome, String cognome, String indirizzo, String telefono) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Aggiorna solo i campi specifici
            user.setNome(nome);
            user.setCognome(cognome);
            user.setIndirizzo(indirizzo);
            user.setTelefono(telefono);
            
            userRepository.save(user);
            return true;
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        
        // per verificare  che la vecchia password sia corretta
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // per codificare e imposta la nuova password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return true;
    }
    
    // @Override
    // @Transactional
    // public boolean changeUsername(String email, String newUsername) {
    //     // per veridficare  se il nuovo username è già in uso
    //     if (userRepository.existsByUsername(newUsername)) {
    //         return false;
    //     }
        
    //     User user = userRepository.findByEmail(email)
    //             .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        
    //     user.setUsername(newUsername);
    //     userRepository.save(user);
        
    //     return true;
    // }
    @Override
    @Transactional
    public boolean changeUsername(String email, String newUsername) {
        // Log per debug
        System.out.println("Service: Cambio username per email " + email + " a " + newUsername);
        
        // Verifica se il nuovo username è già in uso
        if (userRepository.existsByUsername(newUsername)) {
            System.out.println("Service: Username già in uso: " + newUsername);
            return false;
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        
        // Memorizza il vecchio username per log
        String oldUsername = user.getUsername();
        
        user.setUsername(newUsername);
        userRepository.save(user);
        
        System.out.println("Service: Username cambiato da " + oldUsername + " a " + newUsername);
        
        return true;
    }
    
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }
    
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    @Transactional
    public void updateLastLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
}