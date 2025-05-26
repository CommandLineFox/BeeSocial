package raf.aleksabuncic.types;

/**
 * Bootstrap configuration.
 *
 * @param ip   IP of the bootstrap server.
 * @param port Port of the bootstrap server.
 */
public record BootstrapConfig(String ip, int port) {
}