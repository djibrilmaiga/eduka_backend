package com.groupe2_ionic.eduka.services.payment;

import com.groupe2_ionic.eduka.dto.PaiementRequestDto;
import com.groupe2_ionic.eduka.dto.PaiementResponseDto;
import com.groupe2_ionic.eduka.models.Paiement;
import com.groupe2_ionic.eduka.models.enums.StatutPaiement;
import com.groupe2_ionic.eduka.repository.PaiementReposiroty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrangeMoneyService {
    
    private final PaiementReposiroty paiementRepository;
    
    @Value("${orange.money.api.url:https://api.orange.com/orange-money-webpay/ml/v1}")
    private String orangeApiUrl;
    
    @Value("${orange.money.merchant.key:}")
    private String merchantKey;
    
    @Value("${orange.money.client.id:}")
    private String clientId;
    
    @Value("${orange.money.client.secret:}")
    private String clientSecret;
    
    /**
     * Initier un paiement Orange Money
     */
    public PaiementResponseDto initierPaiement(Paiement paiement, PaiementRequestDto requestDto) {
        try {
            // Validation du numéro de téléphone
            if (requestDto.getNumeroTelephone() == null || !requestDto.getNumeroTelephone().matches("^\\+223[67]\\d{7}$")) {
                throw new RuntimeException("Numéro de téléphone Orange Money invalide");
            }
            
            // Générer un ID de transaction unique
            String transactionId = "OM_" + System.currentTimeMillis();
            
            // Simulation de l'appel API Orange Money
            // Dans un vrai projet, faire l'appel HTTP à l'API Orange Money
            
            // Mettre à jour le paiement
            paiement.setTransactionId(transactionId);
            paiement.setNumeroTelephone(requestDto.getNumeroTelephone());
            paiement.setStatut(StatutPaiement.INITE);
            
            paiement = paiementRepository.save(paiement);
            
            // Simulation: envoyer une demande de paiement au téléphone
            log.info("Demande de paiement Orange Money envoyée au {}", requestDto.getNumeroTelephone());
            
            PaiementResponseDto response = new PaiementResponseDto();
            response.setId(paiement.getId());
            response.setMontant(paiement.getMontant());
            response.setMethodePaiement(paiement.getMethode());
            response.setStatut(paiement.getStatut());
            response.setDatePaiement(paiement.getDatePaiement());
            response.setTransactionId(paiement.getTransactionId());
            
            return response;
            
        } catch (Exception e) {
            log.error("Erreur Orange Money: {}", e.getMessage());
            throw new RuntimeException("Erreur lors du paiement Orange Money: " + e.getMessage());
        }
    }
    
    /**
     * Vérifier le statut d'un paiement Orange Money
     */
    public void verifierStatutPaiement(String transactionId) {
        try {
            // Appel API pour vérifier le statut
            // Simulation
            
            Paiement paiement = paiementRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));
            
            // Simulation: 80% de chance de succès
            boolean succes = Math.random() > 0.2;
            
            if (succes) {
                paiement.setStatut(StatutPaiement.REUSSI);
                paiement.setCodeConfirmation("OM_CONF_" + System.currentTimeMillis());
            } else {
                paiement.setStatut(StatutPaiement.ECHEC);
                paiement.setMessageErreur("Paiement refusé par Orange Money");
            }
            
            paiementRepository.save(paiement);
            
        } catch (Exception e) {
            log.error("Erreur lors de la vérification Orange Money: {}", e.getMessage());
        }
    }
}
