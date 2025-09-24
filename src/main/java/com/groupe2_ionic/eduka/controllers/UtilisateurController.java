package com.groupe2_ionic.eduka.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Utilisateurs", description = "API de gestion des utilisateurs (à implémenter)")
public class UtilisateurController {

    // TODO: Implémenter les endpoints de gestion des utilisateurs
    // - POST /api/v1/users/register (inscription)
    // - POST /api/v1/users/login (connexion)
    // - GET /api/v1/users/profile (profil utilisateur)
    // - PUT /api/v1/users/profile (mise à jour profil)
    // - POST /api/v1/users/change-password (changement mot de passe)
    // - POST /api/v1/users/forgot-password (mot de passe oublié)
    // - GET /api/v1/users/roles (gestion des rôles)
}
