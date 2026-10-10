package com.ydev.depenseNaka.utilitaire;

import com.ydev.depenseNaka.DepenseNakaApplication;
import com.ydev.depenseNaka.dto.ProduitDTO;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Data
public class Utilitaire {
    //Chiffre en lettre
    String[] unite = {null," Un"," Deux"," Trois"," Quatre"," Cinq"," Six"," Sept"," Huit"," Neuf"};
    String[] vingtaine = {" Dix"," Onze"," Douze"," Treize"," Quatorze"," Quinze"," Seize"," DixSept"," DixHuit"," DixNeuf"};
    String[] centaine = {null,null," vingt"," Trente"," Quarante"," Cinquante"," Soixante"," SoixanteDix"," QuateVingt"," QuatreVingtDix"};

    //Gasy
    String[] tokana= {null," Raiky "," Roa "," Telo "," Efatra "," Dimy "," Enina "," Fito "," Valo "," Sivy "};
    String[] isaroa= {" Folo "," Raiky ambin'ny folo "," Roa ambin'ny folo ","Telo ambin'ny folo "," Efatra ambin'ny folo "," Dimy ambin'ny folo ","Enina ambin'ny folo"," Fito ambin'ny folo "
            ," Valo ambin'ny folo "," Sivy ambin'ny folo "};
    String[] zato = {null, null," Roapolo "," Telopolo ","Efapolo ","Dimampolo "," Enimpolo "," Fitopolo "," Valopolo "," Sivapolo "};

    //impression facture
    DefaultTableModel tableModel;
    JTextArea textArea;
    String nomClient;

    //format
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    public Utilitaire() {
    }


    //traduction chiffre en lettre inférieur à 1.000.000.000
    public String traduireChiffreEnLettre(int nobreATraduire){
        if (nobreATraduire == 0){
            return "Zéro";
        }else if(nobreATraduire<10){
            return  unite[nobreATraduire];
        }
        else if(nobreATraduire<20){
            return vingtaine[nobreATraduire%10];
        }
        else if(nobreATraduire<100){
            return centaine[nobreATraduire/10]+((nobreATraduire%100>0)?" "+traduireChiffreEnLettre(nobreATraduire%10):" ");
        }
        else if(nobreATraduire<1_000){
            return unite[nobreATraduire/100]+" Cent"+((nobreATraduire%100>0)?""+traduireChiffreEnLettre(nobreATraduire%100):" ");
        }
        else if(nobreATraduire<1_000_000){
            return traduireChiffreEnLettre(nobreATraduire/1_000)+" Mille"+((nobreATraduire%1_000>0)? " "+ traduireChiffreEnLettre(nobreATraduire%1_000):" ");
        }
        else if(nobreATraduire<1_000_000_000){
            return traduireChiffreEnLettre(nobreATraduire/1_000_000)+" Millions"+((nobreATraduire%1_000_000>0)?""+traduireChiffreEnLettre(nobreATraduire%1_000_000):"");
        }
        return traduireChiffreEnLettre(nobreATraduire/1_000_000_000)+" Milliard"+((nobreATraduire%1_000_000_000>0)?""+traduireChiffreEnLettre(nobreATraduire%1_000_000_000):"");
        // return traductionInt(nbrEntrer/1_000)+" Mille"+((nbrEntrer%1_000>0)? " "+ traductionInt(nbrEntrer%1_000):" ");

    }

