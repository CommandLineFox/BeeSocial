package raf.aleksabuncic.types;

import java.io.Serializable;

/**
 * Messages sent by nodes.
 *
 * @param type       Message type.
 * @param senderIp   IP of the sender.
 * @param senderPort Port of the sender.
 * @param content    Message content.
 */
public record Message(String type, String senderIp, int senderPort, String content) implements Serializable {

    @Override
    public String toString() {
        return "[" + type + "] from " + senderIp + ":" + senderPort + " → " + content;
    }
}