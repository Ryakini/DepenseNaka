package com.ydev.depenseNaka.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "id_utilisateur_sequence")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdUtilisateurSequence {
    @Id
    @Column(name = "id_sequence")
    private String idSequence;
    @Column(name = "last_value")
    private long lastValue;
}
