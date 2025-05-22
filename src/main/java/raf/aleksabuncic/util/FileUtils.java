package raf.aleksabuncic.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

public class FileUtils {
    /**
     * Uploads a file to a node's image root directory
     *
     * @param sourcePathStr Path to the file to upload
     * @param imageRoot     Destination to upload to
     * @return True is successful, false is failed
     */
    public static boolean uploadToWorkingRoot(String sourcePathStr, String imageRoot) {
        try {
            Path sourcePath = Path.of(sourcePathStr);
            if (!Files.exists(sourcePath)) {
                System.out.println("File does not exist: " + sourcePathStr);
                return false;
            }

            File rootDir = new File(imageRoot);
            if (!rootDir.exists()) {
                rootDir.mkdirs();
            }

            Path targetPath = Path.of(imageRoot, sourcePath.getFileName().toString());
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to upload file: " + e.getMessage());
            return false;
        }
    }

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