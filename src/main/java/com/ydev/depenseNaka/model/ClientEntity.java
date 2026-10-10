package com.ydev.depenseNaka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientEntity {

    @Id
    private String idClient;

    @NotNull(message = "Le nom du client est obligatoire")
    private String nomClient;

    private String prenomClient;
    private String adresseClient;
    private String telephoneClient;

    //relation
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL,orphanRemoval = true)
    @ToString.Exclude
    private List<FactureEntity> facture = new ArrayList<>();
}