    //traduction chiffre en lettre gasy
    public String traduireChiffreLettreGasy(int nombreATraduire){
        if (nombreATraduire == 0){
            return "Aotra";
        }else if(nombreATraduire<10)
        {
            return tokana[nombreATraduire];
        }
        else if(nombreATraduire<20)
        {
            return isaroa[nombreATraduire%10];
        }
        else if(nombreATraduire == 100)
        {
            return "Zato";
        }
        else if(nombreATraduire<100)
        {
            return ((nombreATraduire%100>0)?" "+traduireChiffreLettreGasy(nombreATraduire%10):" ")+" amby "+zato[nombreATraduire/10];
        }
        else if(nombreATraduire == 1_000)
        {
            return "Arivo";
        }
        else if(nombreATraduire<1_000)
        {
            return ((nombreATraduire%100>0)?" "+traduireChiffreLettreGasy(nombreATraduire%100):"")+" sy "+ tokana[nombreATraduire/100]+" NJATO ";
        }
        else if(nombreATraduire == 10_000)//10.000=Alina
        {
            return "ray alina";
        }
        else if(nombreATraduire<10_000)//-10.000=Arivo
        {
            return ((nombreATraduire%1_000>0)?""+traduireChiffreLettreGasy(nombreATraduire%1_000):"")+" sy "+traduireChiffreLettreGasy(nombreATraduire/1_000)+" ARIVO ";
        }
        else if(nombreATraduire<100_000)//-100_000=Alina
        {
            return ((nombreATraduire%10_000>0)?""+traduireChiffreLettreGasy(nombreATraduire%10_000):"")+" sy "+traduireChiffreLettreGasy(nombreATraduire/10_000)+" ALINA ";
        }
        else if(nombreATraduire == 100_000)//100.000=HETSY
        {
            return "ray hetsy";
        }
        else if(nombreATraduire == 1_000_000)//1.000.000=Alina
        {
            return "ray tapitrisa ";
        }
        else if(nombreATraduire<1_000_000)
        {
            return ((nombreATraduire%100_000>0)?""+traduireChiffreLettreGasy(nombreATraduire%100_000):"")+" sy "+traduireChiffreLettreGasy(nombreATraduire/100_000)+" hetsy ";
        }
        return ((nombreATraduire%1_000_000>0)?""+traduireChiffreLettreGasy(nombreATraduire%1_000_000):"")+" sy "+traduireChiffreLettreGasy(nombreATraduire/1_000_000)+" tapitrisa ";
    }


    //**********************Separatteur de millier
    public String appliquerSeparateurMillierAr(double montant){
        // Formate avec un espace comme séparateur de milliers et 2 décimales maximum
        // Exemple : 1250000.5 -> 1 250 000.50
        //  setText(String.format("%,.2f", item).replace(",", " ").replace(".", ","));
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);

