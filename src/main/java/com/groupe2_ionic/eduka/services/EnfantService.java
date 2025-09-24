package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.EnfantDto;
import com.groupe2_ionic.eduka.dto.EnfantResponseDto;
import com.groupe2_ionic.eduka.models.*;
import com.groupe2_ionic.eduka.models.enums.StatutParrainage;
import com.groupe2_ionic.eduka.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EnfantService {

    private final EnfantRepository enfantRepository;
    private final TuteurRepository tuteurRepository;
    private final EcoleRepository ecoleRepository;
    private final OrganisationRepository organisationRepository;
    private final ParrainageRepository parrainageRepository;

    public EnfantResponseDto creerEnfant(EnfantDto enfantDto) {
        // Vérifier que l'organisation existe
        Organisation organisation = organisationRepository.findById(enfantDto.getOrganisationId())
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        // Enregistre les informations du tuteur et de l'école de l'enfant
        Tuteur tuteur = new Tuteur();
        tuteur.setNom(enfantDto.getNomTuteur());
        tuteur.setPrenom(enfantDto.getPrenomTuteur());
        tuteur.setTelephone(enfantDto.getTelephoneTuteur());

        Ecole ecole = new Ecole();
        ecole.setNom(enfantDto.getNomEcole());
        ecole.setVille(enfantDto.getVilleEcole());
        ecole.setPays(enfantDto.getPaysEcole());

        Enfant enfant = new Enfant();
        enfant.setNom(enfantDto.getNom());
        enfant.setPrenom(enfantDto.getPrenom());
        enfant.setGenre(enfantDto.getGenre());
        enfant.setDateNaissance(enfantDto.getDateNaissance());
        enfant.setNiveauScolaire(enfantDto.getNiveauScolaire());
        enfant.setHistoire(enfantDto.getHistoire());
        enfant.setPhotoProfil(enfantDto.getPhotoProfil());
        enfant.setStatutParrainage(false); // Nouveau enfant non parrainé
        enfant.setSolde(BigDecimal.ZERO);
        enfant.setConsentementPedagogique(enfantDto.getConsentementPedagogique());
        enfant.setOrganisation(organisation);
        enfant.setEcole(ecole); // enregistre l'école
        enfant.setTuteur(tuteur); // enregistre le tuteur

        Enfant enfantSauvegarde = enfantRepository.save(enfant);
        return mapToResponseDto(enfantSauvegarde);
    }

    public EnfantResponseDto modifierEnfant(int enfantId, EnfantDto enfantDto) {
        Enfant enfant = enfantRepository.findById(enfantId)
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));

        // Vérifier que l'organisation a le droit de modifier cet enfant
        if (!(enfant.getOrganisation().getId() == (enfantDto.getOrganisationId()))) {
            throw new RuntimeException("Cette organisation n'a pas le droit de modifier cet enfant");
        }

        enfant.setNom(enfantDto.getNom());
        enfant.setPrenom(enfantDto.getPrenom());
        enfant.setGenre(enfantDto.getGenre());
        enfant.setDateNaissance(enfantDto.getDateNaissance());
        enfant.setNiveauScolaire(enfantDto.getNiveauScolaire());
        enfant.setHistoire(enfantDto.getHistoire());
        enfant.setPhotoProfil(enfantDto.getPhotoProfil());
        enfant.setConsentementPedagogique(enfantDto.getConsentementPedagogique());

        // Mettre à jour le tuteur si nécessaire
        if (enfantDto.getTuteurId() != null) {
            Tuteur tuteur = tuteurRepository.findById(enfantDto.getTuteurId())
                    .orElseThrow(() -> new RuntimeException("Tuteur non trouvé"));
            tuteur.setPrenom(enfantDto.getPrenomTuteur());
            tuteur.setNom(enfantDto.getNomTuteur());
            tuteur.setTelephone(enfantDto.getTelephoneTuteur());
            enfant.setTuteur(tuteur);
        }

        // Mettre à jour l'école si nécessaire
        if (enfantDto.getEcoleId() != null) {
            Ecole ecole = ecoleRepository.findById(enfantDto.getEcoleId())
                    .orElseThrow(() -> new RuntimeException("École non trouvée"));
            ecole.setNom(enfantDto.getNomEcole());
            ecole.setPays(enfantDto.getPaysEcole());
            ecole.setVille(enfantDto.getVilleEcole());
            enfant.setEcole(ecole);
        }

        Enfant enfantModifie = enfantRepository.save(enfant);
        return mapToResponseDto(enfantModifie);
    }

    public void supprimerEnfant(int enfantId, int organisationId) {
        Enfant enfant = enfantRepository.findById(enfantId)
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));

        // Vérifier que l'organisation a le droit de supprimer cet enfant
        if (!(enfant.getOrganisation().getId() == (organisationId))) {
            throw new RuntimeException("Cette organisation n'a pas le droit de supprimer cet enfant");
        }

        // Vérifier qu'il n'y a pas de parrainages actifs
        long parrainagesActifs = parrainageRepository.countParrainagesActifsByEnfantId(enfantId);
        if (parrainagesActifs > 0) {
            throw new RuntimeException("Impossible de supprimer un enfant avec des parrainages actifs");
        }

        enfantRepository.delete(enfant);
    }

    public Page<EnfantResponseDto> getEnfantsDisponibles(int page, int size, String organisationNom, String zone, String niveauScolaire) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateNaissance").descending());

        Page<Enfant> enfants = enfantRepository.findEnfantsDisponiblesAvecFiltres(
                organisationNom, zone, niveauScolaire, pageable);

        return enfants.map(this::mapToResponseDto);
    }

    public Page<EnfantResponseDto> getEnfantsParOrganisation(int organisationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nom").ascending());
        Page<Enfant> enfants = enfantRepository.findByOrganisationId(organisationId, pageable);
        return enfants.map(this::mapToResponseDto);
    }

    public EnfantResponseDto getEnfantById(int enfantId) {
        Enfant enfant = enfantRepository.findById(enfantId)
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));
        return mapToResponseDto(enfant);
    }

    public List<EnfantResponseDto> getFilleulsParParrain(int parrainId) {
        List<Parrainage> parrainagesActifs = parrainageRepository.findByParrainIdAndStatut(
                parrainId, StatutParrainage.ACTIF);

        return parrainagesActifs.stream()
                .map(parrainage -> mapToResponseDto(parrainage.getEnfant()))
                .toList();
    }

    public EnfantStatsDto getStatistiquesEnfants(int organisationId) {
        long totalEnfants = enfantRepository.countByOrganisationId(organisationId);
        long enfantsParraines = enfantRepository.countByOrganisationIdAndStatutParrainageTrue(organisationId);
        long enfantsDisponibles = enfantRepository.countByOrganisationIdAndStatutParrainageFalse(organisationId);

        return new EnfantStatsDto(totalEnfants, enfantsParraines, enfantsDisponibles);
    }

    private EnfantResponseDto mapToResponseDto(Enfant enfant) {
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

        if (enfant.getOrganisation() != null) {
            dto.setOrganisationNom(enfant.getOrganisation().getNom());
        }

        if (enfant.getTuteur() != null) {
            dto.setTuteurNom(enfant.getTuteur().getNom() + " " + enfant.getTuteur().getPrenom());
        }

        if (enfant.getEcole() != null) {
            dto.setEcoleNom(enfant.getEcole().getNom());
        }

        if (enfant.getParrainages() != null) {
            dto.setNombreParrainages(enfant.getParrainages().size());
        }

        if (enfant.getRapports() != null) {
            dto.setNombreRapports(enfant.getRapports().size());
        }

        return dto;
    }

    public static class EnfantStatsDto {
        private final long totalEnfants;
        private final long enfantsParraines;
        private final long enfantsDisponibles;

        public EnfantStatsDto(long totalEnfants, long enfantsParraines, long enfantsDisponibles) {
            this.totalEnfants = totalEnfants;
            this.enfantsParraines = enfantsParraines;
            this.enfantsDisponibles = enfantsDisponibles;
        }

        public long getTotalEnfants() { return totalEnfants; }
        public long getEnfantsParraines() { return enfantsParraines; }
        public long getEnfantsDisponibles() { return enfantsDisponibles; }
    }
}
