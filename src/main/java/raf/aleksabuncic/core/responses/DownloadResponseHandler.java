package raf.aleksabuncic.core.responses;

import raf.aleksabuncic.core.response.ResponseHandler;
import raf.aleksabuncic.core.runtime.NodeRuntime;
import raf.aleksabuncic.types.Message;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class DownloadResponseHandler extends ResponseHandler {
    public DownloadResponseHandler(NodeRuntime runtime) {
        super(runtime);
    }

    @Override
    public String type() {
        return "DOWNLOAD_RESPONSE";
    }

    @Override
    public void handle(Message msg) {
        String[] parts = msg.content().split("::", 2);
        if (parts.length != 2) {
            System.out.println("Malformed DOWNLOAD_RESPONSE.");
            return;
        }

        String filename = parts[0];
        byte[] content = Base64.getDecoder().decode(parts[1]);

        String downloadDir = runtime.getNodeModel().getImagePath() + File.separator + "downloads";
        File outFile = new File(downloadDir, filename);

        try {
            outFile.getParentFile().mkdirs();
            Files.write(outFile.toPath(), content);
            System.out.println("Downloaded and saved: " + outFile.getPath());
        } catch (Exception e) {
            System.out.println("Failed to save downloaded file: " + e.getMessage());
        }
    }
}