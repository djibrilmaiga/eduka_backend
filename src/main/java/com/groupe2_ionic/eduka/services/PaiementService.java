package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.PaiementEspeceDto;
import com.groupe2_ionic.eduka.dto.PaiementHistoriqueDto;
import com.groupe2_ionic.eduka.dto.PaiementRequestDto;
import com.groupe2_ionic.eduka.dto.PaiementResponseDto;
import com.groupe2_ionic.eduka.models.Organisation;
import com.groupe2_ionic.eduka.models.Paiement;
import com.groupe2_ionic.eduka.models.Parrain;
import com.groupe2_ionic.eduka.models.Parrainage;
import com.groupe2_ionic.eduka.models.enums.MethodePaiement;
import com.groupe2_ionic.eduka.models.enums.StatutPaiement;
import com.groupe2_ionic.eduka.repository.OrganisationRepository;
import com.groupe2_ionic.eduka.repository.PaiementReposiroty;
import com.groupe2_ionic.eduka.repository.ParrainRepository;
import com.groupe2_ionic.eduka.repository.ParrainageRepository;
import com.groupe2_ionic.eduka.services.payment.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaiementService {
    private final PaiementReposiroty paiementRepository;
    private final ParrainageRepository parrainageRepository;
    private final ParrainRepository parrainRepository;
    private final OrganisationRepository organisationRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Services d'intégration
    private final StripePaymentService stripePaymentService;
    private final PayPalPaymentService payPalPaymentService;
    private final OrangeMoneyService orangeMoneyService;
    private final MoovMoneyService moovMoneyService;
    private final WavePaymentService wavePaymentService;

    /**
     * Initier un paiement selon la méthode choisie
     */
    @Transactional
    public PaiementResponseDto initierPaiement(PaiementRequestDto requestDto) {
        // Vérifier que le parrainage existe
        Parrainage parrainage = parrainageRepository.findById(requestDto.getParrainageId())
                .orElseThrow(() -> new RuntimeException("Parrainage non trouvé"));

        // Créer l'enregistrement de paiement
        Paiement paiement = new Paiement();
        paiement.setMontant(requestDto.getMontant());
        paiement.setMethode(requestDto.getMethodePaiement());
        paiement.setStatut(StatutPaiement.INITE);
        paiement.setDatePaiement(LocalDate.now());
        paiement.setParrain(parrainage.getParrain());
        paiement.setParrainage(parrainage);

        // Sauvegarder le paiement initial
        paiement = paiementRepository.save(paiement);

        try {
            // Traiter selon la méthode de paiement
            PaiementResponseDto response = switch (requestDto.getMethodePaiement()) {
                case STRIPE -> stripePaymentService.creerPaiement(paiement, requestDto);
                case PAYPAL -> payPalPaymentService.creerPaiement(paiement, requestDto);
                case ORANGE_MONEY -> orangeMoneyService.initierPaiement(paiement, requestDto);
                case MOOV_MONEY -> moovMoneyService.initierPaiement(paiement, requestDto);
                case WAVE -> wavePaymentService.initierPaiement(paiement, requestDto);
                case ESPECE -> traiterPaiementEspece(paiement);
                default -> throw new RuntimeException("Méthode de paiement non supportée");
            };

            return response;

        } catch (Exception e) {
            log.error("Erreur lors de l'initiation du paiement: {}", e.getMessage());

            // Marquer le paiement comme échoué
            paiement.setStatut(StatutPaiement.ECHEC);
            paiement.setMessageErreur(e.getMessage());
            paiementRepository.save(paiement);

            throw new RuntimeException("Erreur lors du paiement: " + e.getMessage());
        }
    }

    /**
     * Confirmer un paiement (callback des services externes)
     */
    @Transactional
    public void confirmerPaiement(String transactionId, StatutPaiement nouveauStatut, String codeConfirmation) {
        Paiement paiement = paiementRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

        paiement.setStatut(nouveauStatut);
        paiement.setCodeConfirmation(codeConfirmation);

        paiementRepository.save(paiement);

        PaiementService.PaiementEvent event = new PaiementService.PaiementEvent(paiement, nouveauStatut);
        eventPublisher.publishEvent(event);

        log.info("Paiement {} confirmé avec statut: {}", transactionId, nouveauStatut);
    }

    /**
     * Traiter un paiement en espèce (enregistré par l'organisation)
     */
    private PaiementResponseDto traiterPaiementEspece(Paiement paiement) {
        // Les paiements en espèce sont confirmés manuellement par l'organisation
        paiement.setStatut(StatutPaiement.INITE);
        paiement = paiementRepository.save(paiement);

        return convertirEnResponseDto(paiement);
    }

    /**
     * Enregistrer un paiement en espèces (par une organisation)
     */
    @Transactional
    public PaiementResponseDto enregistrerPaiementEspece(PaiementEspeceDto paiementEspeceDto) {
        // Vérifier que l'organisation existe
        Organisation organisation = organisationRepository.findById(paiementEspeceDto.getOrganisationId())
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        // Vérifier que le parrainage existe
        Parrainage parrainage = parrainageRepository.findById(paiementEspeceDto.getParrainageId())
                .orElseThrow(() -> new RuntimeException("Parrainage non trouvé"));

        // Créer le paiement en espèces
        Paiement paiement = new Paiement();
        paiement.setMontant(paiementEspeceDto.getMontant());
        paiement.setMethode(MethodePaiement.ESPECE);
        paiement.setStatut(StatutPaiement.REUSSI); // Les paiements en espèces sont automatiquement confirmés
        paiement.setDatePaiement(paiementEspeceDto.getDateReception());
        paiement.setParrain(parrainage.getParrain());
        paiement.setParrainage(parrainage);
        paiement.setOrganisation(organisation);

        // Générer un ID de transaction unique
        String transactionId = "ESPECE_" + System.currentTimeMillis();
        paiement.setTransactionId(transactionId);
        paiement.setCodeConfirmation(paiementEspeceDto.getReferenceReception());

        // Métadonnées
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("nom_donateur", paiementEspeceDto.getNomDonateur());
        metadata.put("contact_donateur", paiementEspeceDto.getContactDonateur());
        metadata.put("commentaire", paiementEspeceDto.getCommentaire());
        metadata.put("reference_reception", paiementEspeceDto.getReferenceReception());
        paiement.setMetadonnees(metadata.toString());

        // Sauvegarder le paiement
        paiement = paiementRepository.save(paiement);

        PaiementService.PaiementEspeceEvent event = new PaiementService.PaiementEspeceEvent(paiement, organisation, parrainage);
        eventPublisher.publishEvent(event);

        log.info("Paiement en espèces enregistré: {} FCFA par l'organisation {}",
                paiement.getMontant(), organisation.getNom());

        return convertirEnResponseDto(paiement);
    }

    /**
     * Récupérer l'historique des paiements d'un parrain
     */
    public Page<PaiementHistoriqueDto> getHistoriquePaiements(Integer parrainId, Pageable pageable) {
        // Vérifier que le parrain existe
        Parrain parrain = parrainRepository.findById(parrainId)
                .orElseThrow(() -> new RuntimeException("Parrain non trouvé"));

        // Récupérer les paiements du parrain
        Page<Paiement> paiements = paiementRepository.findByParrainIdOrderByDatePaiementDesc(parrainId, pageable);

        // Convertir en DTO
        List<PaiementHistoriqueDto> historiqueDtos = paiements.getContent().stream()
                .map(this::convertirEnHistoriqueDto)
                .collect(Collectors.toList());

        return new PageImpl<>(historiqueDtos, pageable, paiements.getTotalElements());
    }

    /**
     * Récupérer l'historique des paiements d'une organisation
     */
    public Page<PaiementHistoriqueDto> getHistoriqueOrganisation(Integer organisationId, Pageable pageable) {
        // Vérifier que l'organisation existe
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        // Récupérer les paiements enregistrés par l'organisation
        Page<Paiement> paiements = paiementRepository.findByOrganisationIdOrderByDatePaiementDesc(organisationId, pageable);

        // Convertir en DTO
        List<PaiementHistoriqueDto> historiqueDtos = paiements.getContent().stream()
                .map(this::convertirEnHistoriqueDto)
                .collect(Collectors.toList());

        return new PageImpl<>(historiqueDtos, pageable, paiements.getTotalElements());
    }

    /**
     * Récupérer les statistiques de paiement d'un parrain
     */
    public Map<String, Object> getStatistiquesPaiements(Integer parrainId) {
        // Vérifier que le parrain existe
        Parrain parrain = parrainRepository.findById(parrainId)
                .orElseThrow(() -> new RuntimeException("Parrain non trouvé"));

        List<Paiement> paiements = paiementRepository.findByParrainId(parrainId);

        Map<String, Object> statistiques = new HashMap<>();

        // Statistiques générales
        statistiques.put("nombreTotalPaiements", paiements.size());
        statistiques.put("nombrePaiementsReussis",
                paiements.stream().filter(p -> p.getStatut() == StatutPaiement.REUSSI).count());

        // Montants
        double montantTotal = paiements.stream()
                .filter(p -> p.getStatut() == StatutPaiement.REUSSI)
                .mapToDouble(p -> p.getMontant().doubleValue())
                .sum();
        statistiques.put("montantTotalPaye", montantTotal);

        // Répartition par méthode de paiement
        Map<MethodePaiement, Long> repartitionMethodes = paiements.stream()
                .filter(p -> p.getStatut() == StatutPaiement.REUSSI)
                .collect(Collectors.groupingBy(Paiement::getMethode, Collectors.counting()));
        statistiques.put("repartitionParMethode", repartitionMethodes);

        // Paiement le plus récent
        paiements.stream()
                .filter(p -> p.getStatut() == StatutPaiement.REUSSI)
                .max((p1, p2) -> p1.getDatePaiement().compareTo(p2.getDatePaiement()))
                .ifPresent(dernierPaiement -> {
                    statistiques.put("dernierPaiementDate", dernierPaiement.getDatePaiement());
                    statistiques.put("dernierPaiementMontant", dernierPaiement.getMontant());
                });

        return statistiques;
    }

    /**
     * Convertir un Paiement en PaiementResponseDto
     */
    private PaiementResponseDto convertirEnResponseDto(Paiement paiement) {
        return PaiementResponseDto.builder()
                .id(paiement.getId())
                .montant(paiement.getMontant())
                .methodePaiement(paiement.getMethode())
                .statut(paiement.getStatut())
                .datePaiement(paiement.getDatePaiement())
                .parrainId(paiement.getParrain() != null ? paiement.getParrain().getId() : null)
                .enfantId(paiement.getParrainage() != null && paiement.getParrainage().getEnfant() != null ?
                        paiement.getParrainage().getEnfant().getId() : null)
                .organisationId(paiement.getOrganisation() != null ? paiement.getOrganisation().getId() : null)
                .transactionId(paiement.getTransactionId())
                .paymentUrl(null) // défini ailleurs si besoin
                .codeConfirmation(paiement.getCodeConfirmation())
                .messageErreur(paiement.getMessageErreur())
                // legacy fields si nécessaires
                .parrainNom(paiement.getParrain() != null ? paiement.getParrain().getNom() : null)
                .enfantNom(paiement.getParrainage() != null && paiement.getParrainage().getEnfant() != null ? paiement.getParrainage().getEnfant().getNom() : null)
                .organisationNom(paiement.getOrganisation() != null ? paiement.getOrganisation().getNom() : null)
                .referenceTransaction(paiement.getTransactionId())
                .build();
    }


    /**
     * Convertir un Paiement en PaiementHistoriqueDto
     */
    private PaiementHistoriqueDto convertirEnHistoriqueDto(Paiement paiement) {
        PaiementHistoriqueDto dto = new PaiementHistoriqueDto();
        dto.setId(paiement.getId());
        dto.setMontant(paiement.getMontant());
        dto.setMethodePaiement(paiement.getMethode());
        dto.setStatut(paiement.getStatut());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setTransactionId(paiement.getTransactionId());
        dto.setCodeConfirmation(paiement.getCodeConfirmation());
        dto.setNumeroTelephone(paiement.getNumeroTelephone());
        dto.setMessageErreur(paiement.getMessageErreur());

        // Informations sur l'enfant
        if (paiement.getParrainage() != null && paiement.getParrainage().getEnfant() != null) {
            dto.setEnfantNom(paiement.getParrainage().getEnfant().getNom());
            dto.setEnfantPrenom(paiement.getParrainage().getEnfant().getPrenom());
            dto.setEnfantPhoto(paiement.getParrainage().getEnfant().getPhotoProfil());
        }

        // Type de paiement
        dto.setTypeParrainage(paiement.getParrainage() != null ?
                (paiement.getParrainage().getStatut().toString().equals("ACTIF") ? "DON_COMPLEMENTAIRE" : "NOUVEAU_PARRAINAGE")
                : "INCONNU");

        // Description
        dto.setDescriptionPaiement(String.format("Paiement %s de %s FCFA",
                paiement.getMethode().toString().toLowerCase().replace("_", " "),
                paiement.getMontant()));

        // Informations organisation (pour paiements en espèces)
        if (paiement.getOrganisation() != null) {
            dto.setOrganisationNom(paiement.getOrganisation().getNom());
            dto.setOrganisationContact(paiement.getOrganisation().getEmail());
        }

        return dto;
    }

    /**
     * Événement publié lors de la confirmation d'un paiement
     */
    public static class PaiementEvent {
        private final Paiement paiement;
        private final StatutPaiement statut;

        public PaiementEvent(Paiement paiement, StatutPaiement statut) {
            this.paiement = paiement;
            this.statut = statut;
        }

        public Paiement getPaiement() { return paiement; }
        public StatutPaiement getStatut() { return statut; }
    }

    /**
     * Événement publié lors de l'enregistrement d'un paiement en espèces
     */
    public static class PaiementEspeceEvent {
        private final Paiement paiement;
        private final Organisation organisation;
        private final Parrainage parrainage;

        public PaiementEspeceEvent(Paiement paiement, Organisation organisation, Parrainage parrainage) {
            this.paiement = paiement;
            this.organisation = organisation;
            this.parrainage = parrainage;
        }

        public Paiement getPaiement() { return paiement; }
        public Organisation getOrganisation() { return organisation; }
        public Parrainage getParrainage() { return parrainage; }
    }
}
