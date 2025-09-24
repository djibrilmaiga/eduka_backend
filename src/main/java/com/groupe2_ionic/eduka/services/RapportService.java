package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.RapportDto;
import com.groupe2_ionic.eduka.dto.RapportResponseDto;
import com.groupe2_ionic.eduka.dto.RapportRecentDto;
import com.groupe2_ionic.eduka.models.Rapport;
import com.groupe2_ionic.eduka.models.Organisation;
import com.groupe2_ionic.eduka.models.Enfant;
import com.groupe2_ionic.eduka.repository.RapportRepository;
import com.groupe2_ionic.eduka.repository.OrganisationRepository;
import com.groupe2_ionic.eduka.repository.EnfantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RapportService {

    private final RapportRepository rapportRepository;
    private final OrganisationRepository organisationRepository;
    private final EnfantRepository enfantRepository;

    @Transactional
    public RapportResponseDto creerRapport(RapportDto rapportDto) {
        // Vérifier que l'organisation existe
        Organisation organisation = organisationRepository.findById(rapportDto.getOrganisationId())
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        // Vérifier que l'enfant existe
        Enfant enfant = enfantRepository.findById(rapportDto.getEnfantId())
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));

        // Créer le rapport
        Rapport rapport = new Rapport();
        rapport.setTitre(rapportDto.getTitre());
        rapport.setTypeRapport(rapportDto.getTypeRapport());
        rapport.setPeriode(rapportDto.getPeriode());
        rapport.setContenu(rapportDto.getContenu());
        rapport.setDate(LocalDate.now());
        rapport.setOrganisation(organisation);
        rapport.setEnfant(enfant);

        Rapport rapportSauvegarde = rapportRepository.save(rapport);
        return convertToResponseDto(rapportSauvegarde);
    }

    @Transactional
    public RapportResponseDto modifierRapport(int rapportId, RapportDto rapportDto) {
        Rapport rapport = rapportRepository.findById(rapportId)
                .orElseThrow(() -> new RuntimeException("Rapport non trouvé"));

        // Vérifier que l'organisation existe
        Organisation organisation = organisationRepository.findById(rapportDto.getOrganisationId())
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        // Vérifier que l'enfant existe
        Enfant enfant = enfantRepository.findById(rapportDto.getEnfantId())
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));

        // Mettre à jour les champs
        rapport.setTitre(rapportDto.getTitre());
        rapport.setTypeRapport(rapportDto.getTypeRapport());
        rapport.setPeriode(rapportDto.getPeriode());
        rapport.setContenu(rapportDto.getContenu());
        rapport.setOrganisation(organisation);
        rapport.setEnfant(enfant);

        Rapport rapportModifie = rapportRepository.save(rapport);
        return convertToResponseDto(rapportModifie);
    }

    @Transactional
    public void supprimerRapport(int rapportId) {
        if (!rapportRepository.existsById(rapportId)) {
            throw new RuntimeException("Rapport non trouvé");
        }
        rapportRepository.deleteById(rapportId);
    }

    public RapportResponseDto getRapportById(int rapportId) {
        Rapport rapport = rapportRepository.findById(rapportId)
                .orElseThrow(() -> new RuntimeException("Rapport non trouvé"));
        return convertToResponseDto(rapport);
    }

    public Page<RapportResponseDto> getRapportsParOrganisation(int organisationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Rapport> rapports = rapportRepository.findByOrganisationId(organisationId, pageable);
        return rapports.map(this::convertToResponseDto);
    }

    public Page<RapportResponseDto> getRapportsParEnfant(int enfantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Rapport> rapports = rapportRepository.findByEnfantId(enfantId, pageable);
        return rapports.map(this::convertToResponseDto);
    }

    public Page<RapportResponseDto> getRapportsParType(String typeRapport, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Rapport> rapports = rapportRepository.findByTypeRapport(typeRapport, pageable);
        return rapports.map(this::convertToResponseDto);
    }

    public List<RapportRecentDto> getRapportsRecents(int organisationId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("date").descending());
        Page<Rapport> rapports = rapportRepository.findByOrganisationId(organisationId, pageable);

        return rapports.getContent().stream()
                .map(rapport -> new RapportRecentDto(
                        rapport.getId(),
                        rapport.getTitre(),
                        rapport.getTypeRapport(),
                        rapport.getPeriode(),
                        rapport.getContenu(),
                        rapport.getDate(),
                        rapport.getOrganisation().getNom(),
                        0,       // nombreDocuments → valeur par défaut
                        false,   // enRetard
                        0        // joursDepuisCreation
                ))
                .collect(Collectors.toList());
    }

    private RapportResponseDto convertToResponseDto(Rapport rapport) {
        RapportResponseDto dto = new RapportResponseDto();
        dto.setId(rapport.getId());
        dto.setTitre(rapport.getTitre());
        dto.setTypeRapport(rapport.getTypeRapport());
        dto.setPeriode(rapport.getPeriode());
        dto.setContenu(rapport.getContenu());
        dto.setDate(rapport.getDate());
        dto.setOrganisationNom(rapport.getOrganisation().getNom());
        dto.setEnfantNom(rapport.getEnfant().getPrenom() + " " + rapport.getEnfant().getNom());
        return dto;
    }
}
