module org.example.onlinevoting {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires org.mariadb.jdbc;


    opens org.example.onlinevoting to javafx.fxml;
    opens org.example.onlinevoting.controller to javafx.fxml;
    opens org.example.onlinevoting.model to javafx.base;

    exports org.example.onlinevoting;
    exports org.example.onlinevoting.controller;
    exports org.example.onlinevoting.util;
}