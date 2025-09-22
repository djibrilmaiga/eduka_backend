package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.DTO.OrganisationConnecterDTO;
import com.groupe2_ionic.eduka.DTO.OrganisationInscrireDTO;
import com.groupe2_ionic.eduka.models.Organisation;
import com.groupe2_ionic.eduka.models.enums.RoleUser;
import com.groupe2_ionic.eduka.repository.OrganisationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class OrganisationService {

    private static final Logger logger = LoggerFactory.getLogger(OrganisationService.class);

    @Autowired
    private OrganisationRepository organisationRepository;

    // Inscription d'une organisation
    public Organisation registerOrganisation(OrganisationInscrireDTO dto) {
        logger.info("DTO reçu pour inscription : {}", dto);

        // Vérifier si l'email existe déjà
        Optional<Organisation> existingOrganisation = organisationRepository.findByEmail(dto.getEmail());
        if (existingOrganisation.isPresent()) {
            logger.warn("Tentative d'inscription avec un email déjà utilisé : {}", dto.getEmail());
            throw new IllegalArgumentException("L'email est déjà utilisé.");
        }

        // Vérifier si le téléphone existe déjà
        Optional<Organisation> existingByTelephone = organisationRepository.findByTelephone(dto.getTelephone());
        if (existingByTelephone.isPresent()) {
            logger.warn("Tentative d'inscription avec un téléphone déjà utilisé : {}", dto.getTelephone());
            throw new IllegalArgumentException("Le numéro de téléphone est déjà utilisé.");
        }

        // Vérifier que le mot de passe n'est pas null ou vide
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            logger.error("Le mot de passe est null ou vide : {}", dto.getPassword());
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        }

        // Créer une nouvelle organisation
        Organisation organisation = new Organisation();
        organisation.setEmail(dto.getEmail());
        organisation.setTelephone(dto.getTelephone());
        organisation.setPassword(dto.getPassword()); // Pas de hachage
        logger.info("Mot de passe défini : {}", dto.getPassword());
        organisation.setNom(dto.getNom());
        organisation.setNomRepresentant(dto.getNomRepresentant());
        organisation.setPrenomRepresentant(dto.getPrenomRepresentant());
        organisation.setFonctionRepresentant(dto.getFonctionRepresentant());
        organisation.setVille(dto.getVille());
        organisation.setPays(dto.getPays());
        organisation.setRole(RoleUser.ROLE_ORGANISATION);
        logger.info("Rôle défini avant sauvegarde : {}", organisation.getRole());
        organisation.setDateInscription(LocalDate.now());
        organisation.setActif(false); // En attente de validation par un admin

        // Sauvegarder dans la base de données
        logger.info("Sauvegarde de l'organisation : {}", organisation);
        Organisation savedOrganisation = organisationRepository.save(organisation);
        logger.info("Organisation sauvegardée : Role = {}", savedOrganisation.getRole());
        return savedOrganisation;
    }

    // Connexion d'une organisation
    public Organisation loginOrganisation(OrganisationConnecterDTO dto) {
        logger.info("Tentative de connexion avec email : {}", dto.getEmail());

        // Rechercher l'organisation par email
        Optional<Organisation> organisationOptional = organisationRepository.findByEmail(dto.getEmail());
        if (organisationOptional.isEmpty()) {
            logger.warn("Email non trouvé : {}", dto.getEmail());
            throw new IllegalArgumentException("Email ou mot de passe incorrect.");
        }

        Organisation organisation = organisationOptional.get();

        // Vérifier le mot de passe (comparaison directe sans hachage)
        if (!dto.getPassword().equals(organisation.getPassword())) {
            logger.warn("Mot de passe incorrect pour email : {}", dto.getEmail());
            throw new IllegalArgumentException("Email ou mot de passe incorrect.");
        }

        // Vérifier si l'organisation est active
        if (!organisation.getActif()) {
            logger.warn("Compte non activé pour email : {}", dto.getEmail());
            throw new IllegalStateException("Votre compte n'est pas encore activé.");
        }

        logger.info("Connexion réussie pour email : {}", dto.getEmail());
        return organisation;
    }
}