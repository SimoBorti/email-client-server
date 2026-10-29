package com.example.mailclient.model;

import java.io.*;
import java.net.Socket;

public class Model {
    private String[] parts;
    private String email;

    public String[] getParts() {
        return parts;
    }
    public void setParts(String[] parts) {
        this.parts = parts;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String Verifica(String email) {
        try (Socket socket = new Socket("localhost", 12345); // porta e indirizzo del server
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String marcatore = "<<NEWLINE>>";

            String message = email + "⊗" + "CHECK_EMAIL";
            // inviamo email al server
            out.println(message);

            // riceviamo risposta dal server
            String response = in.readLine();
            if(response.equals("§§§COMMAND§§§PROBLEM")) {
                System.err.println("Errore nella risposta del server: risposta nulla o non valida.");
                return "ERROR";
            }
            if(response.equals("§§§COMMAND§§§NO")) {
                return "email non trovata";
            }

            //dividiamo i messaggi dal messaggio del server
            setParts(response.split("§§§COMMAND§§§"));

            //settiamo i messaggi
            setParts(parts[0].replace(marcatore, "\n").split("ID: "));

            return "nessun problema";
        }catch (IOException e) {
            return "ERROR";
        }
    }

    public String Cancella(String item) {
        try (Socket socket = new Socket("localhost", 12345); // porta e indirizzo del server
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String message = item + "Email: " + getEmail() + "⊗" + "DELETE_EMAIL";
            out.println(message);

            String response = in.readLine();
            if (response == null) {
                System.err.println("Errore nella risposta del server: risposta nulla o non valida.");
                return "NULL";
            } else {
                return response;
            }
        }catch (IOException e) {
            System.err.println("Errore nella cancellazione del messaggio: " + e.getMessage());
            return "ERROR";
        }
    }

    public boolean checkConnection() {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // invia il comando STATUS
            out.println("check⊗STATUS");//scriviamo check⊗STATUS perché deve rispettare la sintassi richiesta dal clienthandler
            String response = in.readLine();

            return response != null;
        } catch (IOException e) {
            System.err.println("Errore durante la verifica della connessione: " + e.getMessage());
            return false;
        }
    }

    public String Send(String email) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)){
            String marcatore = "<<NEWLINE>>";
            out.println(email.replaceAll("\\r\\n|\\r|\\n", marcatore) + "⊗" + "SEND_EMAIL");

            String response = in.readLine();
            if (response == null) {
                System.err.println("Errore nella risposta del server: risposta nulla o non valida.");
                return "NULL";
            }else{
                return response;
            }
        } catch (IOException e) {
            System.err.println("Errore nell'invio del messaggio: " + e.getMessage());
            return "ERROR";
        }
    }

    public String Ricarica(String emailAddress, String elemento){
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)){
            String marcatore = "<<NEWLINE>>";
            String message = emailAddress + "§" + elemento.replaceAll("\\r\\n|\\r|\\n", marcatore) + "⊗" + "RELOAD_EMAIL";
            out.println(message);
            return in.readLine();
        } catch (IOException e) {
            System.err.println("Errore nella ricarica dei messaggi: " + e.getMessage());
            return "ERROR";
        }
    }

    public String ricaricaVuota(String emailAddress){
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)){
            String message = emailAddress + "⊗" + "RELOAD_EMAIL_EMPTY";
            out.println(message);
            return in.readLine();
        }catch (IOException e) {
            System.err.println("Errore nella ricarica dei messaggi con listview vuota: " + e.getMessage());
            return "ERROR";
        }
    }

    public String Reply(String message){
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)){
            String marcatore = "<<NEWLINE>>";
            out.println(message.replaceAll("\\r\\n|\\r|\\n", marcatore) + "⊗REPLY_EMAIL");

            String response = in.readLine();
            if (response == null) {
                System.err.println("Errore nella risposta del server: risposta nulla o non valida.");
                return "NULL";
            } else {
                return response;
            }
        }catch (IOException e) {
            System.err.println("Errore nella risposta ai messaggi: " + e.getMessage());
            return "ERROR";
        }
    }

    public String replyAll(String message){
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)){
            String marcatore = "<<NEWLINE>>";
            out.println(message.replaceAll("\\r\\n|\\r|\\n", marcatore) + "⊗REPLY_ALL_EMAILS");

            String response = in.readLine();
            if (response == null) {
                System.err.println("Errore nella risposta del server: risposta nulla o non valida.");
                return "NULL";
            }else{
                return response;
            }
        }catch (IOException e) {
            System.err.println("Errore nella risposta ai messaggi (replyAll): " + e.getMessage());
            return "ERROR";
        }
    }


    public String Forward(String message){
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)){
            String marcatore = "<<NEWLINE>>";
            out.println(message.replaceAll("\\r\\n|\\r|\\n", marcatore) + "⊗FORWARD_EMAIL");

            String response = in.readLine();
            if (response == null) {
                System.err.println("Errore nella risposta del server: risposta nulla o non valida.");
                return "NULL";
            }else{
                return response;
            }
        }catch (IOException e) {
            System.err.println("Errore nell'inoltro del messaggio': " + e.getMessage());
            return "ERROR";
        }
    }
}