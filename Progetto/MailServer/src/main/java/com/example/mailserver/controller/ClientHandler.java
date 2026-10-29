package com.example.mailserver.controller;

import com.example.mailserver.model.Model;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Controller controller;
    Model model;

    public ClientHandler(Socket clientSocket, Controller controller) {
        this.clientSocket = clientSocket;
        this.controller = controller;
        this.model = new Model();
    }

    @Override
    public void run() {
        try (
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                PrintWriter writer = new PrintWriter(output, true)
        ) {
            String message;
            message = reader.readLine();
            String[] parts = message.split("⊗");

            // prendiamo singole parti
            String email = parts[0];
            String command = parts[1];
            controller.logMessage("Messaggio ricevuto: " + message);
            switch (command) {
                case "CHECK_EMAIL":
                    String res = model.checkEmail(email);
                    if(res.equals("email non trovata")) {
                        writer.println("§§§COMMAND§§§NO");
                    }else if(res.equals("ERROR")) {
                        writer.println("§§§COMMAND§§§PROBLEM");
                    }else{
                        writer.println(res+"§§§COMMAND§§§OK");
                    }
                    break;

                case "DELETE_EMAIL":
                    writer.println(model.processEmail(email));
                    break;

                case "STATUS":
                    writer.println("OK");
                    break;

                case "SEND_EMAIL":
                    writer.println(model.sendEmail(email));
                    break;

                case "REPLY_EMAIL":
                    writer.println(model.replyEmail(email));
                    break;

                case "REPLY_ALL_EMAILS":
                    writer.println(model.replyAll(email));
                    break;

                case "FORWARD_EMAIL":
                    writer.println(model.Forward(email));
                    break;

                case "RELOAD_EMAIL":
                    writer.println(model.loadEmails(email));
                    break;

                case "RELOAD_EMAIL_EMPTY":
                    writer.println(model.loadEmailsEmpty(email));
                    break;

                default:
                    writer.println("Comando sconosciuto: " + command);
                    break;
            }
        } catch (IOException e) {
            controller.logMessage("Errore con il client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Errore con il client: " + e.getMessage());
            }
            controller.logMessage("Connessione chiusa: " + clientSocket.getInetAddress());
        }
    }
}
