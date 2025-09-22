package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.*;
import com.groupe2_ionic.eduka.models.*;
import com.groupe2_ionic.eduka.models.enums.RoleUser;
import com.groupe2_ionic.eduka.models.enums.StatutParrainage;
import com.groupe2_ionic.eduka.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ParrainService {

    private final ParrainRepository parrainRepository;
    private final EnfantRepository enfantRepository;
    private final ParrainageRepository parrainageRepository;
    private final PaiementReposiroty paiementRepository;
    private final NotificationService notificationService;
    // private final PasswordEncoder passwordEncoder; // À décommenter quand Spring Security sera activé

    /**
     * Inscription d'un nouveau parrain
     */
    public ParrainResponseDto inscrireParrain(ParrainDto parrainDto) {
        // Vérifier si l'email ou le téléphone existe déjà
        if (parrainRepository.findByEmail(parrainDto.getEmail()).isPresent()) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }
        if (parrainRepository.findByTelephone(parrainDto.getTelephone()).isPresent()) {
            throw new RuntimeException("Un compte avec ce téléphone existe déjà");
        }

        Parrain parrain = new Parrain();
        parrain.setNom(parrainDto.getNom());
        parrain.setPrenom(parrainDto.getPrenom());
        parrain.setEmail(parrainDto.getEmail());
        parrain.setTelephone(parrainDto.getTelephone());
        // parrain.setPassword(passwordEncoder.encode(parrainDto.getPassword())); // À décommenter avec Spring Security
        parrain.setPassword(parrainDto.getPassword()); // Temporaire sans encodage
        parrain.setVille(parrainDto.getVille());
        parrain.setPays(parrainDto.getPays());
        parrain.setPhotoProfil(parrainDto.getPhotoProfil());
        parrain.setAnonyme(parrainDto.getAnonyme());
        parrain.setRole(RoleUser.ROLE_PARRAIN);
        parrain.setDateInscription(LocalDate.now());
        parrain.setActif(true);

        Parrain savedParrain = parrainRepository.save(parrain);

        // Envoyer notification de bienvenue
        notificationService.envoyerNotificationBienvenue(savedParrain);

        return mapToResponseDto(savedParrain);
    }

    /**
     * Obtenir le profil d'un parrain
     */
    @Transactional(readOnly = true)
    public ParrainResponseDto obtenirProfilParrain(int parrainId) {
        Parrain parrain = parrainRepository.findById(parrainId)
                .orElseThrow(() -> new RuntimeException("Parrain non trouvé"));
        return mapToResponseDto(parrain);
    }

    /**
     * Mettre à jour le profil d'un parrain
     */
    public ParrainResponseDto mettreAJourProfil(int parrainId, ParrainDto parrainDto) {
        Parrain parrain = parrainRepository.findById(parrainId)
                .orElseThrow(() -> new RuntimeException("Parrain non trouvé"));

        parrain.setNom(parrainDto.getNom());
        parrain.setPrenom(parrainDto.getPrenom());
        parrain.setVille(parrainDto.getVille());
        parrain.setPays(parrainDto.getPays());
        parrain.setPhotoProfil(parrainDto.getPhotoProfil());
        parrain.setAnonyme(parrainDto.getAnonyme());

        Parrain savedParrain = parrainRepository.save(parrain);
        return mapToResponseDto(savedParrain);
    }

    /**
     * Parcourir la liste des enfants disponibles pour parrainage
     */
    @Transactional(readOnly = true)
    public Page<EnfantResponseDto> parcourirEnfantsDisponibles(
            String organisationNom,
            String zone,
            String niveauScolaire,
            Pageable pageable) {

        Page<Enfant> enfants = enfantRepository.findEnfantsDisponiblesAvecFiltres(
                organisationNom, zone, niveauScolaire, pageable);

        return enfants.map(this::mapEnfantToResponseDto);
    }

    /**
     * Obtenir le détail d'un enfant pour parrainage
     */
    @Transactional(readOnly = true)
    public EnfantResponseDto obtenirDetailEnfant(int enfantId) {
        Enfant enfant = enfantRepository.findById(enfantId)
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));

        if (enfant.getStatutParrainage()) {
            throw new RuntimeException("Cet enfant est déjà parrainé");
        }

        return mapEnfantToResponseDto(enfant);
    }

    /**
     * Créer un parrainage pour un enfant
     */
    public ParrainageResponseDto creerParrainage(int parrainId, ParrainageDto parrainageDto) {
        Parrain parrain = parrainRepository.findById(parrainId)
                .orElseThrow(() -> new RuntimeException("Parrain non trouvé"));

        Enfant enfant = enfantRepository.findById(parrainageDto.getEnfantId())
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));

        if (enfant.getStatutParrainage()) {
            throw new RuntimeException("Cet enfant est déjà parrainé");
        }

        // Créer le parrainage
        Parrainage parrainage = new Parrainage();
        parrainage.setParrain(parrain);
        parrainage.setEnfant(enfant);
        parrainage.setStatut(StatutParrainage.ACTIF);
        parrainage.setDateDebut(LocalDate.now());
        parrainage.setMontantTotal(parrainageDto.getMontantTotal());

        Parrainage savedParrainage = parrainageRepository.save(parrainage);

        // Mettre à jour le statut de l'enfant
        enfant.setStatutParrainage(true);
        enfantRepository.save(enfant);

        // Envoyer notifications
        notificationService.notifierNouveauParrainage(savedParrainage);

        return mapParrainageToResponseDto(savedParrainage);
    }

    /**
     * Obtenir l'historique des parrainages d'un parrain
     */
    @Transactional(readOnly = true)
    public List<ParrainageResponseDto> obtenirHistoriqueParrainages(int parrainId) {
        List<Parrainage> parrainages = parrainageRepository.findByParrainIdOrderByDateDebutDesc(parrainId);
        return parrainages.stream()
                .map(this::mapParrainageToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir l'historique des paiements d'un parrain
     */
    @Transactional(readOnly = true)
    public List<PaiementResponseDto> obtenirHistoriquePaiements(int parrainId) {
        List<Paiement> paiements = paiementRepository.findByParrainIdOrderByDatePaiementDesc(parrainId);
        return paiements.stream()
                .map(this::mapPaiementToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Valider une demande de transfert résiduel
     */
    public void validerTransfertResiduel(int parrainId, int transfertId, int nouvelEnfantId) {
        // Cette méthode sera implémentée dans TransfertFondService
        // mais appelée depuis le workflow parrain
    }

    // Méthodes utilitaires de mapping
    private ParrainResponseDto mapToResponseDto(Parrain parrain) {
        ParrainResponseDto dto = new ParrainResponseDto();
        dto.setId(parrain.getId());
        dto.setNom(parrain.getNom());
        dto.setPrenom(parrain.getPrenom());
        dto.setEmail(parrain.getEmail());
        dto.setTelephone(parrain.getTelephone());
        dto.setVille(parrain.getVille());
        dto.setPays(parrain.getPays());
        dto.setPhotoProfil(parrain.getPhotoProfil());
        dto.setAnonyme(parrain.getAnonyme());
        dto.setActif(parrain.getActif());
        dto.setDateInscription(parrain.getDateInscription());
        dto.setNombreParrainages(parrain.getParrainages().size());
        dto.setNombrePaiements(parrain.getPaiements().size());
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
        dto.setOrganisationNom(enfant.getOrganisation() != null ? enfant.getOrganisation().getNom() : null);
        dto.setTuteurNom(enfant.getTuteur() != null ? enfant.getTuteur().getNom() + " " + enfant.getTuteur().getPrenom() : null);
        dto.setEcoleNom(enfant.getEcole() != null ? enfant.getEcole().getNom() : null);
        dto.setNombreParrainages(enfant.getParrainages() != null ? enfant.getParrainages().size() : 0);
        dto.setNombreRapports(enfant.getRapports() != null ? enfant.getRapports().size() : 0);
        return dto;
    }

    private ParrainageResponseDto mapParrainageToResponseDto(Parrainage parrainage) {
        ParrainageResponseDto dto = new ParrainageResponseDto();
        dto.setId(parrainage.getId());
        dto.setStatut(parrainage.getStatut());
        dto.setDateDebut(parrainage.getDateDebut());
        dto.setMontantTotal(parrainage.getMontantTotal());
        dto.setDateFin(parrainage.getDateFin());
        dto.setMotifFin(parrainage.getMotifFin());
        dto.setParrainNom(parrainage.getParrain().getNom());
        dto.setParrainPrenom(parrainage.getParrain().getPrenom());
        dto.setEnfantNom(parrainage.getEnfant().getNom());
        dto.setEnfantPrenom(parrainage.getEnfant().getPrenom());
        dto.setNombrePaiements(parrainage.getPaiements().size());
        // Calculer montant payé et restant
        return dto;
    }

    private PaiementResponseDto mapPaiementToResponseDto(Paiement paiement) {
        PaiementResponseDto dto = new PaiementResponseDto();
        dto.setId(paiement.getId());
        dto.setMethode(paiement.getMethode());
        dto.setMontant(paiement.getMontant());
        dto.setStatut(paiement.getStatut());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setParrainNom(paiement.getParrain().getNom() + " " + paiement.getParrain().getPrenom());
        dto.setEnfantNom(paiement.getParrainage().getEnfant().getNom() + " " + paiement.getParrainage().getEnfant().getPrenom());
        dto.setOrganisationNom(paiement.getOrganisation() != null ? paiement.getOrganisation().getNom() : null);
        return dto;
    }
}
