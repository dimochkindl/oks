package app.lab01.controllers;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Thread.sleep;
import static java.text.ChoiceFormat.nextDouble;

public class PortController {

    private SerialPort[] availablePorts;

    private TextArea textArea;

    private Double stopBits;
    private String currentPort;

    private TextField getBytesNumberField;

    private int getBytes;
    private boolean error = false;
    private byte lastDataInChannel;
    private final String jam = "!";

    private int jamRecievedCounter = 0;

    public PortController() {
    }

    public void setPort(String port) {
        this.currentPort = port;
    }

    public void setGetBytesNumberField(TextField getBytesNumberField) {
        this.getBytesNumberField = getBytesNumberField;
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    public void setStopBits(Double bits) {
        stopBits = bits;
    }

    public List<String> getComPortNames() {
        SerialPort[] all = SerialPort.getCommPorts();
        List<SerialPort> mb = new ArrayList<>();
        for (SerialPort port : all) {
            if (!port.isOpen() && !port.getSystemPortName().equals("COM5") && !port.getSystemPortName().equals("COM6")) {
                if (port.getSystemPortName().length() > 4) {
                    break;
                }
                mb.add(port);
                System.out.println("add port: " + port);
            }
        }

        availablePorts = mb.toArray(new SerialPort[0]);
        List<String> names = new ArrayList<>();
        for (SerialPort port : availablePorts) {
            names.add(port.getSystemPortName().substring(3, port.getSystemPortName().length()));
        }
        return names;
    }

    public String getCurrentPort() {
        return currentPort;
    }

    public void setCurrentPort(String currentPort) {
        currentPort = "COM" + currentPort;
        this.currentPort = currentPort;
    }

    public SerialPort setDefaultParameters() {
        List<SerialPort> ports = Arrays.stream(availablePorts).toList();
        int index = -1;
        for (SerialPort port : ports) {
            if (currentPort.equals(port.getSystemPortName())) {
                index = ports.indexOf(port);
                System.out.println(index);
            }
        }
        if (index == -1) {
            throw new RuntimeException("error with portChoice");
        }
        SerialPort port = availablePorts[index];

        port.setBaudRate(9600);
        port.setNumDataBits(8);
        port.setNumStopBits((stopBits.intValue()));
        port.setParity(SerialPort.NO_PARITY);

        port.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {
                if (serialPortEvent.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    System.out.println("Number of stopBits: " + stopBits);
                    int i = 0;
                    int numberOfBytes = 0;
                    byte[] newData = new byte[50];
                    if((port.bytesAvailable()) != 0){
                        newData = new byte[port.bytesAvailable()];
                        System.out.println("newData length: "+newData.length);
                        try {
                            sleep(5);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        i = port.bytesAvailable();
                        System.out.println("i length: "+i);

                        numberOfBytes = port.readBytes(newData, i);
                        String jam = "jam";
                        byte[] byteJam = jam.getBytes(StandardCharsets.UTF_8);
                        System.out.println("the data: "+new String(newData));
                        System.out.println("data len: "+newData.length);

                        int length = numberOfBytes;
                        int jamLength = byteJam.length;

                        if (length >= jamLength) {
                            boolean hasJam = true;

                            for (int j = 0; j < jamLength; j++) {
                                if (newData[length - jamLength + j] != byteJam[j]) {
                                    hasJam = false;
                                    break;
                                }
                            }

                            if(hasJam){
                                newData = new byte[50];
                                return;
                            }
                        }
                    }
                    //int numRead = port.readBytes(newData, newData.length);
                    getBytes += numberOfBytes;
                    System.out.println("numRead: " + numberOfBytes);
                    newData = Arrays.copyOfRange(newData, 0, 19);
                    if (numberOfBytes > 0) {

                        byte lastByte = newData[newData.length - 1];
                        String receivedData = ByteUnstuffing(newData);

                        System.out.println("Принято: " + receivedData);
                        String receivedStringUnstuffed = new String(newData, StandardCharsets.UTF_8).substring(4);
                        String out = "";

                        if(new Random().nextInt(10) < 3){
                            error = true;
                            int index = ThreadLocalRandom.current().nextInt(0, 112);
                            StringBuilder binaryString = new StringBuilder();

                            for (char character : receivedStringUnstuffed.toCharArray()) {
                                String binaryChar = String.format("%8s", Integer.toBinaryString(character & 0xFF)).replace(' ', '0');
                                binaryString.append(binaryChar);
                            }
                            char bit = binaryString.charAt(index);
                            char invertedBit = bit == '0' ? '1': '0';
                            binaryString.setCharAt(index, invertedBit);

                            String stringWithError = binaryString.toString();
                            System.out.println(stringWithError.length());

                            if (!(division(stringWithError, "10000011").equals('0'))) {
                                for (int j = 0; j < stringWithError.length() - 8; j++) {
                                    if (stringWithError.charAt(j) == '0') {
                                        stringWithError = stringWithError.substring(0, j) + '1' + stringWithError.substring(j + 1);
                                    } else {
                                        stringWithError = stringWithError.substring(0, j) + '0' + stringWithError.substring(j + 1);
                                    }

                                    if (division(stringWithError, "10000011").equals('0')){
                                        break;
                                    } else {
                                        if (stringWithError.charAt(j) == '0') {
                                            stringWithError = stringWithError.substring(0, j) + '1' + stringWithError.substring(j + 1);
                                        } else {
                                            stringWithError = stringWithError.substring(0, j) + '0' + stringWithError.substring(j + 1);
                                        }
                                    }
                                }
                            }
                            StringBuilder charString = new StringBuilder();
                            for (int j = 0; j < stringWithError.length(); j += 8) {
                                String byteString = stringWithError.substring(j, j + 8);
                                int byteValue = Integer.parseInt(byteString, 2);
                                charString.append((char) byteValue);
                            }
                            out = charString.toString();
                        }
                        int read = numberOfBytes;

                        if (textArea != null) {
                            Platform.runLater(() -> {
                                if (read == 13 || receivedData.equals("\r")) {
                                    textArea.appendText(System.lineSeparator());
                                }
                                textArea.appendText(receivedData);
                            });
                        }
                        if (getBytesNumberField != null) {
                            Platform.runLater(() -> {
                                getBytesNumberField.clear();
                                getBytesNumberField.appendText(String.valueOf(getBytes));
                                if(error) {
                                    getBytesNumberField.appendText(" *");
                                    error = false;
                                }
                            });
                        }
                    }
                }
            }
        });

        System.out.println("port opened in default parameters: " + port.isOpen() + port.getSystemPortName());
        return port;
    }

