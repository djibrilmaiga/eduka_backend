package com.groupe2_ionic.eduka.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration des tâches planifiées
 * Active le support des tâches @Scheduled pour le nettoyage automatique
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Configuration automatique par Spring Boot
    // Les méthodes @Scheduled dans les services seront automatiquement exécutées
}
