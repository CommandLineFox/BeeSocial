package raf.aleksabuncic.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import raf.aleksabuncic.types.BootstrapConfig;
import raf.aleksabuncic.types.Node;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ConfigHandler {
    /**
     * Loads all nodes from the given resource path.
     *
     * @param resourcePath Path to the config file.
     * @return List of nodes.
     */
    public static List<Node> loadAllNodes(String resourcePath) {
        List<Node> nodes = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = ConfigHandler.class.getClassLoader().getResourceAsStream(resourcePath);

            if (is == null) {
                throw new IllegalArgumentException("Couldn't find config file: " + resourcePath);
            }

            JsonNode root = mapper.readTree(is);
            for (JsonNode nodeEntry : root.get("nodeList")) {
                Node node = loadEntry(nodeEntry);
                nodes.add(node);
            }

        } catch (Exception e) {
            System.out.println("Error when loading all nodes");
        }

        return nodes;
    }

    /**
     * Loads a node from the given resource path.
     *
     * @param resourcePath Path to the config file.
     * @param id           ID of the node to load.
     * @return Node or null if not found.
     */
    public static Node loadNodeById(String resourcePath, int id) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = ConfigHandler.class.getClassLoader().getResourceAsStream(resourcePath);

            if (is == null) {
                throw new IllegalArgumentException("Couldn't find config file: " + resourcePath);
            }

            JsonNode root = mapper.readTree(is);
            for (JsonNode nodeEntry : root.get("nodeList")) {
                if (nodeEntry.get("id").asInt() == id) {
                    return loadEntry(nodeEntry);
                }
            }

        } catch (Exception e) {
            System.out.println("Error when loading node from config");
        }

        return null;
    }

    /**
     * Loads a node from the given JSON node.
     *
     * @param nodeEntry JSON node.
     * @return Node.
     */
    private static Node loadEntry(JsonNode nodeEntry) {
        short listenPort = (short) nodeEntry.get("listenPort").asInt();
        String bootstrapIp = nodeEntry.get("bootstrapIp").asText();
        short bootstrapPort = (short) nodeEntry.get("bootstrapPort").asInt();
        String imagePath = nodeEntry.get("imagePath").asText();
        int weakThreshold = nodeEntry.get("weakThreshold").asInt();
        int strongThreshold = nodeEntry.get("strongThreshold").asInt();

        return new Node(listenPort, bootstrapIp, bootstrapPort, imagePath, weakThreshold, strongThreshold);
    }

    /**
     * Returns the bootstrap configuration from the given resource path.
     *
     * @param resourcePath Path to the config file.
     * @return Bootstrap configuration or null if not found.
     */
    public static BootstrapConfig loadBootstrapConfig(String resourcePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = ConfigHandler.class.getClassLoader().getResourceAsStream(resourcePath);

            if (is == null) {
                throw new IllegalArgumentException("Couldn't find config file: " + resourcePath);
            }

            JsonNode root = mapper.readTree(is);
            JsonNode bootstrap = root.get("bootstrap");

            String ip = bootstrap.get("ip").asText();
            int port = bootstrap.get("port").asInt();

            return new BootstrapConfig(ip, port);

        } catch (Exception e) {
            System.out.println("Error when loading bootstrap config");
            return null;
        }
    }
}