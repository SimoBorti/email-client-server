module com.example.mailclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.compiler;
    requires org.jetbrains.annotations;
    requires java.desktop;


    opens com.example.mailclient to javafx.fxml;
    exports com.example.mailclient;
    exports com.example.mailclient.controller;
    opens com.example.mailclient.controller to javafx.fxml;
    //aggiunti da noi perchè altrimenti dà un warning su setModel:
    exports com.example.mailclient.model;
}