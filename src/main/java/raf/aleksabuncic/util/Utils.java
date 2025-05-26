package raf.aleksabuncic.util;

import raf.aleksabuncic.types.Token;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * File utilities.
 */
public class Utils {
    /**
     * List the files inside a directory path
     *
     * @param dirPath Directory path to search in
     * @return List of files
     */
    public static List<String> listFilesInDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }

        String[] fileNames = dir.list();
        if (fileNames == null) return Collections.emptyList();

        return List.of(fileNames);
    }

    /**
     * Checks if the provided path leads to an image or video
     *
     * @param localPath File path to check
     * @return True if it's an image, false if not
     */
    public static boolean checkIfMediaFile(String localPath) {
        return (localPath.endsWith(".jpg") || localPath.endsWith(".jpeg") || localPath.endsWith(".png") || localPath.endsWith(".gif") || localPath.endsWith(".bmp") || localPath.endsWith(".mp4") || localPath.endsWith(".mov") || localPath.endsWith(".avi") || localPath.endsWith(".webm") || localPath.endsWith(".mkv"));
    }


    /**
     * Extracts an original file from backup
     *
     * @param backupName Backup name to extract
     * @return Extracted file name or the original name if no underscore is found.
     */
    public static String extractOriginalFileName(String backupName) {
        int idx = backupName.indexOf('_');
        return (idx == -1) ? backupName : backupName.substring(idx + 1);
    }

    /**
     * Serializes a given token so it can be sent as a message content payload
     *
     * @param token Token to serialize
     * @return Serialized token
     */
    public static String serializeToken(Token token) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<Integer, Integer> entry : token.LN.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }

        sb.append("|");

        for (Integer queued : token.queue) {
            sb.append(queued).append(",");
        }

        return sb.toString();
    }

    /**
     * Deserializes a token when it's received
     *
     * @param content Serialized token
     * @return Deserialized token object
     */
    public static Token deserializeToken(String content) {
        Token token = new Token();

        String[] parts = content.split("\\|");
        if (parts.length != 2) return token;

        String[] lnEntries = parts[0].split(",");
        for (String entry : lnEntries) {
            if (entry.isBlank()) continue;
            String[] kv = entry.split(":");
            token.LN.put(Integer.parseInt(kv[0]), Integer.parseInt(kv[1]));
        }

        String[] queueEntries = parts[1].split(",");
        for (String q : queueEntries) {
            if (q.isBlank()) continue;
            token.queue.add(Integer.parseInt(q));
        }

        return token;
    }
}