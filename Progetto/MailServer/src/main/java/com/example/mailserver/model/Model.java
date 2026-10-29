package com.example.mailserver.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Model {
    private static final String BASE_PATH = "src\\main\\resources"; //percorso del file con l'elenco delle email
    private final Lock lock = new ReentrantLock();

    private String isEmailPresent(String email) {
        String fileName = "src\\main\\resources\\dati.txt";
        lock.lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            List<String> emails = reader.lines().toList();
            if(emails.contains(email)) return "nessun problema";
            else return "email non trovata";
        } catch (IOException e) {
            System.err.println("Errore nel trovare l'email: " + e.getMessage());
            return "ERROR";
        } finally {
            lock.unlock();
        }
    }

    public String checkEmail(String email) {
        if (isEmailPresent(email).equals("nessun problema")) {
            return loadFirstEmails(email);
        }else{
            return isEmailPresent(email);
        }
    }

    private String loadFirstEmails(String email){
        String fileName = BASE_PATH + "\\" + email + ".txt";

        lock.lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine())!=null){
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            System.err.println("Errore nel caricare le prime email: "+e.getMessage());
            return "ERROR";
        } finally {
            lock.unlock();
        }
    }

    public String loadEmails(String message) {
        String[] messaggio = message.split("§");
        String fileName = BASE_PATH + "\\" + messaggio[0] + ".txt";
        messaggio[1] = "ID: " + messaggio[1];

        lock.lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) { //con il try-with-resources non serve chiudere il file, lo fa da solo
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.trim().equals(messaggio[1].trim())) {
                    while ((line = reader.readLine()) != null) {
                        result.append(line.trim());
                    }
                    break;
                }
            }
            return result.toString(); // ridà l'intero contenuto come stringa
        } catch (IOException e) {
            System.err.println("Errore nel caricare le email: "+e.getMessage());
            return "ERROR";
        }finally {
            lock.unlock();
        }
    }

    public String loadEmailsEmpty(String message) {
        String fileName = BASE_PATH + "\\" + message + ".txt";

        lock.lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) { //con il try-with-resources non serve chiudere il file, lo fa da solo
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            System.err.println("Errore nel caricare le email con listview vuota: "+e.getMessage());
            return "ERROR";
        } finally {
            lock.unlock();
        }
    }

    public String processEmail(String input) {
        String[] parts = input.split("ID:|From:|To:|Subject:|BodyReceived:|BodySend:|Date:|Email:");
        parts = getStrings(parts);

        String ID = parts[0].trim();
        String fileName = BASE_PATH + "\\" + parts[7].trim() + ".txt";
        String tempFilePath = fileName + ".tmp";
        File originalFile = new File(fileName);
        File tempFile = new File(tempFilePath);

        lock.lock();
        try {
            Path path = Paths.get(fileName);
            List<String> lines = Files.readAllLines(path);

            List<String> updatedLines = new ArrayList<>();
            //copiamo la lista senza quella da cancellare
            for(int i = 0; i < lines.size(); i++){
                if(lines.get(i).trim().contains(ID)){
                    i++;
                }else{
                    updatedLines.add(lines.get(i));
                }
            }

            try(FileWriter writer = new FileWriter(tempFilePath)){
                for(int i = 0; i < updatedLines.size(); i++){
                    if(i == updatedLines.size()-1){
                        writer.write(updatedLines.get(i));
                        break;
                    }
                    writer.write(updatedLines.get(i) + "\n");
                }
            } catch (IOException e) {
                System.err.println("Errore nello scrivere nel file temporaneo: " + e.getMessage());
                return "errore nel file";
            }

            //elimina file originale
            if (originalFile.delete()) {
                System.out.println("File originale eliminato.");
            } else {
                System.err.println("Errore durante l'eliminazione del file originale.");
                return "errore nel file";
            }

            // rinominiamo il file temporaneo con il nome del file originale
            if (tempFile.renameTo(originalFile)) {
                System.out.println("File temporaneo rinominato in file originale.");
            } else {
                System.err.println("Errore durante la rinomina del file temporaneo.");
                return "errore nel file";
            }
            return "nessun problema";
        } catch (IOException e) {
            System.err.println("Errore nel cancellare: " + e.getMessage());
            return "errore nel file";
        } finally {
            lock.unlock();
        }
    }

    public String replyEmail(String message) {
        String[] emailParts = message.split("ID:|From:|To:|Subject:|BodyReceived:|BodySend:|Date:");
        emailParts = getStrings(emailParts);
        String fileName = BASE_PATH + "\\" + emailParts[2].trim() + ".txt";
        if(writeOnFile(message, fileName))
            return "nessun problema";
        else
            return "errore nel file";
    }

    public String sendEmail(String message) {
        return sendAllEmails(message);
    }

    public String replyAll(String message) {
        return sendAllEmails(message);
    }

    public String Forward(String message) {
        return sendAllEmails(message);
    }

    private String sendAllEmails(String message) {
        String[] emailParts = message.split("ID:|From:|To:|Subject:|BodyReceived:|BodySend:|Date:");//i | sono degli OR e significano "ogni volta che incontri uno di questi caratteri dividi la stringa"
        emailParts = getStrings(emailParts);
        String[] destinatari = emailParts[2].split(" ");
        destinatari = getStrings(destinatari);
        String fileName = "src\\main\\resources\\dati.txt";
        List<String> lines = new ArrayList<>();

        lock.lock();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lines.add(linea);
            }
        } catch (IOException e) {
            System.err.println("Errore nell'aprire il file" + e.getMessage());
            return "errore nel file";
        }finally {
            lock.unlock();
        }

        //controlliamo che i destinatari esistano veramente (in un ciclo separato perché li dobbiamo controllare tutti prima di proseguire)
        for (String s : destinatari) {
            if (!lines.contains(s.trim())) {
                return "destinatario non esiste";
            }
        }

        for (String s : destinatari) {
            fileName = BASE_PATH + "\\" + s.trim() + ".txt";
            if (!writeOnFile(message, fileName)) {
                return "errore nel file";
            }
        }
        return "nessun problema";
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

    private boolean writeOnFile(String message, String fileName) {
        lock.lock();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.newLine();//aggiungiamo una nuova riga
            writer.write(message);//scriviamo il contenuto in fondo al file
            return true;
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura nel file " + fileName + ": " + e.getMessage());
            return false;
        }finally {
            lock.unlock();
        }
    }
}