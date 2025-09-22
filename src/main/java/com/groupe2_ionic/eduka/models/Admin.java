package com.groupe2_ionic.eduka.models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.*;

import java.util.Set;

@Entity @Getter @Setter @AllArgsConstructor @NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id_admin")
public class Admin extends Utilisateur{

    // Relation
    // Un admin peut valider plusieurs organisations
    @OneToMany(mappedBy = "validateur")
    private Set<Organisation> organisations;
}
