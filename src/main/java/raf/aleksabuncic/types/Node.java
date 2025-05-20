package raf.aleksabuncic.types;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Node {
    private final int id;
    private final short listenPort;
    private final String bootstrapIp;
    private final short bootstrapPort;
    private final String imagePath;
    private final int weakThreshold;
    private final int strongThreshold;
    private final List<String> followers;
    @Setter
    private NodeVisibility visibility;

    public Node(int id, short listenPort, String bootstrapIp, short bootstrapPort, String imagePath, int weakThreshold, int strongThreshold) {
        this.id = id;
        this.listenPort = listenPort;
        this.bootstrapIp = bootstrapIp;
        this.bootstrapPort = bootstrapPort;
        this.imagePath = imagePath;
        this.weakThreshold = weakThreshold;
        this.strongThreshold = strongThreshold;
        this.followers = new ArrayList<>();
        this.visibility = NodeVisibility.PUBLIC;
    }
}
