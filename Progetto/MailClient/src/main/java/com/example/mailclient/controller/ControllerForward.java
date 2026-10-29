package com.example.mailclient.controller;

import com.example.mailclient.Email;
import com.example.mailclient.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static com.example.mailclient.controller.ControllerSend.emailValida;
import static com.example.mailclient.controller.ControllerSend.hasDuplicates;

public class ControllerForward {
    private Model model;
    private Email email;

    @FXML
    TextField txtMittente;
    @FXML
    TextField txtDestinatario;
    @FXML
    TextField txtOggetto;
    @FXML
    TextField txtDate;
    @FXML
    TextArea txtCorpo;

    public ControllerForward() {}

    public void setModel(Model model) {
        this.model = model;
    }

    public void passData(String corpo, String oggetto) {

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = currentDate.format(formatter);

        String marcatore = "<<NEWLINE>>";
        corpo = corpo.replace(marcatore, "\n");

        txtMittente.setText(model.getEmail());
        txtCorpo.setText(corpo);
        txtOggetto.setText(oggetto);
        txtDate.setText(formattedDate);

        this.email = new Email(0, model.getEmail(), "", "", "Non stai partecipando a nessuna conversazione.", "", currentDate);
    }

    @FXML
    private void handleForward(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if(txtDestinatario.getText().isEmpty()){
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("Campo obbligatorio");
            alert.setContentText("Il campo del destinatario non può essere vuoto. Per favore, compilalo.");
            alert.showAndWait();
            return; //altrimenti chiude anche la finestra di invio del messaggio
        }
        int uniqueID = ControllerSend.generateID();
        this.email.setID(uniqueID);
        this.email.setTo(txtDestinatario.getText());
        this.email.setSubject(txtOggetto.getText());
        this.email.setBodySend(txtCorpo.getText());

        String message = String.format(
                "ID: %sFrom: %sTo: %sSubject: %sBodyReceived: %sBodySend: %sDate: %s",
                email.getID(), email.getFrom(), email.getTo(), email.getSubject(), email.getBodyReceived(), email.getBodySend(), email.getDate()
        );

        if (controllaSintassi(alert, txtDestinatario)) return;

        switch (model.Forward(message)){
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

    static boolean controllaSintassi(Alert alert, TextField txtDestinatario) {
        String[] destinatari = txtDestinatario.getText().split(" ");
        if(hasDuplicates(destinatari)){
            alert.setTitle("Errore");
            alert.setHeaderText("Ripetizione Email");
            alert.setContentText("Hai inserito due volte (o più) lo stesso destinatario.");
            alert.showAndWait(); //mette in attesa gli altri processi
            return true;
        }

        boolean sintassi = false;
        for (String s : destinatari) {
            if (!emailValida(s)) {
                sintassi = true;
            }
        }
        if(sintassi){
            alert.setTitle("Errore");
            alert.setHeaderText("Sintassi Email");
            alert.setContentText("La/le email inserita/e non rispettano il formato valido.");
            alert.showAndWait();
            return true;
        }
        return false;
    }
}