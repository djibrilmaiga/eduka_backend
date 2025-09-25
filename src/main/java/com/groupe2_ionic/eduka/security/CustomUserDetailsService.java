package com.groupe2_ionic.eduka.security;

import com.groupe2_ionic.eduka.models.Utilisateur;
import com.groupe2_ionic.eduka.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service personnalisé pour charger les détails des utilisateurs
 * Implémente UserDetailsService pour l'intégration avec Spring Security
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Tentative de chargement de l'utilisateur: {}", username);

        // Rechercher l'utilisateur par email ou téléphone
        Utilisateur utilisateur = utilisateurRepository.findByEmailOrTelephone(username, username)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé: {}", username);
                    return new UsernameNotFoundException("Utilisateur non trouvé: " + username);
                });

        // Vérifier si l'utilisateur est actif
        if (!utilisateur.getActif()) {
            log.error("Utilisateur inactif: {}", username);
            throw new UsernameNotFoundException("Compte utilisateur désactivé: " + username);
        }

        log.debug("Utilisateur chargé avec succès: {} (Role: {})", username, utilisateur.getRole());

        // Créer et retourner UserDetails
        return User.builder()
                .username(utilisateur.getEmail()) // Utiliser l'email comme username principal
                .password(utilisateur.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority(utilisateur.getRole().name())
                ))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!utilisateur.getActif())
                .build();
    }

    /**
     * Charge un utilisateur par son ID
     */
    public UserDetails loadUserById(Integer userId) throws UsernameNotFoundException {
        log.debug("Tentative de chargement de l'utilisateur par ID: {}", userId);

        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé avec l'ID: {}", userId);
                    return new UsernameNotFoundException("Utilisateur non trouvé avec l'ID: " + userId);
                });

        return loadUserByUsername(utilisateur.getEmail());
    }
}
