package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.DepenseDto;
import com.groupe2_ionic.eduka.dto.DepenseResponseDto;
import com.groupe2_ionic.eduka.dto.DepenseDetailDto;
import com.groupe2_ionic.eduka.dto.DepenseStatistiqueDto;
import com.groupe2_ionic.eduka.models.Depense;
import com.groupe2_ionic.eduka.models.Organisation;
import com.groupe2_ionic.eduka.models.Enfant;
import com.groupe2_ionic.eduka.repository.DepenseRepository;
import com.groupe2_ionic.eduka.repository.OrganisationRepository;
import com.groupe2_ionic.eduka.repository.EnfantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepenseService {

    private final DepenseRepository depenseRepository;
    private final OrganisationRepository organisationRepository;
    private final EnfantRepository enfantRepository;

    @Transactional
    public DepenseResponseDto creerDepense(DepenseDto depenseDto) {
        // Vérifier que l'organisation existe
        Organisation organisation = organisationRepository.findById(depenseDto.getOrganisationId())
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        // Vérifier que l'enfant existe
        Enfant enfant = enfantRepository.findById(depenseDto.getEnfantId())
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));

        // Créer la dépense
        Depense depense = new Depense();
        depense.setTypeDepense(depenseDto.getTypeDepense());
        depense.setJustificatif(depenseDto.getJustificatif());
        depense.setMontant(depenseDto.getMontant());
        depense.setDateEnregistrement(LocalDate.now());
        depense.setOrganisation(organisation);
        depense.setEnfant(enfant);

        Depense depenseSauvegardee = depenseRepository.save(depense);
        return convertToResponseDto(depenseSauvegardee);
    }

    @Transactional
    public DepenseResponseDto modifierDepense(int depenseId, DepenseDto depenseDto) {
        Depense depense = depenseRepository.findById(depenseId)
                .orElseThrow(() -> new RuntimeException("Dépense non trouvée"));

        // Vérifier que l'organisation existe
        Organisation organisation = organisationRepository.findById(depenseDto.getOrganisationId())
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        // Vérifier que l'enfant existe
        Enfant enfant = enfantRepository.findById(depenseDto.getEnfantId())
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));

        // Mettre à jour les champs
        depense.setTypeDepense(depenseDto.getTypeDepense());
        depense.setJustificatif(depenseDto.getJustificatif());
        depense.setMontant(depenseDto.getMontant());
        depense.setOrganisation(organisation);
        depense.setEnfant(enfant);

        Depense depenseModifiee = depenseRepository.save(depense);
        return convertToResponseDto(depenseModifiee);
    }

    @Transactional
    public void supprimerDepense(int depenseId) {
        if (!depenseRepository.existsById(depenseId)) {
            throw new RuntimeException("Dépense non trouvée");
        }
        depenseRepository.deleteById(depenseId);
    }

    public DepenseResponseDto getDepenseById(int depenseId) {
        Depense depense = depenseRepository.findById(depenseId)
                .orElseThrow(() -> new RuntimeException("Dépense non trouvée"));
        return convertToResponseDto(depense);
    }

    public Page<DepenseResponseDto> getDepensesParOrganisation(int organisationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateEnregistrement").descending());
        Page<Depense> depenses = depenseRepository.findByOrganisationId(organisationId, pageable);
        return depenses.map(this::convertToResponseDto);
    }

    public Page<DepenseResponseDto> getDepensesParEnfant(int enfantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateEnregistrement").descending());
        Page<Depense> depenses = depenseRepository.findByEnfantId(enfantId, pageable);
        return depenses.map(this::convertToResponseDto);
    }

    public List<DepenseStatistiqueDto> getStatistiquesDepenses(int organisationId) {
        List<Depense> depenses = depenseRepository.findByOrganisationIdOrderByDateEnregistrementDesc(organisationId);

        return depenses.stream()
                .collect(Collectors.groupingBy(
                        Depense::getTypeDepense,
                        Collectors.reducing(
                                new DepenseStatistiqueDto("", BigDecimal.ZERO, 0L),
                                depense -> new DepenseStatistiqueDto(
                                        depense.getTypeDepense().toString(),
                                        depense.getMontant(),
                                        1L
                                ),
                                (a, b) -> new DepenseStatistiqueDto(
                                        a.getTypeDepense().isEmpty() ? b.getTypeDepense() : a.getTypeDepense(),
                                        a.getTotalMontant().add(b.getTotalMontant()),
                                        a.getNombreDepenses() + b.getNombreDepenses()
                                )
                        )
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }


    private DepenseResponseDto convertToResponseDto(Depense depense) {
        DepenseResponseDto dto = new DepenseResponseDto();
        dto.setId(depense.getId());
        dto.setTypeDepense(depense.getTypeDepense());
        dto.setJustificatif(depense.getJustificatif());
        dto.setMontant(depense.getMontant());
        dto.setDateEnregistrement(depense.getDateEnregistrement());
        dto.setOrganisationNom(depense.getOrganisation().getNom());
        dto.setEnfantNom(depense.getEnfant().getPrenom() + " " + depense.getEnfant().getNom());
        return dto;
    }
}