    public int sendBytes(byte[] text) {
        int attempts = 0;
        SerialPort sendingPort = null;
        for (SerialPort port : availablePorts) {
            if (currentPort.equals(port.getSystemPortName())) {
                sendingPort = port;
            }
        }
        while(true) {
            if (sendingPort.bytesAvailable() != 0 || isBusyChannel()) {
                continue;
            }
            writeBytes(sendingPort, text, 0);
            if (isCollision()) {
                String jam = "jam";
                byte[] bytes = jam.getBytes(StandardCharsets.UTF_8);
                writeBytes(sendingPort, bytes);
                attempts++;
                if (attempts > 10) {
                    return -1;
                }
                makeExponentialBackoff(attempts);
            } else {
                break;
            }
        }
        return attempts;
    }




    public boolean closePreviousPort(String name) {
        name = "COM" + name;
        for (SerialPort port : availablePorts) {
            if (port.getSystemPortName().equals(name) && port.closePort()) {
                return true;
            }
        }
        return false;
    }

    public boolean openCurrentPort() {
        for (SerialPort port : availablePorts) {
            if (port.getSystemPortName().equals(currentPort) && port.openPort()) {
                return true;
            }
        }
        return false;
    }

    public boolean checkForOpenPort(String name) {
        name = "COM" + name;
        for (SerialPort port : availablePorts) {
            if (port.getSystemPortName().equals(name) && port.openPort()) {
                return true;
            }
        }
        return false;
    }

    public String changePort(String currentPort) {
        if (currentPort == null)
            return null;
        int number = Integer.parseInt(currentPort);
        number++;

        this.currentPort = "COM" + number;
        return String.valueOf(number);
    }

    public String ByteUnstuffing(byte[] bytes) {
        String mineData = new String(bytes);
        mineData = mineData.substring(4, 18);
        StringBuilder result = new StringBuilder();
        int i = 0;
        while(i<mineData.length()){
            if(mineData.startsWith("&E2", i)){
                result.append("#n");
                i+=2;
            }else if (mineData.startsWith("&E1", i)){
                result.append("&E");
                i+=2;
            }else {
                result.append(mineData.charAt(i));
                i++;
            }
        }

        mineData = result.toString();
        mineData = mineData.replaceAll("\0", "");
        System.out.println(mineData);
        return mineData;
    }

    private boolean isBusyChannel() {
        return new Random().nextDouble() < 0.3;
    }

    private boolean isCollision() {
        return new Random().nextDouble() < 0.7;
    }

    private void makeExponentialBackoff(int attempts) {
        int delay = new Random().nextInt(0, (int) Math.pow(2, attempts));
        try {
            sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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


    private void simulateDelay(){
        try {
            sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeBytes(SerialPort port, byte[] text){
        port.writeBytes(text, text.length);
        simulateDelay();
    }

    private void writeBytes(SerialPort port, byte[] text, int i){
        port.writeBytes(text, text.length);
    }
}