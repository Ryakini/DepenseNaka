package com.ydev.depenseNaka;

import com.ydev.depenseNaka.dto.*;
import com.ydev.depenseNaka.model.*;
import com.ydev.depenseNaka.repository.CategorieRepository;
import com.ydev.depenseNaka.repository.FactureProduitRepository;
import com.ydev.depenseNaka.repository.FactureRepository;
import com.ydev.depenseNaka.repository.ProduitRepository;
import com.ydev.depenseNaka.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest

class DepenseNakaApplicationTests {

	//catégorie produi
	@Autowired
     private CategorieRepository categorieRepository;
	@Autowired
	private CategorieService categorieService;

	//Produit
	@Autowired
	private ProduitRepository produitRepository;
	@Autowired
	private ProduitService produitService;
	private CategorieEntity categorieTest;

	//Facture
	@Autowired
	private FactureRepository factureRepository;
	@Autowired
	private FactureService factureService;
	@Autowired
	private FactureProduitRepository factureProduitRepository;

	//Depense
	@Autowired
	private DepenseService depenseService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private CommandeService commandeService;

/*
	@BeforeEach
	void setUp(){
		categorieTest = new CategorieEntity();
		categorieTest.setNomCategorie("PANTALON");
		categorieTest.setDescriptionCategorie("Tenue de fete");
		categorieRepository.save(categorieTest);
	}
*/
	/*
	@BeforeEach
	void setUp() {
		// Vider la table pour garantir des résultats isolés
		factureRepository.deleteAll();

		// Insertion de jeux de données de test
		FactureEntity f1 = FactureEntity.builder()
				.idFacture("FAC-001")
				.dateFacture(LocalDate.of(2026, 1, 10))
				.status(StatusPaiement.PAYE)
				.modePaiement(ModePaiement.ESPECE)
				.montantFacture(50000.0)
				.build();

		FactureEntity f2 = FactureEntity.builder()
				.idFacture("FAC-002")
				.dateFacture(LocalDate.of(2026, 2, 15))
				.status(StatusPaiement.PARTIEL)
				.modePaiement(ModePaiement.ESPECE)
				.montantFacture(100000.0)
				.build();

		FactureEntity f3 = FactureEntity.builder()
				.idFacture("FAC-003")
				.dateFacture(LocalDate.of(2026, 3, 20))
				.status(StatusPaiement.NON_PAYE)
				.modePaiement(ModePaiement.VIREMENT)
				.montantFacture(75000.0)
				.build();

		factureRepository.saveAll(List.of(f1, f2, f3));
	}
	*/

	//********************************CATEGOIRIE PRODUIT*******************
	//---------------ADD CATEGORIE
	/*
	@Test
	@Transactional
	void testerAddCategorie() {
		//creation
		CategorieEntity vetement = CategorieEntity.builder()
				.nomCategorie("vetementA")
				.descriptionCategorie("Importer").build();

		//enregistrement via service
		CategorieEntity save = categorieService.createCategorie(vetement);

		//creation
		CategorieEntity vetement1 = CategorieEntity.builder()
				.nomCategorie("vetementB")
				.descriptionCategorie("Importer1").build();

		//enregistrement via service
		CategorieEntity save1 = categorieService.createCategorie(vetement1);

		//tester service ajouter
		//assertNotNull(save.getIdCategorie());
		//assertEquals("VETEMENT", save.getNomCategorie());
		//assertEquals("Importer", save.getDescriptionCategorie());

		//tester liste
		List<CategorieEntity> liste =  categorieRepository.findAllByOrderByNomCategorieAsc();
		assertEquals(2, liste.size());
		assertEquals("VETEMENTB", liste.get(1).getNomCategorie());

		List<CategorieEntity> liste1 =  categorieRepository.findByNomCategorieContainingIgnoreCaseOrderByNomCategorieAsc("vetementA");
		assertEquals(1, liste1.size());
		assertEquals("VETEMENTA", liste1.get(0).getNomCategorie());

		//tester service mise à jour
		//save.setNomCategorie("UPDATE");
		//save.setDescriptionCategorie("update importer");

		//-----------------mise à jour
		//CategorieEntity updateCategorie = categorieService.updateCategorie(save.getIdCategorie(), save);



		//+++++++++++++++verification update service
		//assertNotNull(updateCategorie.getIdCategorie());
		//assertEquals("UPDATE", updateCategorie.getNomCategorie());
		//assertEquals("update importer", updateCategorie.getDescriptionCategorie());

		//+++++++++++++tester service delete
		//categorieService.deleteCategorie(save.getIdCategorie());
		//assertThrows(RuntimeException.class, () -> categorieService.deleteCategorie(save.getIdCategorie()),"Le service devrait empecher la supression car c'est null");

		//liste produit
	}
	*/

