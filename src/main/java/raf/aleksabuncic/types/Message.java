package raf.aleksabuncic.types;

import java.io.Serializable;

public record Message(String type, String senderIp, int senderPort, String content) implements Serializable {

    @Override
    public String toString() {
        return "[" + type + "] from " + senderIp + ":" + senderPort + " → " + content;
    }
}