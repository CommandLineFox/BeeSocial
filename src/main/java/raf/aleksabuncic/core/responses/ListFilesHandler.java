package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.NodeVisibility;
import raf.aleksabuncic.util.Utils;

import java.io.File;
import java.util.List;

/**
 * Handles what happens when a node receives a LIST_FILES request for an ID it is responsible for.
 */
public class ListFilesHandler extends ResponseHandler {
    public ListFilesHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "LIST_FILES";
    }

    @Override
    public void handle(Message message) {
        runtime.enterCriticalSection();

        try {
            String targetId = message.content();

            boolean allowed = runtime.getNodeModel().getVisibility() == NodeVisibility.PUBLIC ||
                    runtime.getFollowers().contains(message.senderPort());

            if (!allowed) {
                System.out.println("Access denied to Node " + message.senderPort());
                return;
            }

            String uploadsPath = runtime.getNodeModel().getWorkPath() + File.separator + "uploads";
            List<String> files = Utils.listFilesInDirectory(uploadsPath);
            String content = String.join(",", files);

            Message response = new Message("LIST_FILES_RESPONSE", runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), runtime.getNodeModel().getListenIp(), runtime.getNodeModel().getListenPort(), content);

            String senderId = runtime.hashString(message.initiatorIp() + ":" + message.initiatorPort());
            runtime.forwardMessage(senderId, response);
        } finally {
            runtime.exitCriticalSection();
        }
    }
}