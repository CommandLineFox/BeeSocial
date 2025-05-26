package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

/**
 * Handles what happens when a node responds to a LIST_FILES request.
 */
public class ListFilesResponseHandler extends ResponseHandler {
    public ListFilesResponseHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "LIST_FILES_RESPONSE";
    }

    @Override
    public void handle(Message message) {
        System.out.println("Received LIST_FILES_RESPONSE from Node " + message.senderPort());
        String[] files = message.content().split(",");
        if (files.length == 0 || (files.length == 1 && files[0].isEmpty())) {
            System.out.println("Remote node has no files.");
            return;
        }

        System.out.println("Files on remote node " + message.senderPort() + ":");
        for (String f : files) {
            System.out.println(" - " + f);
        }
    }
}