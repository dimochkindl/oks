package app.lab01.controllers;

import app.lab01.controllers.PortController;
import app.lab01.data.Packet;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public class ViewController {
    static int n = 14;
    private PortController portController;
    private boolean initializing = true;
    private String currentPortName;
    @FXML
    private ChoiceBox stopBitsNumber, portChoice;
    private StringBuilder message = new StringBuilder();
    @FXML
    private TextField getBytesArea;
    @FXML
    private TextFlow stuffStructure;
    @FXML
    private TextArea inputWindow, outputWindow;
    private int typedSymbols;
    private String inputData;

    private String nextData;

    public ViewController() {

    }

    @FXML
    public void handlePortChoice(ActionEvent actionEvent) {
        if (!initializing) {
            Platform.runLater(() -> {
                portChoice.setValue(currentPortName);
            });
            System.out.println(currentPortName);
        }
    }

    public void checkText() {
    }

    @FXML
    public void initialize() {
        typedSymbols = 0;
        inputData = "";
        nextData = "";

        portController = new PortController();
        portController.setTextArea(outputWindow);
        portController.setGetBytesNumberField(getBytesArea);
        stopBitsNumber.setItems(FXCollections.observableArrayList(1.0, 1.5, 2.0));
        stopBitsNumber.setValue(1);
        portController.setStopBits(1.0);

        ObservableList<String> observableSortedNames = FXCollections.observableArrayList(portController.getComPortNames());
        try {
            if (!observableSortedNames.isEmpty()) {
                portChoice.setItems(observableSortedNames);
                portChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    currentPortName = newValue.toString();
                    if (oldValue != null) {
                        System.out.println("port closed: " + portController.closePreviousPort(oldValue.toString()) + oldValue);
                    }
                    portController.setCurrentPort(newValue.toString());
                    if (!portController.openCurrentPort()) {
                        currentPortName = portController.changePort(currentPortName);
                        portController.openCurrentPort();
                        newValue = currentPortName;
                        portController.setCurrentPort(currentPortName);
                    }
                    portController.setDefaultParameters();

                });
                Platform.runLater(() -> {
                    if (portController.checkForOpenPort(observableSortedNames.get(0))) {
                        portChoice.setValue(observableSortedNames.get(1));
                    }
                    portChoice.setValue(observableSortedNames.get(0));
                });
            } else {
                throw new Exception("Empty observable list");
            }
        } catch (Exception e) {
            showErrorAlert("Произошла ошибка", "Ошибка деления на ноль", e.getMessage());
        }

        addKeyEventHandler();
        initializing = false;
    }

    private void showErrorAlert(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private void addKeyEventHandler() {
        KeyCombination ctrlA = new KeyCodeCombination(
                KeyCode.A, KeyCombination.CONTROL_DOWN);
        KeyCombination ctrlV = new KeyCodeCombination(
                KeyCode.V, KeyCombination.CONTROL_DOWN);
        KeyCombination ctrlZ = new KeyCodeCombination(
                KeyCode.Z, KeyCombination.CONTROL_DOWN);
        inputWindow.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.RIGHT ||
                    event.getCode() == KeyCode.LEFT ||
                    event.getCode() == KeyCode.BACK_SPACE ||
                    event.getCode() == KeyCode.HOME ||
                    event.getCode() == KeyCode.DELETE ||
                    event.getCode() == KeyCode.UP ||
                    ctrlA.match(event) ||
                    ctrlV.match(event) ||
                    ctrlZ.match(event)
            ) {
                event.consume();
            }
        });

        inputWindow.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            inputWindow.positionCaret(inputWindow.getText()
                    .length());
            event.consume();
        });

        inputWindow.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > oldValue.length()) {
                message.append(newValue.charAt(newValue.length() - 1));
                if (message.length() == 14 - nextData.length()) {

                    stuffStructure.getChildren().remove(0, stuffStructure.getChildren().size());

                    String replacedData = stuff_2(message.toString());
                    replacedData = nextData + replacedData;
                    nextData = "";

                    if (replacedData.length() >= 14) {
                        char char13 = replacedData.charAt(12);
                        char char14 = replacedData.charAt(13);

                        if ((char13 == '&' && (char14 == 'E'))) {
                            nextData = replacedData.substring(12);
                            System.out.println(nextData);
                            StringBuilder modifiedInputText = new StringBuilder(replacedData);
                            modifiedInputText.insert(12, "\0\0");
                            System.out.println("modified: " + modifiedInputText);

                            String result = modifiedInputText.toString();
                            replacedData = result.substring(0, 14);
                            System.out.println("nextData:  " + nextData);
                        } else if (replacedData.length() > 14) {
                            nextData = replacedData.substring(15);
                            replacedData = replacedData.substring(0, 14);
                            System.out.println("nextData:  " + nextData);
                        }
                    }

                    StringBuilder binaryString = new StringBuilder();

                    for (char character : replacedData.toCharArray()) {
                        String binaryChar = String.format("%8s", Integer.toBinaryString(character & 0xFF)).replace(' ', '0');
                        binaryString.append(binaryChar);
                    }

                    binaryString.append("00000000");

                    int[] polynomialCoefficients = {1, 0, 0, 0, 0, 0, 1,  1}; // Коэффициенты полинома 0x65

                    StringBuilder polinom = new StringBuilder();

                    for (int coefficient : polynomialCoefficients) {
                        // Преобразование коэффициента в его двоичное представление (1 бит)
                        String binaryCoefficient = Integer.toBinaryString(coefficient);
                        polinom.append(binaryCoefficient);
                    }

                    String quotient = division(binaryString.toString(), polinom.toString());

                    while (quotient.length() < 8) {
                        quotient = "0" + quotient;
                    }

                    byte resultByte = Byte.parseByte(quotient, 2);


                    byte[] bytes = replacedData.getBytes();
                    Packet packet = new Packet(currentPortName, bytes, 0, resultByte);

                    int collisionCounter = portController.sendBytes(packet.toByte());


                    markStuffedBytesAndPrintFrame(packet.toString(), resultByte, collisionCounter);
                    message = new StringBuilder();
                }
            }
        });
    }

    private String stuff_2(String str) {
        String str2 = str;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str2.length(); i++) {
            char currentChar = str2.charAt(i);
            if (i + 1 < str2.length()) {
                if (currentChar == '&' && str2.charAt(i + 1) == 'E') {
                    result.append("&E1");
                    i++;
                } else if (currentChar == '#' && i + 1 < str2.length() && str2.charAt(i + 1) == 'n') {
                    result.append("&E2");
                    i++;
                } else {
                    result.append(currentChar);
                }
            } else {
                result.append(currentChar);
            }

        }
        return result.toString();
    }

    public void markStuffedBytesAndPrintFrame(String stuffedFrame, byte fcs, int collisionCounter) {

        String[] split = stuffedFrame.substring(0, 18).split("");
        List<Text> list = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            Text text = new Text();
            if( i+2 <  split.length) {
                if (split[i].equals("&") && split[i + 1].equals("E") && split[i + 2].equals("2")) {
                    text.setText("&E2");
                    text.setFill(Color.GREEN);
                    i += 2;
                } else if (split[i].equals("&") && split[i + 1].equals("E") && split[i + 2].equals("1")) {
                    text.setText("&E1");
                    text.setFill(Color.GREEN);
                    i += 2;
                }else if (split[i].equals("\n")) {
                    text.setText("\\n");
                } else {
                    text.setText(split[i]);
                }
            }else if (split[i].equals("\n")) {
                text.setText("\\n");
            } else {
                text.setText(split[i]);
            }
            list.add(text);
        }
        list.add(new Text(" "));
        list.add(new Text(Integer.toHexString(fcs).toUpperCase()));
        list.add(new Text("h"));

        list.add(new Text(" "));
        for (int i = 0; i < collisionCounter; i++) {
            list.add(new Text("c"));
        }

        stuffStructure.getChildren().addAll(list);
    }

    public String xor(String a, String b) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) == b.charAt(i)) {
                result.append('0');
            } else {
                result.append('1');
            }
        }
        return result.toString();
    }

    public String division(String dividend, String divisor) {
        dividend = dividend.replaceFirst("^0+", "");
        StringBuilder quotient = new StringBuilder(dividend.substring(0, divisor.length()));
        int count = divisor.length() - 1;

        while (true) {
            quotient = new StringBuilder(quotient.toString().replaceFirst("^0+", ""));
            if (quotient.length() == divisor.length()) {
                quotient = new StringBuilder(xor(quotient.toString(), divisor));
                quotient = new StringBuilder(quotient.toString().replaceFirst("^0+", ""));
            }

            if (dividend.length() - 1 >= count + 1) {
                count++;
                quotient.append(dividend.charAt(count));
            } else {
                break;
            }
        }

        if (quotient.toString().isEmpty()) {
            quotient = new StringBuilder("0");
        }
        return quotient.toString();
    }
}