        return String.format("%-2s Ar", formatter.format(montant));
    }

    public String appliquerSeparateurMillier(double montant){
        // Formate avec un espace comme séparateur de milliers et 2 décimales maximum
        // Exemple : 1250000.5 -> 1 250 000.50
        //  setText(String.format("%,.2f", item).replace(",", " ").replace(".", ","));
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);

        return String.format("%-2s", formatter.format(montant));
    }

    /**
     * Applique un filtre numérique (décimal) réutilisable sur un TextField.
     * Empêche toute saisie de lettres ou caractères spéciaux.
     */
    public static void configureNumericField(TextField textField) {
        Pattern pattern = Pattern.compile("\\d*(\\.\\d*)?");
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (pattern.matcher(newText).matches()) {
                return change;
            }
            return null;
        };
        textField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 1.0, filter));
    }

    /**
     * Convertisseur sécurisé pour les colonnes de TableView (gestion des erreurs de saisie).
     */
    public static StringConverter<Double> getSafeDoubleConverter() {
        return new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return value == null ? "0.0" : String.format("%.2f", value).replace(',', '.');
            }

            @Override
            public Double fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return 0.0;
                }
                try {
                    // Nettoyage de la chaîne avant conversion en double
                    String clean = string.replaceAll("[^0-9.]", "");
                    return clean.isEmpty() ? 0.0 : Double.parseDouble(clean);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        };
    }

    /**
     * Utilitaire pour extraire proprement la valeur numérique d'un TextField.
     */
    public double parseDouble(TextField textField, double defaultValue) {
        try {
            return Double.parseDouble(textField.getText().trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String appliquerSeparateurMillierQuantite(double montant){
        // Formate avec un espace comme séparateur de milliers et 2 décimales maximum
        // Exemple : 1250000.5 -> 1 250 000.50
        //  setText(String.format("%,.2f", item).replace(",", " ").replace(".", ","));
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);

        return String.format("%-2s", formatter.format(montant));
    }

    //Séparateur de millier champ

    /**
     * Applique un formatage de séparateur de milliers en temps réel sur le TextField
     * et retourne une DoubleProperty mise à jour automatiquement pour les calculs.
     */

    public DoubleProperty configurerChampMontantDynamique(TextField textField) {
        DoubleProperty montantProperty = new SimpleDoubleProperty(0.0);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0.##", symbols);

        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                montantProperty.set(0.0);
                return;
            }

            // 1. Conserver uniquement chiffres et séparateur décimal
            String cleanText = newText.replaceAll("[^0-9.,]", "").replace(',', '.');

            if (cleanText.isEmpty()) {
                montantProperty.set(0.0);
                Platform.runLater(() -> textField.setText(""));
                return;
            }

            try {
                // 2. Extraire la valeur numérique pour les calculs
                double doubleValue = Double.parseDouble(cleanText);
                montantProperty.set(doubleValue);

                // 3. Formater la chaîne
                String formattedText;
                if (cleanText.contains(".")) {
                    String[] parts = cleanText.split("\\.", -1);
                    String entier = parts[0].isEmpty() ? "0" : formatter.format(Long.parseLong(parts[0]));
                    String decimales = parts.length > 1 ? parts[1] : "";
                    formattedText = entier + "." + decimales;
                } else {
                    formattedText = formatter.format(Long.parseLong(cleanText));
                }

                // 4. Mettre à jour si le texte a changé sans casser l'index du curseur JavaFX
                if (!newText.equals(formattedText)) {
                    int oldCaret = textField.getCaretPosition();
                    int diffLength = formattedText.length() - newText.length();

                    // On applique la mise à jour et la reposition du curseur dans le cycle JavaFX suivant
                    Platform.runLater(() -> {
                        textField.setText(formattedText);
                        int newCaret = Math.max(0, Math.min(formattedText.length(), oldCaret + diffLength));
                        textField.positionCaret(newCaret);
                    });
                }

            } catch (NumberFormatException e) {
                // Rétablir l'ancien texte en cas d'erreur de formatage
                Platform.runLater(() -> textField.setText(oldText));
            }
        });

        return montantProperty;
    }


    public static DoubleProperty configurerChampMontantDynamiqueStatique(TextField textField) {
        DoubleProperty montantProperty = new SimpleDoubleProperty(0.0);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0.##", symbols);

        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                montantProperty.set(0.0);
                return;
            }

            // 1. Conserver uniquement chiffres et séparateur décimal
            String cleanText = newText.replaceAll("[^0-9.,]", "").replace(',', '.');

            if (cleanText.isEmpty()) {
                montantProperty.set(0.0);
                Platform.runLater(() -> textField.setText(""));
                return;
            }

            try {
                // 2. Extraire la valeur numérique pour les calculs
                double doubleValue = Double.parseDouble(cleanText);
                montantProperty.set(doubleValue);

                // 3. Formater la chaîne
                String formattedText;
                if (cleanText.contains(".")) {
                    String[] parts = cleanText.split("\\.", -1);
                    String entier = parts[0].isEmpty() ? "0" : formatter.format(Long.parseLong(parts[0]));
                    String decimales = parts.length > 1 ? parts[1] : "";
                    formattedText = entier + "." + decimales;
                } else {
                    formattedText = formatter.format(Long.parseLong(cleanText));
                }

                // 4. Mettre à jour si le texte a changé sans casser l'index du curseur JavaFX
                if (!newText.equals(formattedText)) {
                    int oldCaret = textField.getCaretPosition();
                    int diffLength = formattedText.length() - newText.length();

                    // On applique la mise à jour et la reposition du curseur dans le cycle JavaFX suivant
                    Platform.runLater(() -> {
                        textField.setText(formattedText);
                        int newCaret = Math.max(0, Math.min(formattedText.length(), oldCaret + diffLength));
                        textField.positionCaret(newCaret);
                    });
                }

            } catch (NumberFormatException e) {
                // Rétablir l'ancien texte en cas d'erreur de formatage
                Platform.runLater(() -> textField.setText(oldText));
            }
        });

        return montantProperty;
    }


    //****************** séparateur de millier pour une colonne

    public  <S> void appliquerFormatSeparateurMilliers(TableColumn<S, Double> colonne) {
        colonne.setCellFactory(tc -> new TableCell<S, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Formate avec un espace comme séparateur de milliers et 2 décimales maximum
                    //setText(String.format("%,.2f", item).replace(",", " ").replace(".", ","));
                    setText(String.format("%,.2f", item));
                }
            }
        });
    }

    public <S, T extends Number> void appliquerFormatSeparateurMilliersSpecial(TableColumn<S, T> colonne) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.FRANCE);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        colonne.setCellFactory(tc -> new TableCell<S, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item.doubleValue()));
                }
            }
        });
    }

    public <S> void appliquerFormatSeparateurMilliersInt(TableColumn<S, Integer> colonne) {
        colonne.setCellFactory(tc -> new TableCell<S, Integer>() { // Types explicites ici
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Formate avec un espace comme séparateur de milliers et 2 décimales
                    setText(String.format("%,d", item).replace("\u00A0", " ").replace(",", " "));
                    // setText(String.format("%,.2f", item).replace(",", " ").replace(".", ","));
                }
            }
        });
    }

    //numéro de tléphone
    public String formaterTelephoneMalgache(String telephoneBrut) {
        if (telephoneBrut == null) {
            return "";
        }

        // Supprime tout ce qui n'est pas un chiffre (espaces, tirets, etc.)
        String chiffres = telephoneBrut.replaceAll("\\D", " ");

        // Si le numéro fait bien 10 chiffres (ex: 0320000000)
        if (chiffres.length() == 10) {
            return String.format("%s %s %s %s",
                    chiffres.substring(0, 3),  // 032
                    chiffres.substring(3, 5),  // 00
                    chiffres.substring(5, 8),  // 000
                    chiffres.substring(8, 10)  // 00
            );
        }

        // Si le numéro a un indicatif international complet (ex: 261320000000 -> 12 chiffres)
        if (chiffres.length() == 12 && chiffres.startsWith("261")) {
            return String.format("+261 %s %s %s %s",
                    chiffres.substring(3, 5),  // 32
                    chiffres.substring(5, 7),  // 00
                    chiffres.substring(7, 10), // 000
                    chiffres.substring(10, 12) // 00
            );
        }

        // Si le format n'est pas standard, on retourne la valeur nettoyée par défaut
        return chiffres;
    }

    public  <S> void appliquerFormatTelephone(TableColumn<S, String> colonne) {
        colonne.setCellFactory(tc -> new TableCell<S, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Utilise votre méthode statique pour l'affichage visuel
                    setText(formaterTelephoneMalgache(item));
                }
            }
        });
    }

    /**
     * Applique un format JJ/MM/AAAA à n'importe quelle colonne de type TableColumn<?, LocalDate>
     */
    public <T> void formaterColonneDate(TableColumn<T, LocalDate> column) {
        column.setCellFactory(col -> new TableCell<T, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(FORMATTER.format(item));
                }
            }
        });
    }


    public  <S> void appliquerColorationStatutActif(TableColumn<S, String> colonne) {
        colonne.setCellFactory(tc -> new TableCell<S, String>() {
            // 🌟 Forcer explicitement l'utilisation du Label de JavaFX ici
            private final javafx.scene.control.Label badge = new javafx.scene.control.Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    badge.setText(item);

                    badge.setPrefWidth(100);
                    badge.setAlignment(javafx.geometry.Pos.CENTER);
                    badge.setPadding(new javafx.geometry.Insets(4, 8, 4, 8));

                    String styleDeBase = "-fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 10px; ";

                    switch (item) {
                        case "NON_PAYE":
                            badge.setStyle(styleDeBase + "-fx-background-color: #ff4d4d; -fx-text-fill: white;");
                            break;

                        case "PAYE":
                            badge.setStyle(styleDeBase + "-fx-background-color: #2ecc71; -fx-text-fill: white;");
                            break;

                        case "PARTIEL":
                            badge.setStyle(styleDeBase + "-fx-background-color: #e67e22; -fx-text-fill: white;");
                            break;

                        default:
                            badge.setStyle(styleDeBase + "-fx-background-color: #7f8c8d; -fx-text-fill: white;");
                            break;
                    }

                    setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(badge);
                }
            }
        });
    }

    public <S> void appliquerColorationTopVenteDashboard(TableColumn<S, String> colonne) {
        colonne.setCellFactory(tc-> new TableCell<S, String>(){
            private final javafx.scene.control.Label badge = new javafx.scene.control.Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                }else{
                    badge.setText(item);
                    badge.setPrefWidth(100);
                    badge.setAlignment(javafx.geometry.Pos.CENTER);
                    badge.setPadding(new javafx.geometry.Insets(4, 8, 4, 8));

                    String styleDeBase = "-fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 10px; ";

                    badge.setStyle(styleDeBase +  "-fx-background-color: #2ecc71; -fx-text-fill: white;");

                    setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(badge);
                }
            }
        });
    }

    //coloration status
    public  <S> void appliquerColorationStatut(TableColumn<S, String> colonne) {
        colonne.setCellFactory(tc -> new TableCell<S, String>() {
            // 🌟 Forcer explicitement l'utilisation du Label de JavaFX ici
            private final javafx.scene.control.Label badge = new javafx.scene.control.Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    badge.setText(item);

                    badge.setPrefWidth(100);
                    badge.setAlignment(javafx.geometry.Pos.CENTER);
                    badge.setPadding(new javafx.geometry.Insets(4, 8, 4, 8));

                    String styleDeBase = "-fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 10px; ";

                    switch (item) {
                        case "NON_PAYE":
                        case "DECONNECTE":
                            badge.setStyle(styleDeBase + "-fx-background-color: #ff4d4d; -fx-text-fill: white;");
                            break;

                        case "PAYE":
                        case"CONNECTE":
                        case"VALIDEE":
                            badge.setStyle(styleDeBase + "-fx-background-color: #2ecc71; -fx-text-fill: white;");
                            break;

                        case "PARTIEL":
                            badge.setStyle(styleDeBase + "-fx-background-color: #e67e22; -fx-text-fill: white;");
                            break;

                        default:
                            badge.setStyle(styleDeBase + "-fx-background-color: #7f8c8d; -fx-text-fill: white;");
                            break;
                    }

                    setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(badge);
                }
            }
        });
    }

    public <S> void appliquerColorationStatutConnexion(TableColumn<S, Boolean> colonne) {
        colonne.setCellFactory(tc -> new TableCell<S, Boolean>() {
            private final javafx.scene.control.Label badge = new javafx.scene.control.Label();

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    // Conversion du boolean en texte
                    String texte = item ? "CONNECTE" : "DECONNECTE";
                    badge.setText(texte);

                    badge.setPrefWidth(80);
                    badge.setAlignment(javafx.geometry.Pos.CENTER);
                    badge.setPadding(new javafx.geometry.Insets(4, 8, 4, 8));

                    String styleDeBase = "-fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 10px; ";

                    if (item) {
                        // Vert pour Actif (#2ecc71 ou votre couleur succès)
                        badge.setStyle(styleDeBase + "-fx-background-color: #2ecc71; -fx-text-fill: white;");
                    } else {
                        // Rouge pour Inactif (#ff4d4d)
                        badge.setStyle(styleDeBase + "-fx-background-color: #ff4d4d; -fx-text-fill: white;");
                    }

                    setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(badge);
                }
            }
        });
    }

    //coloration badge quantité si disponible
    public void appliquerBadgeStockDisponibilite(TableColumn<ProduitDTO, Number> colQuantite) {
        colQuantite.setCellFactory(tc -> new TableCell<ProduitDTO, Number>() {
            private final javafx.scene.control.Label badge = new javafx.scene.control.Label();

            @Override
            protected void updateItem(Number quantite, boolean empty) {
                super.updateItem(quantite, empty);

                if (empty || quantite == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Récupération de l'objet ProduitDTO de la ligne courante
                    ProduitDTO produit = getTableRow().getItem();

                    // Formatage propre du nombre (ex: 12,00)
                    String texteFormate = String.format("%,.2f", quantite.doubleValue())
                            .replace(" ", "\u00A0")
                            .replace(".", ",");

                    badge.setText(texteFormate);
                    badge.setPrefWidth(110);
                    badge.setMaxWidth(Double.MAX_VALUE);
                    badge.setAlignment(javafx.geometry.Pos.CENTER);
                    badge.setPadding(new javafx.geometry.Insets(4, 8, 4, 8));

                    String styleDeBase = "-fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 10px; ";

                    // Verification du booleen estDisponible du produit
                    // (Remplacez 'estDisponible()' par le nom exact du getter dans ProduitDTO, ex: isDisponible() ou disponible())
                    boolean estDisponible = produit.estDisponibleProduit();

                    if (estDisponible) {
                        // Vert si disponible en BDD
                        badge.setStyle(styleDeBase + "-fx-background-color: #2ecc71; -fx-text-fill: white;");
                    } else {
                        // Rouge si non disponible en BDD
                        badge.setStyle(styleDeBase + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    }

                    setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(badge);
                }
            }
        });
    }

    //************************************* masquer les composants
    /**
     * Masque complètement un composant JavaFX (visibilité + libération de l'espace dans le layout)
     */
    public static  void restreindreAcces(Node node, boolean estAutorise) {
        if (node != null) {
            node.setVisible(estAutorise);
            node.setManaged(estAutorise); // 🌟 Important : empêche le composant de laisser un espace vide
        }
    }

    /**
     * Masque un onglet spécifique d'un TabPane
     */
    public void restreindreOnglet(TabPane tabPane, Tab tab, boolean estAutorise) {
        if (tabPane != null && tab != null && !estAutorise) {
            tabPane.getTabs().remove(tab); // Retire proprement l'onglet si l'utilisateur n'est pas admin
        }
    }


    //***************************CONFIGURATION COMBOBOXE
    /**
     * Active le filtrage dynamique et fixe le nombre de lignes visibles pour une ComboBox.
     */
    public <T> void configureSearchableComboBox(ComboBox<T> comboBox, List<T> originalList, Function<T,
            String> nameExtractor, int visibleRowCount) {

        // 1. Limite le nombre de lignes affichées
        comboBox.setVisibleRowCount(visibleRowCount);

        // 2. Variable pour conserver les éléments filtrés
        ObservableList<T> filteredItems = FXCollections.observableArrayList(originalList);
        comboBox.setItems(filteredItems);

        // 3. Écouteur sur la saisie dans le champ d'édition
        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            // Empêche de filtrer si un élément vient d'être sélectionné dans la liste
            T selectedItem = comboBox.getSelectionModel().getSelectedItem();
            if (selectedItem != null && nameExtractor.apply(selectedItem).equals(newValue)) {
                return;
            }

            // Filtre la liste selon le texte saisi (insensible à la casse)
            String filter = (newValue == null) ? "" : newValue.trim().toLowerCase();

            List<T> matches = originalList.stream()
                    .filter(item -> item != null && nameExtractor.apply(item).toLowerCase().contains(filter))
                    .collect(Collectors.toList());

            filteredItems.setAll(matches);

            // Ouvre automatiquement la liste déroulante s'il y a du texte et des résultats
            if (!filter.isEmpty() && !matches.isEmpty() && !comboBox.isShowing()) {
                comboBox.show();
            }
        });
    }

    /**
     * Applique un design moderne AtlantaFX à une boîte de dialogue.
     *
     * @param alert L'instance de l'Alert à styliser
     * @param iconLiteral Code de l'icône Ikonli (ex: "fas-sign-out-alt", "fas-exclamation-triangle")
     * @param isDanger true si l'action principale est destructive (bouton rouge), false sinon (bouton accent)
     */

    public void styleModernAlert(Alert alert, String iconLiteral, boolean isDanger) {
        DialogPane dialogPane = alert.getDialogPane();

        // 1. Suppression de la barre de titre native
        alert.initStyle(StageStyle.TRANSPARENT);

        // 2. Attacher votre fichier CSS à l'Alert
        if (getClass().getResource("/vue/cssFile/style.css") != null) {
            String cssPath = getClass().getResource("/vue/cssFile/style.css").toExternalForm();
            if (!dialogPane.getStylesheets().contains(cssPath)) {
                dialogPane.getStylesheets().add(cssPath);
            }
        }

        // 3. Application des classes CSS AtlantaFX
        dialogPane.getStyleClass().add("elevated-2");

        // 4. Masquer le bouton fermer
        alert.setOnShown(e -> {
            Node closeBtn = dialogPane.lookup(".close-button");
            if (closeBtn != null) {
                closeBtn.setVisible(false);
                closeBtn.setManaged(false);
            }
        });

        // 5. Icône d'en-tête dynamique (SANS style inline problématique)
        if (iconLiteral != null && !iconLiteral.isBlank()) {
            FontIcon headerIcon = new FontIcon(iconLiteral);
            headerIcon.setIconSize(28);

            // SOLUTION 1 : Utilisation des classes d'icônes AtlantaFX
            headerIcon.getStyleClass().addAll("icon", isDanger ? "danger" : "accent");

            // SOLUTION 2 (Alternative ultra-fiable si le CSS ne passe pas) :
            // Color color = isDanger ? Color.web("#d32f2f") : Color.web("#0288d1");
            // headerIcon.setIconColor(color);

            alert.setGraphic(headerIcon);
        }

        // 6. Stylisation des boutons
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.getStyleClass().addAll("button", isDanger ? "danger" : "accent");
            okButton.setStyle("-fx-cursor: hand;");
        }

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.getStyleClass().addAll("button", "flat");
            cancelButton.setStyle("-fx-cursor: hand; -fx-underline: false;");
        }
    }


    //*************************ALERTE
    //confiramtion
    /**
     * Affiche une boîte de dialogue de confirmation stylisée avec AtlantaFX.
     *
     * @param titre Le titre de la fenêtre
     * @param entete Le texte d'en-tête
     * @param message Le message de confirmation
     * @param isDanger true si l'action est destructive (bouton principal rouge)
     * @return true si l'utilisateur a cliqué sur le bouton de confirmation, false sinon.
     * utilisation: if(afficherConfirmation(String titre, String entete, String message, boolean isDanger))
     */
    public boolean afficherConfirmation(String titre, String entete, String message, boolean isDanger) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titre);
        alert.setHeaderText(entete);
        alert.setContentText(message);

        // Applique votre style AtlantaFX
        styleModernAlert(alert, isDanger ? "fas-exclamation-triangle" : "fas-question-circle", isDanger);
        // 💡 AJOUT DU LOGO SUR LA BARRE DE TITRE DE L'ALERTE
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        // Personnalisation des boutons
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setText(isDanger ? "Oui, supprimer" : "Confirmer");
        }

        Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.setText("Annuler");
        }

        // Capture de la réponse
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    //ERREUR
    public void afficherErreur(String titre, String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(titre);
        alert.setContentText(message);

        styleModernAlert(alert, "fas-exclamation-triangle", true);

        // 💡 AJOUT DU LOGO SUR LA BARRE DE TITRE DE L'ALERTE
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        Button okBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn != null) okBtn.setText("Fermer");

        alert.showAndWait();
    }

    //AVERTISSEMENT
    public void afficherAvertissement(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(titre);
        alert.setContentText(message);

        styleModernAlert(alert, "fas-exclamation-circle", false);

        // 💡 AJOUT DU LOGO SUR LA BARRE DE TITRE DE L'ALERTE
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        Button okBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn != null) okBtn.setText("Compris");

        alert.showAndWait();
    }

    //INFORMATION
    public void afficherInformation(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(titre);
        alert.setContentText(message);

        styleModernAlert(alert, "fas-check-circle", false);

        // 💡 AJOUT DU LOGO SUR LA BARRE DE TITRE DE L'ALERTE
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        Button okSuccesBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okSuccesBtn != null) okSuccesBtn.setText("D'accord");

        alert.showAndWait();
    }

    public void appliquerLogoAlerte(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        var logoStream = getClass().getResourceAsStream("/images/logo.png");
        if (logoStream != null) {
            stage.getIcons().add(new Image(logoStream));
        }
    }

    //*************************************CHANGER CHEMEIN DATABASE
    public void changerDossierBdd(Stage window) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choisir l'emplacement de la base de données");
        File selectedDirectory = directoryChooser.showDialog(window);

        if (selectedDirectory != null) {
            String nouveauChemin = selectedDirectory.getAbsolutePath() + File.separator + "seravarotra.db";
            Preferences prefs = Preferences.userNodeForPackage(DepenseNakaApplication.class);
            prefs.put("DB_PATH", nouveauChemin);

            // Avertir l'utilisateur qu'il doit redémarrer l'application

            afficherInformation(
                    "Redémarrage requis",
                    "Le nouvel emplacement a été enregistré " +
                            "\n(" + nouveauChemin + ")." +
                            "\n Veuillez redémarrer l'application pour appliquer le changement."
            );
        }
    }

    //****************Eviter la saisie des lettre

    /**
     * Restreint la saisie d'un TextField aux nombres décimaux (double).
     * Accepte les chiffres et un seul point (ou virgule) décimal.
     */
    public void restrictToDouble(TextField textField) {
        // Regex : autorise uniquement les chiffres et un seul point/virgule
        Pattern doublePattern = Pattern.compile("\\d*([.,]?\\d*)?");

        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            // Remplacement automatique des virgules par des points pour la compatibilité Java (Double.parseDouble)
            if (change.getText().equals(",")) {
                change.setText(".");
                newText = change.getControlNewText();
            }

            if (doublePattern.matcher(newText).matches()) {
                return change; // Saisie acceptée
            }
            return null; // Saisie rejetée (bloque la touche ou le copier-coller)
        });

        textField.setTextFormatter(formatter);
    }

    /**
     * Restreint la saisie d'un TextField aux nombres entiers positifs (int / long).
     * Accepte uniquement les chiffres de 0 à 9.
     */
    public void restrictToInteger(TextField textField) {
        // Regex : autorise uniquement une suite de chiffres (ou champ vide pendant la saisie)
        Pattern integerPattern = Pattern.compile("\\d*");

        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            if (integerPattern.matcher(newText).matches()) {
                return change; // Saisie acceptée
            }
            return null; // Saisie rejetée (bloque la touche ou le copier-coller)
        });

        textField.setTextFormatter(formatter);
    }

    /**
     * Convertit la valeur d'une cellule Excel (String, Numérique, etc.) en String.
     */
    public String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                // Evite le format scientifique pour les numéros de téléphone (ex: 3.412E8 -> 03412...)
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }





}
