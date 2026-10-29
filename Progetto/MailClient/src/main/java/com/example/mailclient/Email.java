package com.example.mailclient;

import java.time.LocalDate;

public class Email {
    private int ID;
    private final String From;
    private String To;
    private String Subject;
    private String BodyReceived;
    private String BodySend;
    private final LocalDate date;

    public Email(int ID, String from, String to, String subject, String bodyReceived, String bodySend, LocalDate date) {
        this.ID = ID;
        this.From = from;
        this.To = to;
        this.Subject = subject;
        this.BodyReceived = bodyReceived;
        this.BodySend = bodySend;
        this.date = date;
    }

    public int getID() {
        return ID;
    }
    public String getFrom() {
        return From;
    }
    public String getTo() {
        return To;
    }
    public String getSubject() {
        return Subject;
    }
    public String getBodyReceived() {
        return BodyReceived;
    }
    public String getBodySend() {
        return BodySend;
    }
    public LocalDate getDate() {
        return date;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
    public void setTo(String to) {
        To = to;
    }
    public void setSubject(String subject) {
        Subject = subject;
    }
    public void setBodyReceived(String bodyReceived) {
        BodyReceived = bodyReceived;
    }
    public void setBodySend(String bodySend) {
        BodySend = bodySend;
    }
}