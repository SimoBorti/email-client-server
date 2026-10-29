package com.example.mailserver.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Controller {
    @FXML
    private TextArea logArea;

    private static final int PORT = 12345;
    private static final int THREAD_POOL_SIZE = 10;

    private ExecutorService threadPool;
    private ServerSocket serverSocket;

    @FXML
    private void initialize() {
        // ferma il server quando l'utente chiude la finestra
        Platform.runLater(() -> logArea.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
            logMessage("La finestra sta per chiudersi. Arresto del server...");
            stopServer();
            Platform.exit(); //chiude l'applicazione
        }));
    }

    @FXML
    private void startServer() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            logMessage("Il server è già avviato.");
            return;
        }

        threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        //avviamo il server in un nuovo thread
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                logMessage("Server avviato sulla porta " + PORT + ".");

                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        logMessage("\nConnessione accettata da: " + clientSocket.getInetAddress());
                        threadPool.execute(new ClientHandler(clientSocket, this));
                    } catch (SocketException e) { //entra qui perchè serverSocket.accept() lancia eccezione quando chiudo il server
                        if (serverSocket.isClosed()) {
                            logMessage("Il server è stato chiuso manualmente.");
                            break; //esce dal ciclo se il server viene chiuso
                        }
                        throw e; //se no lancia l'eccezione
                    }
                }
            } catch (IOException e) {
                System.err.println("Errore nel server: " + e.getMessage());
            } finally {
                if (threadPool != null && !threadPool.isShutdown()) {
                    threadPool.shutdown();
                }
            }
        }).start();
    }

    @FXML
    private void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // chiude il server socket
            } else {
                logMessage("Il server è già spento.");
                return;
            }
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdown(); // inizia a fermare thread attivi
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {  // attende massimo 60 secondi
                    threadPool.shutdownNow();  //forza la terminazione se i thread non finiscono in tempo ciò che stavano facendo
                }// così siamo sicuri che tutti i thread finiscono quando chiudiamo il server
            }
            logMessage("Server arrestato correttamente.");
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore nel server: " + e.getMessage());
            logMessage("Errore durante l'arresto del server: " + e.getMessage());
        }
    }

    // aggiorna l'interfaccia del server
    public void logMessage(String message) {
        //salviamo la posizione del cursore (caret) per far sì che a ogni messaggio rimanga la stessa e la textarea non scorra ogni volta
        Platform.runLater(() -> {
            int caretPosition = logArea.getCaretPosition();
            logArea.appendText(message + "\n");
            logArea.positionCaret(caretPosition);
        });
    }
}