package com.example.mailclient.controller;

import com.example.mailclient.model.Model;

public abstract class Reply {
    public Reply() {}

    abstract void passData(String destinatario, String conversazione, String oggetto);

    abstract void setModel(Model model);
}