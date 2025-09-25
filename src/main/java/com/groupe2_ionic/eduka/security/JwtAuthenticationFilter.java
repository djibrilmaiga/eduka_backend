package com.groupe2_ionic.eduka.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre d'authentification JWT
 * Intercepte les requêtes pour valider les tokens JWT et configurer le contexte de sécurité
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Vérifier la présence du header Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraire le token
            jwt = jwtUtil.extractTokenFromHeader(authHeader);
            username = jwtUtil.extractUsername(jwt);

            // Vérifier si l'utilisateur n'est pas déjà authentifié
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Charger les détails de l'utilisateur
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Valider le token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // Créer l'objet d'authentification
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // Ajouter les détails de la requête
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Configurer le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Utilisateur authentifié: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
