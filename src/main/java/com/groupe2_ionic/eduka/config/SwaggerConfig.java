package com.groupe2_ionic.eduka.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Classe de configuration pour Swagger / OpenAPI.
 * Permet de personnaliser la documentation générée automatiquement
 * pour l'API Eduka - Plateforme de Parrainage Scolaire.
 */
@Configuration // Indique que cette classe contient des beans de configuration Spring
public class SwaggerConfig {

    /**
     * Déclare un bean OpenAPI personnalisé.
     * Ce bean configure le titre, la version, la description et les informations
     * de contact qui apparaîtront dans la documentation Swagger UI.
     *
     * @return un objet OpenAPI avec les métadonnées de l'API
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        // Titre de la documentation
                        .title("Eduka - Plateforme de Parrainage Scolaire")

                        // Version actuelle de l'API
                        .version("1.0.0")

                        // Description détaillée de l'API avec Markdown autorisé
                        .description("""
                                API REST complète pour la plateforme de parrainage scolaire Eduka.
                                
                                Cette API permet de gérer :
                                - **Gestion des utilisateurs** (Administrateurs, Organisations, Parrains, Parents)
                                - **Gestion des enfants** et de leurs profils scolaires
                                - **Système de parrainage** (création, suivi, statuts)
                                - **Paiements en ligne** (Stripe, Mobile Money, espèces)
                                - **Transferts de fonds résiduels** avec workflow de validation
                                - **Rapports pédagogiques et financiers**
                                - **Notifications** (email, SMS)
                                - **Consentements parentaux**
                                
                                ## Workflow de Transfert Résiduel
                                1. **Organisation** : Demande de transfert → Statut: EN_ATTENTE_PARENT
                                2. **Parent** : Validation/rejet → Si validé: EN_ATTENTE_PARRAIN
                                3. **Parrain** : Choix nouvel enfant → Si validé: VALIDE
                                
                                ## Méthodes de Paiement Supportées
                                - **Carte bancaire** (via Stripe)
                                - **Mobile Money** (Orange Money, Moov Money, Wave)
                                - **Paiements en espèces** (enregistrés par les organisations)
                                - **PayPal** (via Stripe)
                                
                                ## Codes de statut HTTP
                                - 200: Succès
                                - 201: Ressource créée
                                - 204: Succès sans contenu
                                - 400: Erreur de validation ou requête incorrecte
                                - 404: Ressource non trouvée
                                - 409: Conflit (parrainage déjà actif, solde insuffisant)
                                - 500: Erreur serveur interne
                                
                                ## Sécurité
                                ⚠**Note** : La sécurité (Spring Security/OAuth) n'est pas encore implémentée dans cette version.
                                L'authentification sera ajoutée dans une prochaine itération.
                                """)

                        // Informations de contact de l'équipe
                        .contact(new Contact()
                                .name("Équipe Eduka - Groupe 2 IONIC")
                                .email("support@eduka.org")
                                .url("https://github.com/djibrilmaiga/eduka_backend"))
                );
    }
}