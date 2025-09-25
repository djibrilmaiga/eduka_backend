package com.groupe2_ionic.eduka.security;

import com.groupe2_ionic.eduka.models.Tuteur;
import com.groupe2_ionic.eduka.repository.TuteurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service UserDetails pour les tuteurs
 * Gère l'authentification des tuteurs via OTP
 */
@Slf4j
@Service("tuteurUserDetailsService")
@RequiredArgsConstructor
public class TuteurUserDetailsService implements UserDetailsService {

    private final TuteurRepository tuteurRepository;

    @Override
    public UserDetails loadUserByUsername(String telephone) throws UsernameNotFoundException {
        log.debug("Tentative de chargement du tuteur: {}", telephone);

        Tuteur tuteur = tuteurRepository.findByTelephone(telephone)
                .orElseThrow(() -> {
                    log.error("Tuteur non trouvé: {}", telephone);
                    return new UsernameNotFoundException("Tuteur non trouvé: " + telephone);
                });

        log.debug("Tuteur chargé avec succès: {} {} ({})",
                tuteur.getPrenom(), tuteur.getNom(), telephone);

        // Créer et retourner UserDetails pour tuteur
        return User.builder()
                .username(tuteur.getTelephone())
                .password("") // Pas de mot de passe pour les tuteurs (authentification OTP)
                .authorities(Collections.singletonList(() -> "ROLE_TUTEUR"))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * Charge un tuteur par son ID
     */
    public UserDetails loadUserById(Integer tuteurId) throws UsernameNotFoundException {
        log.debug("Tentative de chargement du tuteur par ID: {}", tuteurId);

        Tuteur tuteur = tuteurRepository.findById(tuteurId)
                .orElseThrow(() -> {
                    log.error("Tuteur non trouvé avec l'ID: {}", tuteurId);
                    return new UsernameNotFoundException("Tuteur non trouvé avec l'ID: " + tuteurId);
                });

        return loadUserByUsername(tuteur.getTelephone());
    }
}