	//*******************************PRODUIT*****************************
	/*
	@Test
	@Transactional
	@DisplayName("Création d'un nouveau produit et softDelete")
	void testerAddProduit() {
		ProduitEntity produit = ProduitEntity.builder()
				.nomProduit("Teste")
				.quantiteProduit(10)
				.prixAchatProduit(10000.0)
				.prixVenteProduit(15000.0)
				.estDisponibleProduit(true)
				.estActifProduit(true)
				.categorie(categorieTest)
				.build();

		ProduitEntity saveProduct = produitService.createProduit(produit);

		//verification creation
		/*
		assertNotNull(saveProduct.getIdProduit());
		assertTrue(saveProduct.getIdProduit().startsWith("PANT"));
		assertEquals(10, saveProduct.getQuantiteProduit());
		assertEquals(5000.0, saveProduct.getMargeProduit());

		 */

		//verification suppression
	/*
		produitService.deleteProduit(saveProduct.getIdProduit());

		ProduitEntity prodRecuper = produitRepository.findById(saveProduct.getIdProduit()).orElse(null);
		assertNotNull(prodRecuper);
		assertFalse(prodRecuper.isEstActifProduit());

	}
	*/



	//********************************FACTURE**************
	/*
	@Test
	@Transactional
	@DisplayName("Tester création et softDelete facture")
	void testerFacture(){
		ProduitEntity produit = ProduitEntity.builder()
				.nomProduit("Teste")
				.quantiteProduit(10)
				.prixAchatProduit(10000.0)
				.prixVenteProduit(15000.0)
				.estDisponibleProduit(true)
				.estActifProduit(true)
				.categorie(categorieTest)
				.build();

		ProduitEntity saveProduct = produitService.createProduit(produit);

		FactureEntity facture = FactureEntity.builder()
				.avanceFacture(15000)
				.modePaiement(ModePaiement.ESPECE).build();

		FactureProduitEntity ligne = FactureProduitEntity.builder()
				.quantiteVenteProduit(2)
				.produit(saveProduct)
				.build();

		List<FactureProduitEntity> lignes = new ArrayList<>();
		lignes.add(ligne);
		facture.setFactureProduitEntityList(lignes);

		//Creation facture
		/*
		FactureEntity savedFacture = factureService.createFacture(facture);
		assertNotNull(savedFacture.getIdFacture());
		assertEquals(30000.0, savedFacture.getMontantFacture());
		assertEquals(15000.0, savedFacture.getResteFacture());
		assertEquals(StatusPaiement.PARTIEL, savedFacture.getStatus());

		// Vérification de l'impact sur le stock du produit creation
		ProduitEntity prodMaj = produitService.getProduit(saveProduct.getIdProduit());
		assertEquals(8, prodMaj.getQuantiteProduit());
		*/

/*
		//mise à jour facture
		FactureEntity savedFacture = factureService.createFacture(facture);

		// 2. Facture de mise à jour (nouvel objet)
		FactureEntity updateFactureData = FactureEntity.builder()
				.avanceFacture(15000)
				.modePaiement(ModePaiement.ESPECE)
				.build();

		FactureProduitEntity updateLigne = FactureProduitEntity.builder()
				.quantiteVenteProduit(5)
				.produit(saveProduct)
				.build();

		List<FactureProduitEntity> updateLignes = new ArrayList<>();
		updateLignes.add(updateLigne);
		updateFactureData.setFactureProduitEntityList(updateLignes);

		FactureEntity savedUpdateFacture = factureService.updateFacture(savedFacture.getIdFacture(), updateFactureData);

		//verifications
		assertEquals(60000.0, savedUpdateFacture.getResteFacture());
		assertEquals(1, savedUpdateFacture.getFactureProduitEntityList().size());
		assertEquals(75000.0 , savedUpdateFacture.getMontantFacture() );
		assertEquals(StatusPaiement.PARTIEL, savedUpdateFacture.getStatus());

	}

	*/

	//----------------tester recherche
	/*
	@Test
	@Transactional
	@DisplayName("Tester la liste des factures")
	void testerRecherche(){
		List<FactureEntity> resultats = factureService.findAll();

		//verification
		assertNotNull(resultats);
		assertEquals(3, resultats.size());
	}
	*/

	//----------------recherche multicritère
	/*
	@Test
	@Transactional
	@DisplayName("Tester recherche milticritère")
	void testerRechercheMulticriteria(){

	*/
		// Recherche avec '00' dans l'ID, page 0, taille 2
		/*
		Page<FactureEntity> page = factureService.findMultiCriteriaFacture(
				"00", null, null, null, null, 0, 2
		);

		assertNotNull(page);
		assertEquals(3, page.getTotalElements()); // Total en base
		assertEquals(2, page.getContent().size()); // Éléments sur la page courante
		assertEquals(2, page.getTotalPages());    // Total de pages (3 éléments / taille 2)
		assertEquals("FAC-001", page.getContent().get(0).getIdFacture());
		 */

