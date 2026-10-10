package com.ydev.depenseNaka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "facture")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FactureEntity {
    @Id
    @Column(name = "id_facture")
    private String idFacture;

    @NotNull
    private LocalDate dateFacture;
    private LocalDate dateModifFacture;

    @NotNull
    private double montantFacture;
    @Min(0)
    private double avanceFacture;
    @Min(0)
    private double resteFacture;
    @Min(0)
    private double margeFacture;

    @Enumerated(EnumType.STRING)
    private ModePaiement modePaiement;
    @Enumerated(EnumType.STRING)
    private StatusPaiement status;

    //relation
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @ToString.Exclude
    private List<FactureProduitEntity> factureProduitEntityList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client")
    private ClientEntity client;

}
