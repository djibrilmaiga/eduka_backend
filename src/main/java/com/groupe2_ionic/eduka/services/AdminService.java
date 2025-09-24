package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.RapportGlobalDto;
import com.groupe2_ionic.eduka.dto.ValidationOrganisationDto;
import com.groupe2_ionic.eduka.dto.ValidationResponseDto;
import com.groupe2_ionic.eduka.models.Admin;
import com.groupe2_ionic.eduka.models.Organisation;
import com.groupe2_ionic.eduka.models.enums.StatutPaiement;
import com.groupe2_ionic.eduka.models.enums.StatutValidation;
import com.groupe2_ionic.eduka.repository.AdminRepository;
import com.groupe2_ionic.eduka.repository.EnfantRepository;
import com.groupe2_ionic.eduka.repository.OrganisationRepository;
import com.groupe2_ionic.eduka.repository.PaiementReposiroty;
import com.groupe2_ionic.eduka.repository.ParrainRepository;
import com.groupe2_ionic.eduka.repository.TransfertFondRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final OrganisationRepository organisationRepository;
    private final NotificationService notificationService;
    private final EnfantRepository enfantRepository;
    private final ParrainRepository parrainRepository;
    private final PaiementReposiroty paiementRepository;
    private final TransfertFondRepository transfertFondRepository;

    /**
     * Valider ou rejeter l'inscription d'une organisation
     */
    @Transactional
    public ValidationResponseDto validerOrganisation(Integer organisationId, Integer adminId, ValidationOrganisationDto validationDto) {
        // Vérifier que l'admin existe
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Administrateur non trouvé"));

        // Vérifier que l'organisation existe
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        // Vérifier que l'organisation est en attente de validation
        if (organisation.getStatutValidation() != StatutValidation.EN_ATTENTE) {
            throw new RuntimeException("Cette organisation a déjà été traitée");
        }

        // Mettre à jour le statut de validation
        organisation.setStatutValidation(validationDto.getStatut());
        organisation.setCommentaireValidation(validationDto.getCommentaire());
        organisation.setValidateur(admin);
        organisation.setDateValidation(LocalDateTime.now());

        // Si validée, activer l'organisation
        if (validationDto.getStatut() == StatutValidation.VALIDEE) {
            organisation.setActif(true);
        } else {
            organisation.setActif(false);
        }

        Organisation organisationSauvegardee = organisationRepository.save(organisation);

        // Envoyer une notification à l'organisation
        String message = validationDto.getStatut() == StatutValidation.VALIDEE
                ? "Félicitations ! Votre inscription a été validée. Vous pouvez maintenant accéder à toutes les fonctionnalités."
                : "Votre inscription a été rejetée. Raison: " + validationDto.getCommentaire();

        notificationService.envoyerNotification(organisation, "Statut de votre inscription", message);

        // Retourner la réponse
        return new ValidationResponseDto(
                organisationSauvegardee.getId(),
                organisationSauvegardee.getNom(),
                organisationSauvegardee.getStatutValidation(),
                organisationSauvegardee.getCommentaireValidation(),
                admin.getEmail(), // Utiliser l'email comme nom pour l'instant
                organisationSauvegardee.getDateValidation()
        );
    }

    /**
     * Récupérer toutes les organisations en attente de validation
     */
    public Page<ValidationResponseDto> getOrganisationsEnAttente(Pageable pageable) {
        Page<Organisation> organisations = organisationRepository.findByStatutValidation(StatutValidation.EN_ATTENTE, pageable);

        return organisations.map(org -> new ValidationResponseDto(
                org.getId(),
                org.getNom(),
                org.getStatutValidation(),
                org.getCommentaireValidation(),
                org.getValidateur() != null ? org.getValidateur().getEmail() : null,
                org.getDateValidation()
        ));
    }

    /**
     * Récupérer l'historique des validations d'un admin
     */
    public List<ValidationResponseDto> getHistoriqueValidations(Integer adminId) {
        List<Organisation> organisations = organisationRepository.findByValidateurIdOrderByDateValidationDesc(adminId);

        return organisations.stream()
                .map(org -> new ValidationResponseDto(
                        org.getId(),
                        org.getNom(),
                        org.getStatutValidation(),
                        org.getCommentaireValidation(),
                        org.getValidateur().getEmail(),
                        org.getDateValidation()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les statistiques de validation
     */
    public ValidationStatsDto getStatistiquesValidation() {
        long enAttente = organisationRepository.countByStatutValidation(StatutValidation.EN_ATTENTE);
        long validees = organisationRepository.countByStatutValidation(StatutValidation.VALIDEE);
        long rejetees = organisationRepository.countByStatutValidation(StatutValidation.REJETEE);

        return new ValidationStatsDto(enAttente, validees, rejetees);
    }

    /**
     * Récupérer le tableau de bord administrateur
     */
   /* public AdminDashboardDto getDashboard() {
        long totalOrganisations = organisationRepository.count();
        long organisationsEnAttente = organisationRepository.countByStatutValidation(StatutValidation.EN_ATTENTE);
        long organisationsValidees = organisationRepository.countByStatutValidation(StatutValidation.VALIDEE);
        long organisationsRejetees = organisationRepository.countByStatutValidation(StatutValidation.REJETEE);

        long totalEnfants = enfantRepository.count();
        long totalParrains = parrainRepository.count();
        long totalPaiements = paiementRepository.count();

        BigDecimal montantTotalPaiements = paiementRepository.sumMontantByStatut(StatutPaiement.REUSSI);
        if (montantTotalPaiements == null) montantTotalPaiements = BigDecimal.ZERO;

        long transfertsEnAttente = transfertFondRepository.countByStatut(StatutTransfert.EN_ATTENTE);
        long litiges = 0; // À implémenter selon les besoins

        return new AdminDashboardDto(
                totalOrganisations, organisationsEnAttente, organisationsValidees, organisationsRejetees,
                totalEnfants, totalParrains, totalPaiements, montantTotalPaiements,
                transfertsEnAttente, litiges
        );
    }*/

    /**
     * Générer un rapport global pour une période donnée
     */
    public RapportGlobalDto genererRapportGlobal(LocalDate dateDebut, LocalDate dateFin) {
        // Statistiques générales
        long nombreOrganisations = organisationRepository.countByStatutValidation(StatutValidation.VALIDEE);
        long nombreEnfants = enfantRepository.countByDateInscriptionBetween(dateDebut, dateFin);
        long nombreParrains = parrainRepository.countByDateInscriptionBetween(dateDebut, dateFin);
        long nombrePaiements = paiementRepository.countByDatePaiementBetween(dateDebut, dateFin);

        BigDecimal montantTotalPaiements = paiementRepository.sumMontantByDatePaiementBetweenAndStatut(
                dateDebut, dateFin, StatutPaiement.REUSSI);
        if (montantTotalPaiements == null) montantTotalPaiements = BigDecimal.ZERO;

        // Statistiques par pays (à implémenter selon les besoins)
        List<RapportGlobalDto.StatistiqueParPaysDto> statistiquesParPays = new ArrayList<>();

        // Top organisations (à implémenter selon les besoins)
        List<RapportGlobalDto.StatistiqueParOrganisationDto> topOrganisations = new ArrayList<>();

        return new RapportGlobalDto(
                dateDebut, dateFin, nombreOrganisations, nombreEnfants, nombreParrains,
                nombrePaiements, montantTotalPaiements, statistiquesParPays, topOrganisations
        );
    }

    /**
     * Superviser les paiements suspects ou en échec
     */
    /*public List<PaiementResponseDto> superviserPaiements() {
        List<Paiement> paiementsEchec = paiementRepository.findByStatutOrderByDatePaiementDesc(StatutPaiement.ECHEC);

        return paiementsEchec.stream()
                .map(this::convertirEnPaiementResponseDto)
                .collect(Collectors.toList());
    }*/

    /**
     * Superviser les transferts en attente
     */
    /*public List<TransfertFondResponseDto> superviserTransferts() {
        List<TransfertFond> transfertsEnAttente = transfertFondRepository.findByStatutOrderByDateCreationDesc(StatutTransfert.EN_ATTENTE);

        return transfertsEnAttente.stream()
                .map(this::convertirEnTransfertResponseDto)
                .collect(Collectors.toList());
    }*/

    /**
     * Exporter les données comptables
     */
    public byte[] exporterDonneesComptables(LocalDate dateDebut, LocalDate dateFin, String format) {
        // Cette méthode sera implémentée avec les services PDF/CSV
        // Pour l'instant, retourner un placeholder
        return new byte[0];
    }

    // Méthodes utilitaires privées
    /*private PaiementResponseDto convertirEnPaiementResponseDto(Paiement paiement) {
        // Conversion Paiement -> PaiementResponseDto
        return new PaiementResponseDto(
                paiement.getId(),
                paiement.getMontant(),
                paiement.getMethode(),
                paiement.getStatut(),
                paiement.getDatePaiement(),
                paiement.getParrain() != null ? paiement.getParrain().getId() : null,
                paiement.getEnfant() != null ? paiement.getEnfant().getId() : null,
                paiement.getOrganisation() != null ? paiement.getOrganisation().getId() : null
        );
    }*/

    /*private TransfertFondResponseDto convertirEnTransfertResponseDto(TransfertFond transfert) {
        // Conversion TransfertFond -> TransfertFondResponseDto
        return new TransfertFondResponseDto(
                transfert.getId(),
                transfert.getMontant(),
                transfert.getMotif(),
                transfert.getStatut(),
                transfert.getDateCreation(),
                transfert.getEnfantSource() != null ? transfert.getEnfantSource().getId() : null,
                transfert.getEnfantDestination() != null ? transfert.getEnfantDestination().getId() : null,
                transfert.getParrain() != null ? transfert.getParrain().getId() : null
        );
    }*/

    // Classe interne pour les statistiques
    public static class ValidationStatsDto {
        public final long enAttente;
        public final long validees;
        public final long rejetees;

        public ValidationStatsDto(long enAttente, long validees, long rejetees) {
            this.enAttente = enAttente;
            this.validees = validees;
            this.rejetees = rejetees;
        }
    }
}
