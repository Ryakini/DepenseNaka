package com.ydev.depenseNaka.service;

import com.ydev.depenseNaka.dto.ConnexionDTO;
import com.ydev.depenseNaka.exception.IncorrectLogin;
import com.ydev.depenseNaka.mapper.ConnexionMapper;
import com.ydev.depenseNaka.mapper.UtilisateurMapper;
import com.ydev.depenseNaka.model.ConnexionEntity;
import com.ydev.depenseNaka.model.UtilisateurEntity;
import com.ydev.depenseNaka.repository.ConnexionRepository;
import com.ydev.depenseNaka.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnexionService {

    private final UtilisateurRepository utilisateurRepository;
    private final ConnexionRepository connexionRepository;
    private final ConnexionMapper connexionMapper;
    private final BCryptPasswordEncoder  bCryptPasswordEncoder;

    //login, ouverture de la session
    public ConnexionDTO login(String pseudo, String password) {
        //verification pseudo
        UtilisateurEntity existUtilisateur = utilisateurRepository.findByPseudoUtilisateur(pseudo)
                .orElseThrow(()-> new IncorrectLogin());

        System.out.println("Saisi en clair : " + password);
        System.out.println("Hash en BDD : " + existUtilisateur.getMdpUtilisateur());
        System.out.println("Correspondance : " + bCryptPasswordEncoder.matches(password, existUtilisateur.getMdpUtilisateur()));

        //verification mot de passe si on a un meme cryptage
        if (!bCryptPasswordEncoder.matches(password, existUtilisateur.getMdpUtilisateur())) {
            throw new IncorrectLogin();
        }

        //fermeture de session existante
        connexionRepository.findFirstByUtilisateurIdUtilisateurAndIsActiveTrueOrderByDateDebutConnexionDesc(existUtilisateur.getIdUtilisateur())
                .ifPresent(connexionEntity -> {
                    connexionEntity.setActive(false);
                    connexionEntity.setDateFinConnexion(LocalDateTime.now());
                    connexionRepository.save(connexionEntity);
                });

        //création d'une nouvelle session
        ConnexionEntity newConnexion =  ConnexionEntity.builder()
                .idConnexion("SESS-" + System.currentTimeMillis())
                .nomConnexion("Session de : "+existUtilisateur.getPseudoUtilisateur())
                .dateDebutConnexion(LocalDateTime.now())
                .isActive(true)
                .utilisateur(existUtilisateur)
                .build();

        ConnexionEntity savedNewConnexion = connexionRepository.save(newConnexion);

        return connexionMapper.toDTO(savedNewConnexion);

    }

    //log out, fermeture de la session
    public ConnexionDTO logOut(String idConnexion) {
        //verification d'existance de l'identifiant de connexion
       /* ConnexionEntity existConnexion = connexionRepository.findFirstByUtilisateurIdUtilisateurAndIsActiveTrueOrderByDateDebutConnexionDesc(idUtilisateur)
                .orElseThrow(()-> new RuntimeException("Aucune session active trouvée pour l'utilisateur !"));*/
        ConnexionEntity existConnexion = connexionRepository.findById(idConnexion)
                .orElseThrow(()-> new RuntimeException("Aucune session active trouvée pour l'utilisateur !"));


        existConnexion.setActive(false);
        existConnexion.setDateFinConnexion(LocalDateTime.now());
        ConnexionEntity savedConnexion = connexionRepository.save(existConnexion);

        return connexionMapper.toDTO(savedConnexion);

    }

    //compter nombre connexion utilisatuer
    public long countNombreConnexion(String idUtilisateur) {
        return connexionRepository.countByUtilisateurIdUtilisateur(idUtilisateur);
    }

    //historique
    @Transactional(readOnly = true)
    public List<ConnexionDTO> obtenirHistoriqueConnexion(String idUtilisateur) {
        return connexionRepository.findByUtilisateurIdUtilisateurOrderByDateDebutConnexionDesc(idUtilisateur)
                .stream()
                .map(connexionMapper::toDTO)
                .toList();
    }
}
