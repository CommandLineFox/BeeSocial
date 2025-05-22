package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.net.Sender;
import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class DownloadRequestHandler extends ResponseHandler {
    public DownloadRequestHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "DOWNLOAD_REQUEST";
    }

    @Override
    public void handle(Message msg) {
        String filename = msg.content();

        File file = new File(runtime.getNodeModel().getImagePath() + File.separator + "uploads", filename);

        if (!file.exists() || !file.isFile()) {
            System.out.println("File " + filename + " not found in uploads/ for download.");
            return;
        }

        try {
            byte[] content = Files.readAllBytes(file.toPath());
            String encoded = Base64.getEncoder().encodeToString(content);
            String responseContent = filename + "::" + encoded;

            Message response = new Message("DOWNLOAD_RESPONSE", runtime.getNodeModel().getListenPort(), responseContent);
            Sender.sendMessage("127.0.0.1", msg.senderId(), response);

            System.out.println("Sent DOWNLOAD_RESPONSE to Node " + msg.senderId());
        } catch (Exception e) {
            System.out.println("Failed to send file: " + e.getMessage());
        }
    }
}