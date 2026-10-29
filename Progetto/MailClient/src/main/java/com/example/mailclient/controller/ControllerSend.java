package com.example.mailclient.controller;

import com.example.mailclient.Email;
import com.example.mailclient.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ControllerSend {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    private Model model;
    private Email email;

    @FXML
    TextField txtMittente;
    @FXML
    TextField txtDestinatario;
    @FXML
    TextField txtOggetto;
    @FXML
    TextField txtData;
    @FXML
    TextArea txtCorpo;

    public ControllerSend() {//serve un controller SENZA PARAMETRI, altrimenti java dà errore runtime (oppure un controller vuoto, però è meglio di no perché serve inizializzare model)
    }//Se avessi definito un costruttore con parametri senza includere un costruttore vuoto, Java non genererebbe automaticamente il costruttore predefinito. In questo caso, l’FXMLLoader non potrebbe creare l’istanza del controller automaticamente, portando a un errore.

    public void setModel(Model model) { //serve per passare i messaggi tra i controller
        this.model = model;
    }

    public void passData(){
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = currentDate.format(formatter);

        txtMittente.setText(model.getEmail());
        txtData.setText(formattedDate);

        //passData deve già settare i campi From e Date, perciò non serve il set e quindi vengono dichiarati final e resi immutabili
        this.email = new Email(0, model.getEmail(), "", "", "", "", currentDate);
    }

    @FXML
    private void handleInvia(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if(txtDestinatario.getText().isEmpty()){
            alert.setTitle("Errore");
            alert.setHeaderText("Campo obbligatorio");
            alert.setContentText("Il campo del destinatario non può essere vuoto. Per favore, compilalo.");
            alert.showAndWait();
            return; //altrimenti chiude anche la finestra di invio del messaggio
        }
        if(txtOggetto.getText().isEmpty()){
            alert.setTitle("Errore");
            alert.setHeaderText("Campo obbligatorio");
            alert.setContentText("Il campo dell'oggetto non può essere vuoto. Per favore, compilalo.");
            alert.showAndWait();
            return;
        }
        int uniqueID = generateID();
        this.email.setID(uniqueID);
        this.email.setTo(txtDestinatario.getText());
        this.email.setSubject(txtOggetto.getText());
        this.email.setBodyReceived("- " + model.getEmail() + ": " + txtCorpo.getText());
        this.email.setBodySend(txtCorpo.getText());

        String message = String.format(
                "ID: %sFrom: %sTo: %sSubject: %sBodyReceived: %sBodySend: %sDate: %s",
                email.getID(), email.getFrom(), email.getTo(), email.getSubject(), email.getBodyReceived(), email.getBodySend(), email.getDate()
        );

        if (ControllerForward.controllaSintassi(alert, txtDestinatario)) return;

        switch (model.Send(message)){
            case "nessun problema":
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close();
                break;
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
            case "destinatario non esiste":
                alert.setTitle("Errore");
                alert.setHeaderText("Destinatario inesistente");
                alert.setContentText("Il destinatario inserito non esiste.");
                alert.showAndWait();
                break;
        }
    }

    public static int generateID() {
        SecureRandom random = new SecureRandom();
        // numero casuale tra 10000 e 99999 perchè l'id ha 5 cifre
        return 10000 + random.nextInt(90000);
    }

    public static boolean emailValida(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return pattern.matcher(email).matches();
    }

    public static boolean hasDuplicates(String[] array) {
        return Arrays.stream(array).distinct().count() != array.length;
    }
}