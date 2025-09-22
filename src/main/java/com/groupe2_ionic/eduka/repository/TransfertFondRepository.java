package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.TransfertFond;
import com.groupe2_ionic.eduka.models.enums.StatutTransfert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransfertFondRepository extends JpaRepository<TransfertFond, Integer>{

    List<TransfertFond> findByOrganisationIdOrderByDateDemandeDesc(int organisationId);
    Page<TransfertFond> findByOrganisationId(int organisationId, Pageable pageable);

    List<TransfertFond> findByStatut(StatutTransfert statut);
    List<TransfertFond> findByOrganisationIdAndStatut(int organisationId, StatutTransfert statut);
    List<TransfertFond> findByStatutIn(List<StatutTransfert> statuts);

    List<TransfertFond> findByDateDemandeBetween(LocalDate dateDebut, LocalDate dateFin);
    List<TransfertFond> findByOrganisationIdAndDateDemandeBetween(int organisationId, LocalDate dateDebut, LocalDate dateFin);

    List<TransfertFond> findByEnfantSourceId(int enfantId);
    List<TransfertFond> findByEnfantCibleId(int enfantId);
    List<TransfertFond> findByEnfantSourceIdOrEnfantCibleId(int enfantSourceId, int enfantCibleId);

    List<TransfertFond> findByParrainIdOrderByDateDemandeDesc(int parrainId);
    List<TransfertFond> findByParrainIdAndStatut(int parrainId, StatutTransfert statut);

    Long countByOrganisationId(int organisationId);
    Long countByOrganisationIdAndStatut(int organisationId, StatutTransfert statut);

    @Query("SELECT SUM(t.montant) FROM TransfertFond t WHERE t.organisation.id = :organisationId")
    BigDecimal sumMontantByOrganisationId(@Param("organisationId") int organisationId);

    @Query("SELECT SUM(t.montant) FROM TransfertFond t WHERE t.organisation.id = :organisationId AND t.statut = :statut")
    BigDecimal sumMontantByOrganisationIdAndStatut(@Param("organisationId") int organisationId, @Param("statut") StatutTransfert statut);

    @Query("SELECT t FROM TransfertFond t WHERE t.statut = 'EN_ATTENTE' AND t.enfantSource.tuteur.id = :tuteurId")
    List<TransfertFond> findTransfertsEnAttenteParent(@Param("tuteurId") int tuteurId);

    @Query("SELECT t FROM TransfertFond t WHERE t.statut = 'EN_ATTENTE' AND t.parrain.id = :parrainId")
    List<TransfertFond> findTransfertsEnAttenteParrain(@Param("parrainId") int parrainId);
}
