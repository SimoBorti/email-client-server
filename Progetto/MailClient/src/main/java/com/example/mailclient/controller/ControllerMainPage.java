package com.example.mailclient.controller;

import com.example.mailclient.model.Model;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.*;

public class ControllerMainPage {
    Stage emailStage;
    Stage stageScrivi;
    Stage stageInoltra;
    Stage stageReply;

    private Model model;

    @FXML
    private ListView<String> lstLista;
    @FXML
    private Label lblConnessioneOn;
    @FXML
    private Label lblConnessioneOff;
    @FXML
    private Label lblNotifica;
    @FXML
    private Label lblSeleziona;
    @FXML
    private Label lblServer;

    private ObservableList<String> lista;

    public ControllerMainPage() { // serve un controller SENZA PARAMETRI, altrimenti java dà errore runtime (oppure un controller vuoto, però è meglio di no perché serve inizializzare model)
        this.model = new Model();
    }  // Se avessimo definito un costruttore con parametri senza includere un costruttore vuoto, Java non genererebbe automaticamente il costruttore predefinito. Così fxmlloader non potrebbe creare il controller automaticamente creando un errore.

    public void setModel(Model model) { //serve per passare i messaggi tra i controller
        this.model = model;
    }

    public void PassData() {
        String[] output = model.getParts();
        String[] messages = getStrings(output);

        if (model == null) {
            System.err.println("Modello non inizializzato");
            return;
        }

        //controlliamo se messages è vuoto
        if (messages.length == 0) {
            ObservableList<String> observableList = FXCollections.observableArrayList("Nessuna e-mail trovata.");
            lstLista.setItems(observableList);
            lstLista.setOnMouseClicked(event -> handleDoubleClick(event, lstLista));
            this.lista = lstLista.getItems(); // conserviamo un riferimento alla lista
            return;  //usciamo dal metodo se non ci sono dati
        }

        for(int i = 0; i < messages.length; i++) {
            messages[i] = "ID: " + messages[i];
            messages[i] = messages[i].replaceAll("\\r\\n|\\r|\\n", "<<NEWLINE>>");
        }
        ObservableList<String> observableList = FXCollections.observableArrayList(messages);
        lstLista.setItems(observableList);
        lstLista.setOnMouseClicked(event -> handleDoubleClick(event, lstLista));
        this.lista = lstLista.getItems();
    }

    @FXML
    public void initialize() {
        reloadEmail();
    }

    private void handleDoubleClick(MouseEvent event, ListView<String> listView) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
             //otteniamo l'elemento selezionato
            String selectedEmail = listView.getSelectionModel().getSelectedItem();

