package app.lab01;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(new PortsCloser()));
        System.setProperty("file.encoding", "UTF-8");
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("Com-порты");
        stage.setOnCloseRequest(this::closePorts);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }

    private void closePorts(WindowEvent event){
        PortsCloser.closePorts();
        System.exit(0);
    }
}