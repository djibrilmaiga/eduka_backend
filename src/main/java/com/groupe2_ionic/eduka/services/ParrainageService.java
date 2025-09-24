package com.groupe2_ionic.eduka.services;

import com.groupe2_ionic.eduka.dto.ParrainageDto;
import com.groupe2_ionic.eduka.dto.ParrainageResponseDto;
import com.groupe2_ionic.eduka.models.*;
import com.groupe2_ionic.eduka.models.enums.StatutParrainage;
import com.groupe2_ionic.eduka.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ParrainageService {

    private final ParrainageRepository parrainageRepository;
    private final ParrainRepository parrainRepository;
    private final EnfantRepository enfantRepository;
    private final PaiementReposiroty paiementRepository;

    public ParrainageResponseDto creerParrainage(ParrainageDto parrainageDto) {
        // Vérifier que le parrain existe
        Parrain parrain = parrainRepository.findById(parrainageDto.getParrainId())
                .orElseThrow(() -> new RuntimeException("Parrain non trouvé"));

        // Vérifier que l'enfant existe
        Enfant enfant = enfantRepository.findById(parrainageDto.getEnfantId())
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé"));

        // Créer le parrainage
        Parrainage parrainage = new Parrainage();
        parrainage.setParrain(parrain);
        parrainage.setEnfant(enfant);
        parrainage.setStatut(StatutParrainage.ACTIF);
        parrainage.setDateDebut(LocalDate.now());
        parrainage.setMontantTotal(BigDecimal.ZERO); // Sera mis à jour avec les paiements

        Parrainage parrainageSauvegarde = parrainageRepository.save(parrainage);

        // Mettre à jour le statut de parrainage de l'enfant
        enfant.setStatutParrainage(true);
        enfantRepository.save(enfant);

        return mapToResponseDto(parrainageSauvegarde);
    }

    public ParrainageResponseDto terminerParrainage(int parrainageId, String motifFin) {
        Parrainage parrainage = parrainageRepository.findById(parrainageId)
                .orElseThrow(() -> new RuntimeException("Parrainage non trouvé"));

        if (parrainage.getStatut() != StatutParrainage.ACTIF) {
            throw new RuntimeException("Ce parrainage n'est pas actif");
        }

        parrainage.setStatut(StatutParrainage.TERMINE);
        parrainage.setDateFin(LocalDate.now());
        parrainage.setMotifFin(motifFin);

        Parrainage parrainageTermine = parrainageRepository.save(parrainage);

        // Vérifier s'il reste d'autres parrainages actifs pour cet enfant
        long parrainagesActifsRestants = parrainageRepository.countParrainagesActifsByEnfantId(parrainage.getEnfant().getId());

        // Si plus de parrainages actifs, libérer l'enfant
        if (parrainagesActifsRestants == 0) {
            Enfant enfant = parrainage.getEnfant();
            enfant.setStatutParrainage(false);
            enfantRepository.save(enfant);
        }

        return mapToResponseDto(parrainageTermine);
    }

    public ParrainageResponseDto suspendreParrainage(int parrainageId, String motifSuspension) {
        Parrainage parrainage = parrainageRepository.findById(parrainageId)
                .orElseThrow(() -> new RuntimeException("Parrainage non trouvé"));

        if (parrainage.getStatut() != StatutParrainage.ACTIF) {
            throw new RuntimeException("Ce parrainage n'est pas actif");
        }

        parrainage.setStatut(StatutParrainage.SUSPENDU);
        parrainage.setMotifFin(motifSuspension);

        return mapToResponseDto(parrainageRepository.save(parrainage));
    }

    public ParrainageResponseDto reactiverParrainage(int parrainageId) {
        Parrainage parrainage = parrainageRepository.findById(parrainageId)
                .orElseThrow(() -> new RuntimeException("Parrainage non trouvé"));

        if (parrainage.getStatut() != StatutParrainage.SUSPENDU) {
            throw new RuntimeException("Ce parrainage n'est pas suspendu");
        }

        parrainage.setStatut(StatutParrainage.ACTIF);
        parrainage.setMotifFin(null);

        return mapToResponseDto(parrainageRepository.save(parrainage));
    }

    public void mettreAJourMontantTotal(int parrainageId, BigDecimal montantPaiement) {
        Parrainage parrainage = parrainageRepository.findById(parrainageId)
                .orElseThrow(() -> new RuntimeException("Parrainage non trouvé"));

        BigDecimal nouveauMontant = parrainage.getMontantTotal().add(montantPaiement);
        parrainage.setMontantTotal(nouveauMontant);
        parrainageRepository.save(parrainage);
    }

    public void recalculerMontantTotal(int parrainageId) {
        Parrainage parrainage = parrainageRepository.findById(parrainageId)
                .orElseThrow(() -> new RuntimeException("Parrainage non trouvé"));

        BigDecimal montantTotal = paiementRepository.sumMontantByParrainageId(parrainageId);
        if (montantTotal == null) {
            montantTotal = BigDecimal.ZERO;
        }

        parrainage.setMontantTotal(montantTotal);
        parrainageRepository.save(parrainage);
    }

    public List<ParrainageResponseDto> getParrainagesParParrain(int parrainId) {
        List<Parrainage> parrainages = parrainageRepository.findByParrainIdOrderByDateDebutDesc(parrainId);
        return parrainages.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ParrainageResponseDto> getParrainagesActifsParParrain(int parrainId) {
        List<Parrainage> parrainages = parrainageRepository.findByParrainIdAndStatut(parrainId, StatutParrainage.ACTIF);
        return parrainages.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ParrainageResponseDto> getHistoriqueParrainagesEnfant(int enfantId) {
        List<Parrainage> parrainages = parrainageRepository.findByEnfantIdOrderByDateDebutDesc(enfantId);
        return parrainages.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ParrainageResponseDto> getParrainagesActifsEnfant(int enfantId) {
        List<Parrainage> parrainages = parrainageRepository.findByEnfantIdAndStatut(enfantId, StatutParrainage.ACTIF);
        return parrainages.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public ParrainageResponseDto getParrainageById(int parrainageId) {
        Parrainage parrainage = parrainageRepository.findById(parrainageId)
                .orElseThrow(() -> new RuntimeException("Parrainage non trouvé"));
        return mapToResponseDto(parrainage);
    }

    public boolean peutParrainerEnfant(int parrainId, int enfantId) {
        // Vérifier s'il existe déjà un parrainage actif entre ce parrain et cet enfant
        List<Parrainage> parrainagesExistants = parrainageRepository.findByParrainIdAndEnfantIdAndStatut(
                parrainId, enfantId, StatutParrainage.ACTIF);

        return parrainagesExistants.isEmpty();
    }

    public ParrainageStatsDto getStatistiquesParrainage(int parrainId) {
        long totalParrainages = parrainageRepository.countByParrainId(parrainId);
        long parrainagesActifs = parrainageRepository.countParrainagesActifsByParrainId(parrainId);
        long parrainagesTermines = parrainageRepository.countByParrainIdAndStatut(parrainId, StatutParrainage.TERMINE);
        BigDecimal montantTotalVerse = paiementRepository.sumMontantByParrainId(parrainId);

        if (montantTotalVerse == null) {
            montantTotalVerse = BigDecimal.ZERO;
        }

        return new ParrainageStatsDto(totalParrainages, parrainagesActifs, parrainagesTermines, montantTotalVerse);
    }

    private ParrainageResponseDto mapToResponseDto(Parrainage parrainage) {
        ParrainageResponseDto dto = new ParrainageResponseDto();
        dto.setId(parrainage.getId());
        dto.setStatut(parrainage.getStatut());
        dto.setDateDebut(parrainage.getDateDebut());
        dto.setMontantTotal(parrainage.getMontantTotal());
        dto.setDateFin(parrainage.getDateFin());
        dto.setMotifFin(parrainage.getMotifFin());

        if (parrainage.getParrain() != null) {
            dto.setParrainNom(parrainage.getParrain().getNom());
            dto.setParrainPrenom(parrainage.getParrain().getPrenom());
        }

        if (parrainage.getEnfant() != null) {
            dto.setEnfantNom(parrainage.getEnfant().getNom());
            dto.setEnfantPrenom(parrainage.getEnfant().getPrenom());
        }

        if (parrainage.getPaiements() != null) {
            dto.setNombrePaiements(parrainage.getPaiements().size());
        }

        return dto;
    }

    public static class ParrainageStatsDto {
        private final long totalParrainages;
        private final long parrainagesActifs;
        private final long parrainagesTermines;
        private final BigDecimal montantTotalVerse;

        public ParrainageStatsDto(long totalParrainages, long parrainagesActifs, long parrainagesTermines, BigDecimal montantTotalVerse) {
            this.totalParrainages = totalParrainages;
            this.parrainagesActifs = parrainagesActifs;
            this.parrainagesTermines = parrainagesTermines;
            this.montantTotalVerse = montantTotalVerse;
        }

        public long getTotalParrainages() { return totalParrainages; }
        public long getParrainagesActifs() { return parrainagesActifs; }
        public long getParrainagesTermines() { return parrainagesTermines; }
        public BigDecimal getMontantTotalVerse() { return montantTotalVerse; }
    }
}