            if (selectedEmail != null) {
                //apriamo una nuova finestra per mostrare il contenuto dell'email
                openEmailWindow(selectedEmail);
            }
        }
    }

    private void openEmailWindow(String emailContent) {
        //controlla che la finestra non sia già aperta e nel caso impedisci di aprirne un'altra
        if(emailStage != null && emailStage.isShowing())
            return;

        //crea una nuova finestra
        emailStage = new Stage();
        emailStage.setTitle("Dettagli Email");
        String marcatore = "<<NEWLINE>>";
        String marcatore2 = "<<!NEWLINE!>>";

        //separiamo il contenuto dell'email (ignoriamo l'ID)
        String[] emailParts = emailContent.split("From:|To:|Subject:|BodyReceived:|BodySend:|Date:");

        //estraiamo i campi
        String from = emailParts[1].trim();
        String to = emailParts[2].trim();
        String subject = emailParts[3].trim();
        String body2;
        if(emailParts[4].trim().isEmpty()) {
            body2 = "Non stai partecipando a nessuna conversazione.";
        }else{
            body2 = emailParts[4].replace(marcatore, " ").trim();
            body2 = body2.replace(marcatore2, "\n");
        }
        String body = emailParts[5].replace(marcatore, "\n").trim();
        String date = emailParts[6].trim();

        //se c'è scritto solo "Non stai partecipando a nessuna conversazione." lo teniamo, altrimenti lo togliamo
        String substring = "Non stai partecipando a nessuna conversazione.";
        if (body2.contains(substring) && !(body2.equals(substring))) {
            body2 = body2.replace(substring, "");
            body2 = rimuoviPrimoRitornoACapo(body2);
        }

        //creiamo le etichette per il mittente e il destinatario e l'oggetto
        Label fromLabel = new Label("Mittente:");
        Label toLabel = new Label("Destinatari/o:");
        Label subjectLabel = new Label("Oggetto:");
        Label bodyLabel = new Label("Corpo:");
        Label messageLabel = new Label("Conversazione precedente:"); // nuova label per il messaggio
        Label dateLabel = new Label("Data di invio:");

        // creiamo i text field e textarea per visualizzare i dettagli
        TextField fromField = new TextField(from);
        fromField.setEditable(false); //rendiamo il campo non modificabile

        TextField toField = new TextField(to);
        toField.setEditable(false);

        TextField subjectField = new TextField(subject);
        subjectField.setEditable(false);

        TextField dateField = new TextField(date);
        dateField.setEditable(false);

        TextArea bodyArea = new TextArea(body);
        bodyArea.setEditable(false);
        bodyArea.setWrapText(true); //aggiunge ritorno a capo automatico

        TextArea bodyArea2 = new TextArea(body2);
        bodyArea2.setEditable(false);
        bodyArea2.setWrapText(true);

        //si crea un layout a griglia
        GridPane emailLayout = new GridPane();
        emailLayout.setVgap(10);
        emailLayout.setHgap(10);

        // si aggiungono gli elementi alla griglia
        emailLayout.add(fromLabel, 0, 0);
        emailLayout.add(fromField, 1, 0);
        emailLayout.add(toLabel, 0, 1);
        emailLayout.add(toField, 1, 1);
        emailLayout.add(subjectLabel, 0, 2);
        emailLayout.add(subjectField, 1, 2);
        emailLayout.add(dateLabel, 0, 3);
        emailLayout.add(dateField, 1, 3);
        emailLayout.add(messageLabel, 0,4);
        emailLayout.add(bodyArea2, 1, 4);
        emailLayout.add(bodyLabel, 0, 5);
        emailLayout.add(bodyArea, 1, 5);

        //crea la scena e la impostiamo sulla finestra
        Scene emailScene = new Scene(emailLayout, 700, 550);
        emailStage.setScene(emailScene);
        emailStage.setResizable(false);

        //mostra la finestra
        emailStage.show();
    }

    @FXML
    private void handleScrivi(){
        if(stageScrivi != null && stageScrivi.isShowing())
            return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mailclient/Send.fxml"));
            Parent root = loader.load();

            //ottiene il controller di Send.fxml
            ControllerSend controllerSend = loader.getController();

            // passa il modello al controller Send
            controllerSend.setModel(model);
            controllerSend.passData();

             //creiamo e mostriamo la nuova finestra
            stageScrivi = new Stage();
            stageScrivi.setScene(new Scene(root));
            stageScrivi.setTitle("Invia");

            stageScrivi.setResizable(false);
            stageScrivi.show();
        } catch (IOException e) {
            System.err.println("Errore nel caricare la pagina di scrittura: " + e.getMessage());
        }
    }

    @FXML
    private void handleRispondi() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mailclient/Reply.fxml"));
            Parent root = loader.load();

            ControllerReply controllerReply = loader.getController();
            String title = "Rispondi";

            //se l'utente non ha selezionato nulla nella listview e preme il bottone, glielo comunica
            if(lstLista.getSelectionModel().getSelectedItem()==null) {
                showNotification(lblSeleziona, 2);
                return;
            }
            String[] message = (lstLista.getSelectionModel().getSelectedItem()).split("ID:|From:|To:|Subject:|BodyReceived:|BodySend:|Date:");
            message = getStrings(message);
            String destinatari = message[1].trim();
            loadReply(controllerReply, title, root, message, destinatari);
        } catch (IOException e) {
            System.err.println("Errore nel caricare la pagina di risposta: " + e.getMessage());
        }
    }

    @FXML
    private void handleRispondiATutti() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mailclient/ReplyAll.fxml"));
            Parent root = loader.load();

            ControllerReplyAll controllerReplyAll = loader.getController();
            String title = "Rispondi a tutti";

            if(lstLista.getSelectionModel().getSelectedItem()==null) {
                showNotification(lblSeleziona, 2);
                return;
            }
            String[] message = (lstLista.getSelectionModel().getSelectedItem()).split("ID:|From:|To:|Subject:|BodyReceived:|BodySend:|Date:");
            message = getStrings(message);
            String stringaModificata = eliminaDuplicati(message);
            if(stringaModificata.equals("uguali")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Attenzione");
                alert.setHeaderText("Unico destinatario/a");
                alert.setContentText("Tu sei l'unico/a destinatario/a di questo messaggio.");
                alert.showAndWait();
                return;
            }
            loadReply(controllerReplyAll, title, root, message, stringaModificata);
        } catch (IOException e) {
            System.err.println("Errore nel caricare la pagina di risposta (replyAll): " + e.getMessage());
        }
    }

    @FXML
    private void handleInoltra(){
        if(stageInoltra != null && stageInoltra.isShowing())
            return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mailclient/Forward.fxml"));
            Parent root = loader.load();

            ControllerForward controllerForward = loader.getController();

            if(lstLista.getSelectionModel().getSelectedItem()==null) {
                showNotification(lblSeleziona, 2);
                return;
            }
            String[] message = (lstLista.getSelectionModel().getSelectedItem()).split("ID:|From:|To:|Subject:|BodyReceived:|BodySend:|Date:");
            message = getStrings(message);
            String corpo = message[5].trim();
            String oggetto = message[3].trim();

            controllerForward.setModel(model);
            controllerForward.passData(corpo, oggetto);

            stageInoltra = new Stage();
            stageInoltra.setScene(new Scene(root));
            stageInoltra.setTitle("Inoltra");

            stageInoltra.setResizable(false);
            stageInoltra.show();
        } catch (IOException e) {
            System.err.println("Errore nel caricare la pagina di inoltro: " + e.getMessage());
        }
    }

    private void loadReply(Reply controller, String title, Parent root, String[] message, String destinatari) {
        if(stageReply != null && stageReply.isShowing())
            return;
        String conversazione = message[4].trim();
        String oggetto = message[3].trim();

        controller.setModel(model);
        controller.passData(destinatari, conversazione, oggetto);

        stageReply = new Stage();
        stageReply.setScene(new Scene(root));
        stageReply.setTitle(title);

        stageReply.setResizable(false);
        stageReply.show();
    }

    private @NotNull String eliminaDuplicati(String[] message) {
        //se un utente fa rispondi a tutti in una mail in cui è l'unico destinatario, glielo impediamo
        String destinatari = message[1].trim()+" "+ message[2].trim();
        if(message[1].trim().equals(message[2].trim())) {
            return "uguali";
        }

        //eliminiamo l'utente stesso nei destinatari così non si auto-risponde
        String destinatari_finali = destinatari.replace(model.getEmail(), "").trim();
        StringBuilder stringaModificata = getStringaModificata(destinatari_finali);
        return stringaModificata.toString();
    }

    private static @NotNull StringBuilder getStringaModificata(String destinatari_finali) {
        String[] parole = destinatari_finali.split("\\s+"); //divide la stringa nei singoli indirizzi

        LinkedHashSet<String> paroleUniche = new LinkedHashSet<>();//non ammette duplicati
        StringBuilder stringaModificata = new StringBuilder();

        //elimina duplicati nell'array parole
        for (String parola : parole) {
            if (paroleUniche.add(parola)) { //aggiunge solo se non è già presente
                if (!stringaModificata.isEmpty()) {
                    stringaModificata.append(" ");
                }
                stringaModificata.append(parola);
            }
        }
        return stringaModificata;
    }

    @FXML
    private void handleCancella() {
        String selectedItem = lstLista.getSelectionModel().getSelectedItem();
        if(lstLista.getSelectionModel().getSelectedItem()==null) {
            showNotification(lblSeleziona, 2);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        switch (model.Cancella(selectedItem)){
            case "nessun problema":
                lista.remove(selectedItem);//rimuovere l'elemento dalla lista usando la referenza creata nel metodo passData
                break;                     // perché non sono stringhe ma oggetti observableList
            case "NULL":
                alert.setTitle("Errore");
                alert.setHeaderText("Risposta del server");
                alert.setContentText("C'è stato un errore nella risposta del server.");
                alert.showAndWait();
                break;
            case "ERROR":
                alert.setTitle("Errore");
                alert.setHeaderText("Invio del messaggio");
                alert.setContentText("C'è stato un errore di comunicazione con il server.");
                alert.showAndWait();
                break;
            case "errore nel file":
                alert.setTitle("Errore");
                alert.setHeaderText("Lettura database");
                alert.setContentText("C'è stato un errore nel leggere i dati dal database.");
                alert.showAndWait();
                break;
        }
    }

    @FXML
    private void handleCheckConnection() {
        if(model.checkConnection()){
            showNotification(lblConnessioneOn,2);
        }
        else{
            showNotification(lblConnessioneOff,2);
        }
    }

    private void reloadEmail() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            ObservableList<String> currentItems = lstLista.getItems();
            String allItemsAsString = String.join("ID: ", currentItems);
            String[] items = allItemsAsString.split("ID: ");
            String lastElement = items[items.length - 1];

            if(lastElement==null || lastElement.isEmpty()){
                currentItems.addAll("Nessuna e-mail trovata.");
            }else if(lastElement.equals("Nessuna e-mail trovata.")){
                String messages = model.ricaricaVuota(model.getEmail());
                if(messages.equals("ERROR")){
                    lblServer.setVisible(true);
                    return;
                }
                lblServer.setVisible(false);
                List<String> newEmails = parseEmails(messages);
                if (!newEmails.isEmpty()) {
                    showNotification(lblNotifica, 3);
                    currentItems.clear();
                    currentItems.addAll(newEmails);
                }
            }else {
                lblServer.setVisible(false);

                //ottieni messaggi dal server
                String messages = model.Ricarica(model.getEmail(), lastElement);
                if(messages.equals("ERROR")){
                    lblServer.setVisible(true);
                    return;
                }

                List<String> newEmails = parseEmails(messages);
                if (!newEmails.isEmpty()) {
                    showNotification(lblNotifica, 3);
                }
                //aggiorna lista
                currentItems.addAll(newEmails);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void showNotification(Label label, int duration) {
        //Mostra  label
        label.setVisible(true);

        //crea pausa a tempo
        PauseTransition pause = new PauseTransition(Duration.seconds(duration));
        pause.setOnFinished(event -> label.setVisible(false)); // nasconde label dopo il tempo segnato
        pause.play(); // avvia
    }

    private List<String> parseEmails(String messages) {
        // dividiamo le email usando l'id come divisore di ogni messaggio
        String[] emailArray = messages.split("ID: ");
        emailArray = getStrings(emailArray);

        List<String> parsedEmails = new ArrayList<>();

        for (String email : emailArray) {
            if (!email.isBlank()) {
                parsedEmails.add("ID: " + email.trim()); //ri-aggiungiamo "ID:" per il formato giusto
            }
        }
        return parsedEmails;
    }

    //rimuoviamo il ritorno a capo che rimane dopo aver eliminato "Non stai partecipando a nessuna conversazione."
    static String rimuoviPrimoRitornoACapo(String str) {
        int index = str.indexOf('\n');

        if (index != -1) {
            return str.substring(0, index) + str.substring(index + 1);
        }
        return str;
    }

    //metodo per rimuovere il primo elemento vuoto nell'array di stringhe
    private String[] getStrings(String[] emailParts) {
        boolean firstEmptyRemoved = false;
        List<String> result = new ArrayList<>();

        for (String part : emailParts) {
            if (!part.isBlank()) {
                result.add(part); //teniamo gli elementi non vuoti
            } else if (!firstEmptyRemoved) {
                firstEmptyRemoved = true; //saltiamo il primo elemento vuoto
            } else {
                result.add(part); //teniamo gli altri elementi vuoti
            }
        }

        return result.toArray(new String[0]);
    }
}