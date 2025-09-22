package com.groupe2_ionic.eduka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classe de configuration Spring Boot pour gérer l'accès aux fichiers uploadés.
 * Elle permet de mapper un dossier local du serveur vers une URL publique,
 * afin que les fichiers (images, PDF, etc.) soient accessibles via HTTP.
 */
@Configuration // Indique à Spring que cette classe contient des configurations
public class FileStorageConfig implements WebMvcConfigurer {

    /**
     * Récupère le chemin du dossier d'upload depuis application.properties (file.upload-dir).
     * Si la propriété n'est pas définie, utilise par défaut "uploads".
     */
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Méthode de configuration qui indique à Spring où chercher les ressources statiques.
     * On redirige toutes les requêtes vers /uploads/** vers le dossier local correspondant.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convertit le chemin relatif en chemin absolu normalisé
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        // Préfixe "file:" pour indiquer à Spring qu'il s'agit d'un chemin système (et non d'une ressource du classpath)
        String location = "file:" + uploadPath.toString() + "/";

        // Associe l'URL /uploads/** aux fichiers du dossier local
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
