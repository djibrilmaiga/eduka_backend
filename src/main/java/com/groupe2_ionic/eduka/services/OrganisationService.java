package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.*;
import com.groupe2_ionic.eduka.models.*;
import com.groupe2_ionic.eduka.models.enums.RoleUser;
import com.groupe2_ionic.eduka.models.enums.StatutTransfert;
import com.groupe2_ionic.eduka.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final EnfantRepository enfantRepository;
    private final BesoinRepository besoinRepository;
    private final DepenseRepository depenseRepository;
    private final RapportRepository rapportRepository;
    private final DocumentRepository documentRepository;
    private final TransfertFondRepository transfertFondRepository;
    private final NotificationService notificationService;

    /**
     * Inscription d'une nouvelle organisation
     */
    public OrganisationResponseDto inscrireOrganisation(OrganisationDto organisationDto) {
        // Vérifier si l'email ou le téléphone existe déjà
        if (organisationRepository.findByEmail(organisationDto.getEmail()).isPresent()) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }
        if (organisationRepository.findByTelephone(organisationDto.getTelephone()).isPresent()) {
            throw new RuntimeException("Un compte avec ce téléphone existe déjà");
        }

        Organisation organisation = new Organisation();
        organisation.setNom(organisationDto.getNom());
        organisation.setEmail(organisationDto.getEmail());
        organisation.setTelephone(organisationDto.getTelephone());
        organisation.setPassword(organisationDto.getPassword()); // À encoder avec Spring Security
        organisation.setNomRepresentant(organisationDto.getNomRepresentant());
        organisation.setPrenomRepresentant(organisationDto.getPrenomRepresentant());
        organisation.setFonctionRepresentant(organisationDto.getFonctionRepresentant());
        organisation.setVille(organisationDto.getVille());
        organisation.setPays(organisationDto.getPays());
        organisation.setRole(RoleUser.ROLE_ORGANISATION);
        organisation.setDateInscription(LocalDate.now());
        organisation.setActif(false); // En attente de validation par admin

        Organisation savedOrganisation = organisationRepository.save(organisation);

        // Notifier les admins de la nouvelle demande
        notificationService.notifierNouvelleDemandeOrganisation(savedOrganisation);

        return mapToResponseDto(savedOrganisation);
    }

    /**
     * Enregistrer un nouvel enfant
     */
    public EnfantResponseDto enregistrerEnfant(int organisationId, EnfantDto enfantDto) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        if (!organisation.getActif()) {
            throw new RuntimeException("Organisation non validée");
        }

        Enfant enfant = new Enfant();
        enfant.setNom(enfantDto.getNom());
        enfant.setPrenom(enfantDto.getPrenom());
        enfant.setGenre(enfantDto.getGenre());
        enfant.setDateNaissance(enfantDto.getDateNaissance());
        enfant.setNiveauScolaire(enfantDto.getNiveauScolaire());
        enfant.setHistoire(enfantDto.getHistoire());
        enfant.setPhotoProfil(enfantDto.getPhotoProfil());
        enfant.setStatutParrainage(false);
        enfant.setSolde(BigDecimal.ZERO);
        enfant.setConsentementPedagogique(enfantDto.getConsentementPedagogique());
        enfant.setOrganisation(organisation);

        Enfant savedEnfant = enfantRepository.save(enfant);

        // Notifier les parrains potentiels
        notificationService.notifierNouvelEnfantDisponible(savedEnfant);

        return mapEnfantToResponseDto(savedEnfant);
    }

    /**
     * Obtenir la liste des enfants d'une organisation
     */
    @Transactional(readOnly = true)
    public Page<EnfantResponseDto> obtenirEnfantsOrganisation(int organisationId, Pageable pageable) {
        Page<Enfant> enfants = enfantRepository.findByOrganisationId(organisationId, pageable);
        return enfants.map(this::mapEnfantToResponseDto);
    }

    /**
     * Créer un besoin pour un enfant
     */
    public BesoinResponseDto creerBesoin(int organisationId, BesoinDto besoinDto) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        Enfant enfant = enfantRepository.findById(besoinDto.getEnfantId())
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));

        if (!(enfant.getOrganisation().getId() == (organisationId))) {
            throw new RuntimeException("Cet enfant n'appartient pas à votre organisation");
        }

        Besoin besoin = new Besoin();
        besoin.setType(besoinDto.getType());
        besoin.setMontant(besoinDto.getMontant());
        besoin.setEnfant(enfant);

        Besoin savedBesoin = besoinRepository.save(besoin);

        // Notifier le parrain si l'enfant est parrainé
        if (enfant.getStatutParrainage()) {
            notificationService.notifierNouveauBesoin(savedBesoin);
        }

        return mapBesoinToResponseDto(savedBesoin);
    }

    /**
     * Enregistrer une dépense
     */
    public DepenseResponseDto enregistrerDepense(int organisationId, DepenseDto depenseDto) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        Enfant enfant = enfantRepository.findById(depenseDto.getEnfantId())
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));

        if (!(enfant.getOrganisation().getId() == (organisationId))) {
            throw new RuntimeException("Cet enfant n'appartient pas à votre organisation");
        }

        Depense depense = new Depense();
        depense.setTypeDepense(depenseDto.getTypeDepense());
        depense.setJustificatif(depenseDto.getJustificatif());
        depense.setMontant(depenseDto.getMontant());
        depense.setDateEnregistrement(LocalDate.now());
        depense.setOrganisation(organisation);
        depense.setEnfant(enfant);

        Depense savedDepense = depenseRepository.save(depense);

        // Déduire du solde de l'enfant si applicable
        if (enfant.getSolde() != null && enfant.getSolde().compareTo(depenseDto.getMontant()) >= 0) {
            enfant.setSolde(enfant.getSolde().subtract(depenseDto.getMontant()));
            enfantRepository.save(enfant);
        }

        // Notifier le parrain
        if (enfant.getStatutParrainage()) {
            notificationService.notifierNouvelleDepense(savedDepense);
        }

        return mapDepenseToResponseDto(savedDepense);
    }

    /**
     * Créer un rapport pour un enfant
     */
    public RapportResponseDto creerRapport(int organisationId, RapportDto rapportDto) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        Enfant enfant = enfantRepository.findById(rapportDto.getEnfantId())
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));

        if (!(enfant.getOrganisation().getId() == (organisationId))) {
            throw new RuntimeException("Cet enfant n'appartient pas à votre organisation");
        }

        Rapport rapport = new Rapport();
        rapport.setTitre(rapportDto.getTitre());
        rapport.setTypeRapport(rapportDto.getTypeRapport());
        rapport.setPeriode(rapportDto.getPeriode());
        rapport.setContenu(rapportDto.getContenu());
        rapport.setDate(LocalDate.now());
        rapport.setEnfant(enfant);
        rapport.setOrganisation(organisation);

        Rapport savedRapport = rapportRepository.save(rapport);

        // Notifier le parrain
        if (enfant.getStatutParrainage()) {
            notificationService.notifierNouveauRapport(savedRapport);
        }

        return mapRapportToResponseDto(savedRapport);
    }

    /**
     * Obtenir les rapports d'une organisation
     */
    @Transactional(readOnly = true)
    public List<RapportResponseDto> obtenirRapportsOrganisation(int organisationId) {
        List<Rapport> rapports = rapportRepository.findByOrganisationIdOrderByDateDesc(organisationId);
        return rapports.stream()
                .map(this::mapRapportToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Créer une demande de transfert résiduel
     */
    public TransfertFondResponseDto creerDemandeTransfertResiduel(int organisationId, TransfertFondDto transfertDto) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        Enfant enfantSource = enfantRepository.findById(transfertDto.getEnfantSourceId())
                .orElseThrow(() -> new RuntimeException("Enfant source non trouvé"));

        if (!(enfantSource.getOrganisation().getId() == (organisationId))) {
            throw new RuntimeException("Cet enfant n'appartient pas à votre organisation");
        }

        if (enfantSource.getSolde() == null || enfantSource.getSolde().compareTo(transfertDto.getMontant()) < 0) {
            throw new RuntimeException("Solde insuffisant pour le transfert");
        }

        TransfertFond transfert = new TransfertFond();
        transfert.setMotif(transfertDto.getMotif());
        transfert.setDescription(transfertDto.getDescription());
        transfert.setMontant(transfertDto.getMontant());
        transfert.setStatut(StatutTransfert.EN_ATTENTE);
        transfert.setDateDemande(LocalDate.now());
        transfert.setEnfantSource(enfantSource);
        transfert.setOrganisation(organisation);

        TransfertFond savedTransfert = transfertFondRepository.save(transfert);

        // Notifier le parent/tuteur pour validation
        notificationService.notifierDemandeTransfertParent(savedTransfert);

        return mapTransfertToResponseDto(savedTransfert);
    }

    /**
     * Obtenir le tableau de bord de l'organisation
     */
    @Transactional(readOnly = true)
    public OrganisationDashboardDto obtenirTableauDeBord(int organisationId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        OrganisationDashboardDto dashboard = new OrganisationDashboardDto();
        dashboard.setNombreEnfants(enfantRepository.countByOrganisationId(organisationId));
        dashboard.setNombreEnfantsParraines(enfantRepository.countByOrganisationIdAndStatutParrainageTrue(organisationId));
        dashboard.setNombreRapports(rapportRepository.countByOrganisationId(organisationId));
        dashboard.setNombreDepenses(depenseRepository.countByOrganisationId(organisationId));
        dashboard.setMontantTotalDepenses(depenseRepository.sumMontantByOrganisationId(organisationId));

        return dashboard;
    }

    // Méthodes utilitaires de mapping
    private OrganisationResponseDto mapToResponseDto(Organisation organisation) {
        OrganisationResponseDto dto = new OrganisationResponseDto();
        dto.setId(organisation.getId());
        dto.setNom(organisation.getNom());
        dto.setEmail(organisation.getEmail());
        dto.setTelephone(organisation.getTelephone());
        dto.setNomRepresentant(organisation.getNomRepresentant());
        dto.setPrenomRepresentant(organisation.getPrenomRepresentant());
        dto.setFonctionRepresentant(organisation.getFonctionRepresentant());
        dto.setVille(organisation.getVille());
        dto.setPays(organisation.getPays());
        dto.setActif(organisation.getActif());
        dto.setDateInscription(organisation.getDateInscription());
        dto.setValidateurNom(organisation.getValidateur() != null ?
                organisation.getValidateur().getNom() + " " + organisation.getValidateur().getPrenom() : null);
        dto.setNombreEnfants(organisation.getEnfants().size());
        dto.setNombrePaiements(organisation.getPaiements().size());
        return dto;
    }

    private EnfantResponseDto mapEnfantToResponseDto(Enfant enfant) {
        EnfantResponseDto dto = new EnfantResponseDto();
        dto.setId(enfant.getId());
        dto.setNom(enfant.getNom());
        dto.setPrenom(enfant.getPrenom());
        dto.setGenre(enfant.getGenre());
        dto.setDateNaissance(enfant.getDateNaissance());
        dto.setAge(Period.between(enfant.getDateNaissance(), LocalDate.now()).getYears());
        dto.setNiveauScolaire(enfant.getNiveauScolaire());
        dto.setHistoire(enfant.getHistoire());
        dto.setPhotoProfil(enfant.getPhotoProfil());
        dto.setStatutParrainage(enfant.getStatutParrainage());
        dto.setSolde(enfant.getSolde());
        dto.setConsentementPedagogique(enfant.getConsentementPedagogique());
        dto.setOrganisationNom(enfant.getOrganisation().getNom());
        dto.setTuteurNom(enfant.getTuteur() != null ? enfant.getTuteur().getNom() + " " + enfant.getTuteur().getPrenom() : null);
        dto.setEcoleNom(enfant.getEcole() != null ? enfant.getEcole().getNom() : null);
        dto.setNombreParrainages(enfant.getParrainages() != null ? enfant.getParrainages().size() : 0);
        dto.setNombreRapports(enfant.getRapports() != null ? enfant.getRapports().size() : 0);
        return dto;
    }

    private BesoinResponseDto mapBesoinToResponseDto(Besoin besoin) {
        BesoinResponseDto dto = new BesoinResponseDto();
        dto.setId(besoin.getId());
        dto.setType(besoin.getType());
        dto.setMontant(besoin.getMontant());
        dto.setEnfantNom(besoin.getEnfant().getNom());
        dto.setEnfantPrenom(besoin.getEnfant().getPrenom());
        return dto;
    }

    private DepenseResponseDto mapDepenseToResponseDto(Depense depense) {
        DepenseResponseDto dto = new DepenseResponseDto();
        dto.setId(depense.getId());
        dto.setTypeDepense(depense.getTypeDepense());
        dto.setJustificatif(depense.getJustificatif());
        dto.setMontant(depense.getMontant());
        dto.setDateEnregistrement(depense.getDateEnregistrement());
        dto.setOrganisationNom(depense.getOrganisation().getNom());
        dto.setEnfantNom(depense.getEnfant().getNom());
        dto.setEnfantPrenom(depense.getEnfant().getPrenom());
        return dto;
    }

    private RapportResponseDto mapRapportToResponseDto(Rapport rapport) {
        RapportResponseDto dto = new RapportResponseDto();
        dto.setId(rapport.getId());
        dto.setTitre(rapport.getTitre());
        dto.setTypeRapport(rapport.getTypeRapport());
        dto.setPeriode(rapport.getPeriode());
        dto.setContenu(rapport.getContenu());
        dto.setDate(rapport.getDate());
        dto.setEnfantNom(rapport.getEnfant().getNom());
        dto.setEnfantPrenom(rapport.getEnfant().getPrenom());
        dto.setOrganisationNom(rapport.getOrganisation().getNom());
        dto.setNombreDocuments(rapport.getDocuments().size());
        return dto;
    }

    private TransfertFondResponseDto mapTransfertToResponseDto(TransfertFond transfert) {
        TransfertFondResponseDto dto = new TransfertFondResponseDto();
        dto.setId(transfert.getId());
        dto.setMotif(transfert.getMotif());
        dto.setDescription(transfert.getDescription());
        dto.setMontant(transfert.getMontant());
        dto.setStatut(transfert.getStatut());
        dto.setDateDemande(transfert.getDateDemande());
        dto.setDateTraitement(transfert.getDateTraitement());
        dto.setCommentaireValidation(transfert.getCommentaireValidation());
        dto.setEnfantSourceNom(transfert.getEnfantSource().getNom() + " " + transfert.getEnfantSource().getPrenom());
        dto.setEnfantCibleNom(transfert.getEnfantCible() != null ?
                transfert.getEnfantCible().getNom() + " " + transfert.getEnfantCible().getPrenom() : null);
        dto.setOrganisationNom(transfert.getOrganisation().getNom());
        dto.setParrainNom(transfert.getParrain() != null ?
                transfert.getParrain().getNom() + " " + transfert.getParrain().getPrenom() : null);
        return dto;
    }
}
