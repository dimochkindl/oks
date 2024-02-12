package app.lab01;

import com.fazecast.jSerialComm.SerialPort;

public class PortsCloser implements Runnable {
    public PortsCloser() {
    }

    public static void closePorts() {
        SerialPort[] all = SerialPort.getCommPorts();
        for (SerialPort port : all) {
            if (port.isOpen()) {
                System.out.println("Закрываю порт: " + port.getSystemPortName());
                port.closePort();
                System.out.println("Порт успешно закрыт.");
            }
        }
    }
    @Override
    public void run() {
        closePorts();
    }
}
