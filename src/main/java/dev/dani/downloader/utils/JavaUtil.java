package dev.dani.downloader.utils;

import lombok.experimental.UtilityClass;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/*
 * Project: Spigot-Downloader
 * Created at: 20/1/24 10:19
 * Created by: Dani-error
 */
@UtilityClass
public class JavaUtil {

    public boolean isDirEmpty(final Path directory) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    public Integer tryParseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            return null;
        }
    }

    public void createFileWithContent(File file, String content) {
        try {
            // Create FileWriter and BufferedWriter
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Write content to the file
            bufferedWriter.write(content);

            // Close the resources
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
