package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;
import raf.aleksabuncic.types.NodeVisibility;
import raf.aleksabuncic.util.FileUtils;

import java.io.File;

public class ListFilesHandler extends ResponseHandler {
    public ListFilesHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "LIST_FILES";
    }

    @Override
    public void handle(Message msg) {
        System.out.println("Received LIST_FILES from Node " + msg.senderId());

        boolean allowed = runtime.getNodeModel().getVisibility() == NodeVisibility.PUBLIC ||
                runtime.getFollowers().contains(msg.senderId());

        if (!allowed) {
            System.out.println("Access denied to Node " + msg.senderId());
            return;
        }

        String uploadsPath = runtime.getNodeModel().getImagePath() + File.separator + "uploads";
        var files = FileUtils.listFilesInDirectory(uploadsPath);
        String content = String.join(",", files);

        Message response = new Message("LIST_FILES_RESPONSE", runtime.getNodeModel().getListenPort(), content);
        Sender.sendMessage("127.0.0.1", msg.senderId(), response);
        System.out.println("Sent LIST_FILES_RESPONSE to Node " + msg.senderId());
    }
}