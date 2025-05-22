package raf.aleksabuncic.starter;

import raf.aleksabuncic.bootstrap.BootstrapServer;
import raf.aleksabuncic.config.ConfigHandler;
import raf.aleksabuncic.types.BootstrapConfig;

public class BootstrapStarter {
    public static void main(String[] args) {
        BootstrapConfig config = ConfigHandler.loadBootstrapConfig("config.json");
        if (config == null) {
            System.out.println("Failed to load bootstrap configuration.");
            return;
        }

        System.out.println("Starting Bootstrap on " + config.ip() + ":" + config.port());

        new Thread(new BootstrapServer(config.port())).start();
    }
}
