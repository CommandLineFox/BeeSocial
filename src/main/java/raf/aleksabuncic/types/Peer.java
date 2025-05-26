package raf.aleksabuncic.types;

import java.util.Objects;

/**
 * Represents a node in the Chord ring.
 *
 * @param ip   IP address.
 * @param port Port number.
 */
public record Peer(String ip, int port) {
    @Override
    public String toString() {
        return ip + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Peer peer)) {
            return false;
        }
        return port == peer.port && Objects.equals(ip, peer.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}