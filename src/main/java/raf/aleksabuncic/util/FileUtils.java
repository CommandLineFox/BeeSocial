package raf.aleksabuncic.util;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * File utilities.
 */
public class FileUtils {
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
}