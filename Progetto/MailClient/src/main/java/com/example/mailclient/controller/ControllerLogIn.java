package com.example.mailclient.controller;

import com.example.mailclient.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.*;
import java.util.regex.Pattern;

public class ControllerLogIn {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    @FXML
    private TextField txtInserisci;
    @FXML
    private Label lblVerifica;

    Model model = new Model();

    private static boolean emailValida(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return pattern.matcher(email).matches();
    }

    @FXML
    private void handleInserisci(ActionEvent event) {
        String email = txtInserisci.getText().trim();
        model.setEmail(email);

        if (email.isEmpty()) {
            lblVerifica.setVisible(true);
            lblVerifica.setText("Inserisci un indirizzo email.");
            return;
        }

        if(emailValida(email)) {
            lblVerifica.setVisible(true);
            switch(model.Verifica(email)) {
                case "nessun problema":
                    lblVerifica.setText("Accesso riuscito!");
                    break;
                case "email non trovata":
                    lblVerifica.setText("L'email inserita non è presente nel database.");
                    return; //così non continua a eseguire il codice successivo allo switch-case
                case "ERROR":
                    lblVerifica.setText("ATTENZIONE: Non sei connesso/a al server!");
                    return;
            }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mailclient/MainPage.fxml"));
                Parent root = loader.load();

                //prendiamo il controller della main page
                ControllerMainPage controllerMainPage = loader.getController();

                // passiamo i dati alla main page
                controllerMainPage.setModel(model); //serve per passare i messaggi tra i controller
                controllerMainPage.PassData();

                //creiamo la finestra
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle(email);
                stage.setResizable(false);
                stage.show();

                //chiudiamo la finestra corrente
                ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
            } catch (IOException e) {
                System.err.println("Errore nel caricare la main page: " + e.getMessage());
            }
        }else{
            lblVerifica.setVisible(true);
            lblVerifica.setText("L'email inserita non rispetta la sintassi corretta.");
        }
    }
}