		//Recherche par intervalle de date
		/*
		LocalDate debut = LocalDate.of(2026, 1, 1);
		LocalDate fin = LocalDate.of(2026, 2, 28);

		// Recherche entre Janvier et Février 2026 avec statut PAYE
		Page<FactureEntity> page = factureService.findMultiCriteriaFacture(
				null, debut, fin, StatusPaiement.PAYE, null, 0, 10
		);

		assertEquals(1, page.getTotalElements());
		assertEquals("FAC-001", page.getContent().get(0).getIdFacture());

		 */

		//Recherche par mode de paiment
		/*
		Page<FactureEntity> page = factureService.findMultiCriteriaFacture(
				null, null, null, null, ModePaiement.ESPECE, 0, 10
		);

		assertEquals(2, page.getTotalElements());

	}

		 */

	//**************************DEPENSE*******************


	//************************tester fflux complet
	/*
	@Test
	@Transactional
	@DisplayName("Test du flux complet : Catégorie -> Produit -> Client -> Commande/Dépense -> Facture")
	void testFluxCompletLogiciel() {

		// 1. TEST CATEGORIE SERVICE
		CategorieDTO catDTO = new CategorieDTO(null, "Boissons", "Jus et boissons énergisantes");
		CategorieDTO catCree = categorieService.createCategorie(catDTO);
		assertNotNull(catCree.idCategorie());
		assertEquals("BOISSONS", catCree.nomCategorie());

		// 2. TEST PRODUIT SERVICE
		ProduitDTO prodDTO = new ProduitDTO(
				null, "Jus d'Orange 1L", "Xl",
				LocalDate.now(), true, true, 10,
				15000, 30000, 15000, "tester desc", catCree
		);
		ProduitDTO prodCree = produitService.createProduit(prodDTO);
		assertNotNull(prodCree.idProduit());
		assertEquals(10.0, prodCree.quantiteProduit());

		// 3. TEST CLIENT SERVICE
		ClientDTO clientDTO = new ClientDTO(null, "Raza", "Jean", "0340000000", "Toamasina");
		ClientDTO clientCree = clientService.createClient(clientDTO);
		assertNotNull(clientCree.idClient());

		// 4. TEST COMMANDE SERVICE (Préparation & Validation)
		UtilisateurProduitDTO ligneCmd = new UtilisateurProduitDTO(null, prodCree.idProduit(), prodCree.nomProduit(), 20.0, 1000.0, 20000.0);
		CommandeDTO cmdDTO = new CommandeDTO(
				null, null, null, LocalDate.now().plusDays(2),
				"Fournisseur", "Commande mensuelle",
				StatusCommande.EN_ATTENTE, 20000.0,
				List.of(ligneCmd), List.of()
		);
		CommandeDTO cmdPreparee = commandeService.preparerCommande(cmdDTO);
		assertEquals(StatusCommande.EN_ATTENTE, cmdPreparee.statusCmd());

		// Validation de la commande -> Augmente le stock de +20 (Passage de 10 à 30)
		commandeService.validerCommande(cmdPreparee.idCommande());
		ProduitDTO prodApresCmd = produitService.getProduit(prodCree.idProduit());
		assertEquals(30.0, prodApresCmd.quantiteProduit());

		// 5. TEST DEPENSE SERVICE
		DepenseDTO depenseDTO = new DepenseDTO(
				null, null, null, "Frais de transport commande",
				5000.0, 1.0, 5000.0, "Livraison camion", cmdPreparee.idCommande()
		);
		DepenseDTO depenseCree = depenseService.createDepense(depenseDTO);
		assertNotNull(depenseCree.idDepense());
		assertEquals(5000.0, depenseCree.montantDepense());

		// 6. TEST FACTURE SERVICE (Vente au Client)
		FactureProduitDTO ligneFacture = new FactureProduitDTO(
				null, 1500.0, 5.0, 7500.0, 2500.0, prodCree.idProduit(), prodCree.nomProduit()
		);
		FactureDTO factureDTO = new FactureDTO(
				null, null, null, 7500.0, 7500.0, 0.0, 2500.0,
				ModePaiement.ESPECE, StatusPaiement.PAYE, clientCree, List.of(ligneFacture)
		);
		FactureDTO factureCree = factureService.createFacture(factureDTO);

		assertNotNull(factureCree.idFacture());
		assertEquals(StatusPaiement.PARTIEL, factureCree.status());

		// Vérification de la déduction du stock (-5 unités -> Passe de 30 à 25)
		ProduitDTO prodApresVente = produitService.getProduit(prodCree.idProduit());
		assertEquals(25.0, prodApresVente.quantiteProduit());
	}

*/

}
