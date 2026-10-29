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
//importiamo i metodi statici da ControllerReply
import static com.example.mailclient.controller.ControllerReply.setFields;
import static com.example.mailclient.controller.ControllerReply.setPassData;

public class ControllerReplyAll extends Reply{
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

    public ControllerReplyAll() {}

    public void passData(String destinatario, String conversazione, String oggetto) {
        this.email = setPassData(destinatario, conversazione, oggetto, txtMittente, txtDestinatario, txtRicezione, txtOggetto, txtDate, model);
    }

    public void setModel(Model model){
        this.model = model;
    }

    @FXML
    public void handleReplyAll(ActionEvent event){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if(txtOggetto.getText().isEmpty()){
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("Campo obbligatorio");
            alert.setContentText("Il campo dell'oggetto non può essere vuoto. Per favore, compilalo.");
            alert.showAndWait();
            return; //altrimenti chiude anche la finestra di invio del messaggio
        }

        String message = setFields(this.email, txtDestinatario, txtOggetto, txtRicezione, model, txtCorpo);

        switch (model.replyAll(message)){
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
}
