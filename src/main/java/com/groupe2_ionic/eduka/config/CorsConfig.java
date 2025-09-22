package com.groupe2_ionic.eduka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Classe de configuration pour gérer les règles CORS (Cross-Origin Resource Sharing).
 * CORS est nécessaire pour permettre à un client (par ex. Angular sur un autre port)
 * d'accéder aux ressources de l'API Spring Boot.
 */
@Configuration // Indique à Spring que cette classe contient des configurations
public class CorsConfig {

    /**
     * Déclare un bean WebMvcConfigurer qui configure les règles CORS globales pour l'application.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        // On retourne une implémentation anonyme de WebMvcConfigurer
        return new WebMvcConfigurer() {

            /**
             * Configure-les mappings CORS pour l'API.
             * @param registry objet qui gère les règles CORS
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Autorise toutes les routes de l'API
                        .allowedOrigins("http://localhost:4200") // Autorise uniquement le frontend Angular local
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Méthodes autorisées
                        .allowedHeaders("*"); // Tous les headers autorisés
            }
        };
    }
}
