module app.lab01 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fazecast.jSerialComm;


    opens app.lab01 to javafx.fxml;
    exports app.lab01;
    exports app.lab01.data;
    opens app.lab01.data to javafx.fxml;
    exports app.lab01.controllers;
    opens app.lab01.controllers to javafx.fxml;
}