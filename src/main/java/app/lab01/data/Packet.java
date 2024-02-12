package app.lab01.data;

import java.io.IOException;

public class Packet {
    private String sourceAddress;
    private byte[] data;
    private String flag;
    private int destinationAddress;
    private byte fcs;

    public Packet(String sourceAddress, byte[] data, int destinationAddress, byte fcs) {
        this.sourceAddress = sourceAddress.replace("COM", "");
        this.data = data;
        this.flag = "#n";
        this.destinationAddress = destinationAddress;
        this.fcs = fcs;
    }

    public String toString() {
        String restoredStr = new String(data);
        return new String(flag + destinationAddress + sourceAddress + restoredStr);
    }

    public byte[] toByte() {
        byte[] originalArray = this.toString().getBytes();
        byte[] newArray = new byte[originalArray.length + 1];
        System.arraycopy(originalArray, 0, newArray, 0, originalArray.length);
        newArray[newArray.length - 1] = fcs;
        return newArray;
    }

}







