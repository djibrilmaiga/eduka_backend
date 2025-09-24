package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.TransfertFondDto;
import com.groupe2_ionic.eduka.dto.TransfertFondResponseDto;
import com.groupe2_ionic.eduka.models.TransfertFond;
import com.groupe2_ionic.eduka.models.Organisation;
import com.groupe2_ionic.eduka.models.Enfant;
import com.groupe2_ionic.eduka.models.Parrain;
import com.groupe2_ionic.eduka.models.enums.StatutTransfert;
import com.groupe2_ionic.eduka.repository.TransfertFondRepository;
import com.groupe2_ionic.eduka.repository.OrganisationRepository;
import com.groupe2_ionic.eduka.repository.EnfantRepository;
import com.groupe2_ionic.eduka.repository.ParrainRepository;
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
public class TransfertFondService {

    private final TransfertFondRepository transfertFondRepository;
    private final OrganisationRepository organisationRepository;
    private final EnfantRepository enfantRepository;
    private final ParrainRepository parrainRepository;

    @Transactional
    public TransfertFondResponseDto creerDemandeTransfert(TransfertFondDto transfertDto) {
        // Vérifier que l'organisation existe
        Organisation organisation = organisationRepository.findById(transfertDto.getOrganisationId())
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        // Vérifier que l'enfant source existe
        Enfant enfantSource = enfantRepository.findById(transfertDto.getEnfantSourceId())
                .orElseThrow(() -> new RuntimeException("Enfant source non trouvé"));

        // Vérifier l'enfant cible s'il est spécifié
        Enfant enfantCible = null;
        if (transfertDto.getEnfantCibleId() != null) {
            enfantCible = enfantRepository.findById(transfertDto.getEnfantCibleId())
                    .orElseThrow(() -> new RuntimeException("Enfant cible non trouvé"));
        }

        // Vérifier que le parrain existe
        Parrain parrain = parrainRepository.findById(transfertDto.getParrainId())
                .orElseThrow(() -> new RuntimeException("Parrain non trouvé"));

        // Créer la demande de transfert
        TransfertFond transfert = new TransfertFond();
        transfert.setMotif(transfertDto.getMotif());
        transfert.setDescription(transfertDto.getDescription());
        transfert.setMontant(transfertDto.getMontant());
        transfert.setStatut(StatutTransfert.EN_ATTENTE);
        transfert.setDateDemande(LocalDate.now());
        transfert.setEnfantSource(enfantSource);
        transfert.setEnfantCible(enfantCible);
        transfert.setOrganisation(organisation);
        transfert.setParrain(parrain);

        TransfertFond transfertSauvegarde = transfertFondRepository.save(transfert);
        return convertToResponseDto(transfertSauvegarde);
    }

    @Transactional
    public TransfertFondResponseDto validerTransfert(int transfertId, int parrainId, String commentaire, boolean approuve) {
        TransfertFond transfert = transfertFondRepository.findById(transfertId)
                .orElseThrow(() -> new RuntimeException("Transfert non trouvé"));

        // Vérifier que le parrain existe et correspond
        Parrain parrain = parrainRepository.findById(parrainId)
                .orElseThrow(() -> new RuntimeException("Parrain non trouvé"));

        if (!(transfert.getParrain().getId() == (parrainId))) {
            throw new RuntimeException("Ce parrain n'est pas autorisé à valider ce transfert");
        }

        // Mettre à jour le statut
        transfert.setStatut(approuve ? StatutTransfert.VALIDE : StatutTransfert.REJETE);
        transfert.setDateTraitement(LocalDate.now());
        transfert.setCommentaireValidation(commentaire);

        TransfertFond transfertModifie = transfertFondRepository.save(transfert);
        return convertToResponseDto(transfertModifie);
    }

    public TransfertFondResponseDto getTransfertById(int transfertId) {
        TransfertFond transfert = transfertFondRepository.findById(transfertId)
                .orElseThrow(() -> new RuntimeException("Transfert non trouvé"));
        return convertToResponseDto(transfert);
    }

    public Page<TransfertFondResponseDto> getTransfertsParOrganisation(int organisationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateDemande").descending());
        Page<TransfertFond> transferts = transfertFondRepository.findByOrganisationId(organisationId, pageable);
        return transferts.map(this::convertToResponseDto);
    }

    public Page<TransfertFondResponseDto> getTransfertsParStatut(StatutTransfert statut, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateDemande").descending());
        Page<TransfertFond> transferts = transfertFondRepository.findByStatut(statut, pageable);
        return transferts.map(this::convertToResponseDto);
    }

    public Page<TransfertFondResponseDto> getTransfertsParParrain(int parrainId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateDemande").descending());
        Page<TransfertFond> transferts = transfertFondRepository.findByParrainId(parrainId, pageable);
        return transferts.map(this::convertToResponseDto);
    }

    public List<TransfertFondResponseDto> getTransfertsEnAttente() {
        List<TransfertFond> transferts = transfertFondRepository.findByStatutOrderByDateDemandeAsc(StatutTransfert.EN_ATTENTE);
        return transferts.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    private TransfertFondResponseDto convertToResponseDto(TransfertFond transfert) {
        TransfertFondResponseDto dto = new TransfertFondResponseDto();
        dto.setId(transfert.getId());
        dto.setMotif(transfert.getMotif());
        dto.setDescription(transfert.getDescription());
        dto.setMontant(transfert.getMontant());
        dto.setStatut(transfert.getStatut());
        dto.setDateDemande(transfert.getDateDemande());
        dto.setDateTraitement(transfert.getDateTraitement());
        dto.setCommentaireValidation(transfert.getCommentaireValidation());
        dto.setOrganisationNom(transfert.getOrganisation().getNom());
        dto.setEnfantSourceNom(transfert.getEnfantSource().getPrenom() + " " + transfert.getEnfantSource().getNom());
        if (transfert.getEnfantCible() != null) {
            dto.setEnfantCibleNom(transfert.getEnfantCible().getPrenom() + " " + transfert.getEnfantCible().getNom());
        }
        dto.setParrainNom(transfert.getParrain().getPrenom() + " " + transfert.getParrain().getNom());
        return dto;
    }
}
