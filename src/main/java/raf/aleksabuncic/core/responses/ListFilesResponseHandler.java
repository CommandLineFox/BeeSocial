package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

public class ListFilesResponseHandler extends ResponseHandler {
    public ListFilesResponseHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "LIST_FILES_RESPONSE";
    }

    @Override
    public void handle(Message msg) {
        System.out.println("Received LIST_FILES_RESPONSE from Node " + msg.senderId());
        String[] files = msg.content().split(",");
        if (files.length == 0 || (files.length == 1 && files[0].isEmpty())) {
            System.out.println("Remote node has no files.");
        } else {
            System.out.println("Files on remote node " + msg.senderId() + ":");
            for (String f : files) {
                System.out.println(" - " + f);
            }
        }
    }
}
