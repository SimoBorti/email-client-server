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
import static com.example.mailclient.controller.ControllerMainPage.rimuoviPrimoRitornoACapo;

public class ControllerReply extends Reply {
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
    TextArea txtRicezione;
    @FXML
    TextArea txtCorpo;

    public ControllerReply() {}

    public void setModel(Model model) {
        this.model = model;
    }

    public void passData(String destinatario, String conversazione, String oggetto) {
        this.email = setPassData(destinatario, conversazione, oggetto, txtMittente, txtDestinatario, txtRicezione, txtOggetto, txtDate, model);
    }

    @FXML
    public void handleReply(ActionEvent event){
        String message = setFields(this.email, txtDestinatario, txtOggetto, txtRicezione, model, txtCorpo);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        switch(model.Reply(message)){
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
        }
    }

    static String setFields(Email email, TextField txtDestinatario, TextField txtOggetto, TextArea txtRicezione, Model model, TextArea txtCorpo) {
        int uniqueID = ControllerSend.generateID();
        email.setID(uniqueID);
        email.setTo(txtDestinatario.getText());
        email.setSubject(txtOggetto.getText());
        email.setBodyReceived(txtRicezione.getText() + "<<!NEWLINE!>>- " + model.getEmail()+ ": " + txtCorpo.getText().replaceAll("\\r\\n|\\r|\\n", "<<NEWLINE>>"));//togliamo già ora i "\n", altrimenti non riusciamo a distinguere quelli dentro il body da quelli che separano i diversi messaggi
        email.setBodySend(txtCorpo.getText());

        return String.format(
                "ID: %sFrom: %sTo: %sSubject: %sBodyReceived: %sBodySend: %sDate: %s",
                email.getID(), email.getFrom(), email.getTo(), email.getSubject(), email.getBodyReceived().replaceAll("\\r\\n|\\r|\\n", "<<!NEWLINE!>>"), email.getBodySend(), email.getDate()
        );
    }

    static Email setPassData(String destinatario, String conversazione, String oggetto, TextField txtMittente, TextField txtDestinatario, TextArea txtRicezione, TextField txtOggetto, TextField txtDate, Model model) {
        String substring = "Non stai partecipando a nessuna conversazione.";
        // creiamo la data attuale
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = currentDate.format(formatter);

        conversazione = conversazione.replace("<<NEWLINE>>", " ");
        conversazione = conversazione.replace("<<!NEWLINE!>>", "\n");

        txtMittente.setText(model.getEmail());
        txtDestinatario.setText(destinatario);
        //se c'è scritto solo "Non stai partecipando a nessuna conversazione." lo teniamo, altrimenti lo togliamo
        if (conversazione.contains(substring) && !(conversazione.equals(substring))) {
            conversazione = conversazione.replace(substring, "");
            conversazione = rimuoviPrimoRitornoACapo(conversazione);//togliamo la riga vuota che rimane dopo aver eliminato la substring
        }
        txtRicezione.setText(conversazione);
        txtOggetto.setText(oggetto);
        txtDate.setText(formattedDate);

        return new Email(0, model.getEmail(), "", "", "", "", currentDate);
    }
}