package com.ydev.depenseNaka.service;

import com.ydev.depenseNaka.dto.UtilisateurDTO;
import com.ydev.depenseNaka.exception.ExistUtilisateur;
import com.ydev.depenseNaka.exception.ImpossibleDeleteUtilisateur;
import com.ydev.depenseNaka.exception.NotFoundUtilisateur;
import com.ydev.depenseNaka.mapper.UtilisateurMapper;
import com.ydev.depenseNaka.model.IdUtilisateurSequence;
import com.ydev.depenseNaka.model.RoleUtilisateur;
import com.ydev.depenseNaka.model.UtilisateurEntity;
import com.ydev.depenseNaka.model.UtilisteurAdmin;
import com.ydev.depenseNaka.repository.IdUtilisateurSequenceRepository;
import com.ydev.depenseNaka.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UtilisateurService {
    private final UtilisateurRepository utilisateurRepository;
    private final IdUtilisateurSequenceRepository idUtilisateurSequenceRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    //creation
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public UtilisateurDTO createUtilisateur(UtilisateurDTO utilisateurDTO) {
        //verification pseudo
        if(utilisateurRepository.existsByPseudoUtilisateur(utilisateurDTO.pseudoUtilisateur())){
                throw new ExistUtilisateur(utilisateurDTO.pseudoUtilisateur());
        }

        //Incréméntation séquence
        IdUtilisateurSequence idUtilisateurSequence = idUtilisateurSequenceRepository.findById("id_sequence")
                .orElse(new IdUtilisateurSequence("id_sequence", 0L));

        idUtilisateurSequence.setLastValue(idUtilisateurSequence.getLastValue() + 1);
        idUtilisateurSequenceRepository.save(idUtilisateurSequence);

        if (utilisateurDTO.pseudoUtilisateur().isEmpty() || utilisateurDTO.pseudoUtilisateur().isBlank()) {
            throw new RuntimeException("Ce champs est obligatoire !");
        }


        RoleUtilisateur role = utilisateurDTO.roleUtilisateur() != null ? utilisateurDTO.roleUtilisateur() : RoleUtilisateur.UTILISATEUR;
        String prefix = (role == RoleUtilisateur.ADMINISTRATEUR) ? "ADM" : "USE";
        String customId = String.format("%s-%03d", prefix, idUtilisateurSequence.getLastValue());

        UtilisateurEntity saveUser = utilisateurMapper.toEntity(utilisateurDTO);
        saveUser.setIdUtilisateur(customId);
        saveUser.setRoleUtilisateur(role);
        if (!utilisateurDTO.mdpUtilisateur().isEmpty() && !utilisateurDTO.mdpUtilisateur().isBlank()) {
            String encode = bCryptPasswordEncoder.encode(utilisateurDTO.mdpUtilisateur());
            saveUser.setMdpUtilisateur(encode);
        }

        UtilisateurEntity savedEntity = utilisateurRepository.save(saveUser);

        return utilisateurMapper.toDTO(savedEntity);
    }

    //update
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public UtilisateurDTO  updateUtilisateur(String idUtilisateur, UtilisateurDTO utilisateurDTO) {
        UtilisateurEntity existUtilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(()-> new NotFoundUtilisateur(idUtilisateur));

        //modification
        if (utilisateurDTO.nomUtilisateur() != null && !utilisateurDTO.nomUtilisateur().isBlank()) {
            existUtilisateur.setNomUtilisateur(utilisateurDTO.nomUtilisateur());
        }

        if (utilisateurDTO.prenomUtilisateur() != null && !utilisateurDTO.prenomUtilisateur().isBlank()) {
            existUtilisateur.setPrenomUtilisateur(utilisateurDTO.prenomUtilisateur());
        }

        if (utilisateurDTO.pseudoUtilisateur() != null && !utilisateurDTO.pseudoUtilisateur().isBlank()) {
            existUtilisateur.setPseudoUtilisateur(utilisateurDTO.pseudoUtilisateur());
        }

        if (utilisateurDTO.adresseUtilisateur() != null && !utilisateurDTO.adresseUtilisateur().isBlank()) {
            existUtilisateur.setAdresseUtilisateur(utilisateurDTO.adresseUtilisateur());
        }

        if (utilisateurDTO.telephoneUtilisateur() != null && !utilisateurDTO.telephoneUtilisateur().isBlank()) {
            existUtilisateur.setTelephoneUtilisateur(utilisateurDTO.telephoneUtilisateur());
        }

        if (existUtilisateur instanceof UtilisteurAdmin && utilisateurDTO.roleUtilisateur().equals(RoleUtilisateur.ADMINISTRATEUR)) {
            existUtilisateur.setRoleUtilisateur(RoleUtilisateur.ADMINISTRATEUR);
        }else {
            existUtilisateur.setRoleUtilisateur(RoleUtilisateur.UTILISATEUR);
        }

        //gestion de mot de passe
        String newMdp = utilisateurDTO.mdpUtilisateur();
        if(newMdp != null && !newMdp.isEmpty()){
            boolean isEncoder = newMdp.startsWith("$2a$");
            //verification si c'est encoder donc on compare les encodages si c'est égal ou  non
            if(!isEncoder){
                //si texte clair, on verifiie si c'est différent de l'ancien
                if (!bCryptPasswordEncoder.matches(newMdp, existUtilisateur.getMdpUtilisateur())) {
                    existUtilisateur.setMdpUtilisateur(bCryptPasswordEncoder.encode(newMdp)); //modification si s'est différent
                }else {
                   existUtilisateur.setMdpUtilisateur(newMdp);
                }
            }

        }

        UtilisateurEntity savedUtilisateur = utilisateurRepository.save(existUtilisateur);

        return utilisateurMapper.toDTO(savedUtilisateur);
    }

    //delete
    @PreAuthorize("hasAuthority('ADMINISTRATEUR')")
    public void deleteUtilisateur(String idUtilisateur) {
        //verification existence utilisateur
       UtilisateurEntity existUtilisateur = utilisateurRepository.findById(idUtilisateur)
               .orElseThrow(()-> new NotFoundUtilisateur(idUtilisateur));

       //verification si c'est lié à une commande
        if (existUtilisateur.getCommandes() != null && !existUtilisateur.getCommandes().isEmpty()) {
            throw new ImpossibleDeleteUtilisateur(existUtilisateur.getIdUtilisateur());
        }else {
            utilisateurRepository.deleteById(existUtilisateur.getIdUtilisateur());
        }
    }

    //vreifier
    public boolean existUtilisateur(String idUtilisateur) {
        if (utilisateurRepository.findById(idUtilisateur).isPresent()) {
            return true;
        }else {
            return false;
        }
    }

    public boolean isAdmin(String idUtilisateur) {
        if (idUtilisateur == null) return false;

        return utilisateurRepository.findById(idUtilisateur)
                .map(u -> u.getRoleUtilisateur() == RoleUtilisateur.ADMINISTRATEUR)
                .orElse(false);
    }


    //liste
    @Transactional(readOnly = true)
    public UtilisateurDTO listeByIdUtilisateur(String idUtilisateur) {
        return utilisateurRepository.findById(idUtilisateur)
                .map(utilisateurMapper::toDTO)
                .orElseThrow(()-> new NotFoundUtilisateur(idUtilisateur));
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> listeUtilisateur() {
        return utilisateurRepository.findAll()
                .stream()
                .map(utilisateurMapper::toDTO)
                .toList();
    }
}
