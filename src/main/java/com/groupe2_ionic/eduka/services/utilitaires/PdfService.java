package com.groupe2_ionic.eduka.services.utilitaires;

import com.groupe2_ionic.eduka.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfService {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Génère un reçu de paiement en PDF
     */
    public byte[] genererRecuPaiement(Paiement paiement) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Simulation de génération PDF
            // Dans un vrai projet, utiliser iText, Apache PDFBox, ou Flying Saucer

            String contenu = genererContenuRecuPaiement(paiement);

            // Pour la simulation, on retourne le contenu en bytes
            // Dans la réalité, on utiliserait une bibliothèque PDF
            baos.write(contenu.getBytes("UTF-8"));

            log.info("Reçu de paiement généré pour le paiement ID: {}", paiement.getId());
            return baos.toByteArray();
        }
    }

    /**
     * Génère un rapport mensuel d'organisation en PDF
     */
    public byte[] genererRapportMensuelOrganisation(Organisation organisation, LocalDate mois,
                                                    List<Paiement> paiements, List<Enfant> enfants) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            String contenu = genererContenuRapportMensuel(organisation, mois, paiements, enfants);
            baos.write(contenu.getBytes("UTF-8"));

            log.info("Rapport mensuel généré pour l'organisation: {}", organisation.getNom());
            return baos.toByteArray();
        }
    }

    /**
     * Génère un rapport de parrainage en PDF
     */
    public byte[] genererRapportParrainage(Parrainage parrainage, List<Paiement> paiements) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            String contenu = genererContenuRapportParrainage(parrainage, paiements);
            baos.write(contenu.getBytes("UTF-8"));

            log.info("Rapport de parrainage généré pour le parrainage ID: {}", parrainage.getId());
            return baos.toByteArray();
        }
    }

    /**
     * Génère un rapport administratif global en PDF
     */
    public byte[] genererRapportAdministratif(LocalDate dateDebut, LocalDate dateFin,
                                              List<Organisation> organisations, List<Paiement> paiements,
                                              BigDecimal montantTotal) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            String contenu = genererContenuRapportAdministratif(dateDebut, dateFin, organisations, paiements, montantTotal);
            baos.write(contenu.getBytes("UTF-8"));

            log.info("Rapport administratif généré pour la période: {} - {}", dateDebut, dateFin);
            return baos.toByteArray();
        }
    }

    /**
     * Génère le contenu du reçu de paiement
     */
    private String genererContenuRecuPaiement(Paiement paiement) {
        return String.format("""
            =====================================
                    REÇU DE PAIEMENT
            =====================================
            
            Date: %s
            Référence: %s
            
            INFORMATIONS PARRAIN:
            Nom: %s %s
            Email: %s
            Téléphone: %s
            
            INFORMATIONS ENFANT:
            Nom: %s %s
            Âge: %d ans
            École: %s
            
            DÉTAILS DU PAIEMENT:
            Montant: %.0f FCFA
            Méthode: %s
            Statut: %s
            Date de paiement: %s
            Code de confirmation: %s
            
            ORGANISATION:
            %s
            %s, %s
            
            =====================================
            Merci pour votre contribution à l'éducation !
            =====================================
            """,
                LocalDate.now().format(dateFormatter),
                paiement.getTransactionId() != null ? paiement.getTransactionId() : "N/A",
                paiement.getParrain().getNom(),
                paiement.getParrain().getPrenom(),
                paiement.getParrain().getEmail(),
                paiement.getParrain().getTelephone(),
                paiement.getParrainage().getEnfant().getNom(),
                paiement.getParrainage().getEnfant().getPrenom(),
                paiement.getParrainage().getEnfant().getAge(),
                paiement.getParrainage().getEnfant().getEcole(),
                paiement.getMontant(),
                paiement.getMethode(),
                paiement.getStatut(),
                paiement.getDatePaiement().format(dateFormatter),
                paiement.getCodeConfirmation() != null ? paiement.getCodeConfirmation() : "N/A",
                paiement.getParrainage().getEnfant().getOrganisation().getNom(),
                paiement.getParrainage().getEnfant().getOrganisation().getVille(),
                paiement.getParrainage().getEnfant().getOrganisation().getPays()
        );
    }

    /**
     * Génère le contenu du rapport mensuel d'organisation
     */
    private String genererContenuRapportMensuel(Organisation organisation, LocalDate mois,
                                                List<Paiement> paiements, List<Enfant> enfants) {
        BigDecimal montantTotal = paiements.stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return String.format("""
            =====================================
                RAPPORT MENSUEL - %s
            =====================================
            
            ORGANISATION: %s
            Représentant: %s %s (%s)
            Localisation: %s, %s
            Email: %s
            Téléphone: %s
            
            PÉRIODE: %s
            
            STATISTIQUES:
            - Nombre d'enfants: %d
            - Nombre de paiements: %d
            - Montant total reçu: %.0f FCFA
            
            DÉTAIL DES PAIEMENTS:
            %s
            
            LISTE DES ENFANTS:
            %s
            
            =====================================
            Rapport généré le %s
            =====================================
            """,
                mois.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                organisation.getNom(),
                organisation.getNomRepresentant(),
                organisation.getPrenomRepresentant(),
                organisation.getFonctionRepresentant(),
                organisation.getVille(),
                organisation.getPays(),
                organisation.getEmail(),
                organisation.getTelephone(),
                mois.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                enfants.size(),
                paiements.size(),
                montantTotal,
                genererDetailPaiements(paiements),
                genererListeEnfants(enfants),
                LocalDate.now().format(dateFormatter)
        );
    }

    /**
     * Génère le contenu du rapport de parrainage
     */
    private String genererContenuRapportParrainage(Parrainage parrainage, List<Paiement> paiements) {
        BigDecimal montantTotal = paiements.stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return String.format("""
            =====================================
                RAPPORT DE PARRAINAGE
            =====================================
            
            PARRAIN:
            Nom: %s %s
            Email: %s
            Téléphone: %s
            Pays: %s
            
            ENFANT PARRAINÉ:
            Nom: %s %s
            Âge: %d ans
            École: %s
            Organisation: %s
            
            PARRAINAGE:
            Date de début: %s
            Statut: %s
            Montant mensuel: %.0f FCFA
            
            HISTORIQUE DES PAIEMENTS:
            Nombre total: %d
            Montant total: %.0f FCFA
            
            %s
            
            =====================================
            Rapport généré le %s
            =====================================
            """,
                parrainage.getParrain().getNom(),
                parrainage.getParrain().getPrenom(),
                parrainage.getParrain().getEmail(),
                parrainage.getParrain().getTelephone(),
                parrainage.getParrain().getPays(),
                parrainage.getEnfant().getNom(),
                parrainage.getEnfant().getPrenom(),
                parrainage.getEnfant().getAge(),
                parrainage.getEnfant().getEcole(),
                parrainage.getEnfant().getOrganisation().getNom(),
                parrainage.getDateDebut().format(dateFormatter),
                parrainage.getStatut(),
                parrainage.getMontantTotal(),
                paiements.size(),
                montantTotal,
                genererDetailPaiements(paiements),
                LocalDate.now().format(dateFormatter)
        );
    }

    /**
     * Génère le contenu du rapport administratif
     */
    private String genererContenuRapportAdministratif(LocalDate dateDebut, LocalDate dateFin,
                                                      List<Organisation> organisations, List<Paiement> paiements,
                                                      BigDecimal montantTotal) {
        return String.format("""
            =====================================
                RAPPORT ADMINISTRATIF
            =====================================
            
            PÉRIODE: %s - %s
            
            STATISTIQUES GLOBALES:
            - Nombre d'organisations: %d
            - Nombre de paiements: %d
            - Montant total: %.0f FCFA
            
            RÉPARTITION PAR ORGANISATION:
            %s
            
            DÉTAIL DES PAIEMENTS:
            %s
            
            =====================================
            Rapport généré le %s
            =====================================
            """,
                dateDebut.format(dateFormatter),
                dateFin.format(dateFormatter),
                organisations.size(),
                paiements.size(),
                montantTotal,
                genererRepartitionOrganisations(organisations),
                genererDetailPaiements(paiements),
                LocalDate.now().format(dateFormatter)
        );
    }

    /**
     * Génère le détail des paiements
     */
    private String genererDetailPaiements(List<Paiement> paiements) {
        if (paiements.isEmpty()) {
            return "Aucun paiement pour cette période.";
        }

        StringBuilder sb = new StringBuilder();
        for (Paiement p : paiements) {
            sb.append(String.format("- %s: %.0f FCFA (%s) - %s\n",
                    p.getDatePaiement().format(dateFormatter),
                    p.getMontant(),
                    p.getMethode(),
                    p.getStatut()
            ));
        }
        return sb.toString();
    }

    /**
     * Génère la liste des enfants
     */
    private String genererListeEnfants(List<Enfant> enfants) {
        if (enfants.isEmpty()) {
            return "Aucun enfant enregistré.";
        }

        StringBuilder sb = new StringBuilder();
        for (Enfant e : enfants) {
            sb.append(String.format("- %s %s (%d ans) - %s\n",
                    e.getNom(),
                    e.getPrenom(),
                    e.getAge(),
                    e.getEcole()
            ));
        }
        return sb.toString();
    }

    /**
     * Génère la répartition par organisations
     */
    private String genererRepartitionOrganisations(List<Organisation> organisations) {
        if (organisations.isEmpty()) {
            return "Aucune organisation.";
        }

        StringBuilder sb = new StringBuilder();
        for (Organisation o : organisations) {
            sb.append(String.format("- %s (%s, %s)\n",
                    o.getNom(),
                    o.getVille(),
                    o.getPays()
            ));
        }
        return sb.toString();
    }
}
