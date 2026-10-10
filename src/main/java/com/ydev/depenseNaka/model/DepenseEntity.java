package com.ydev.depenseNaka.model;

import com.ydev.depenseNaka.service.CommandeService;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "depense")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepenseEntity {
    @Id
    @Column(name = "id_depense")
    private String idDepense;

    private LocalDate dateCreationDepense;
    private LocalDate dateModificationDepense;


    @NotNull(message = "Le libellé de dépense est obligatoire")
    private String libelleDepense;
    @Min(0)
    private double prixDepense;
    @Min(0)
    private double quantiteDepense;
    @Min(0)
    private double montantDepense;
    private String descriptionDepense;

    //relation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_commande")
    private CommandeEntity commande;

}
