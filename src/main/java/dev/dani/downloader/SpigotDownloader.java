package dev.dani.downloader;

import dev.dani.downloader.objects.Version;
import dev.dani.downloader.utils.ConsoleUtil;
import dev.dani.downloader.utils.JavaUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Project: Spigot-Downloader
 * Created at: 20/1/24 09:55
 * Created by: Dani-error
 */
public class SpigotDownloader {

    private static final String BUKKIT_ENDPOINT = "https://getbukkit.org/download/spigot";
    private static final String DEFAULT_VERSION = "1.8.8";

    public static void main(String[] args) {
        ConsoleUtil.printHeader();

        String directory = ConsoleUtil.prompt("Enter the directory", "./server");

        File directoryFolder = new File(directory.replace('/', File.separatorChar));

        if (!directoryFolder.isDirectory() && directoryFolder.exists()) {
            ConsoleUtil.alert("That file is not a directory!");
            return;
        }

        if (directoryFolder.exists()) {
            try {
                if (!JavaUtil.isDirEmpty(directoryFolder.toPath())) {
                    ConsoleUtil.alert("That directory is not empty!");
                    return;
                }
            } catch (IOException e) {
                ConsoleUtil.alert("That directory is not empty!");
                return;
            }
        }

        ConsoleUtil.info("Fetching spigot versions index...");

        List<Version> versions = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(BUKKIT_ENDPOINT)
                    .header("User-Agent", "Spigot-Downloader/Indexer")
                    .header("Accept", "*/*")
                    .header("Connection", "keep-alive")
                    .get();

            doc.select("div").stream().filter(element -> element.className().equals("download-pane")).filter(element -> element.children().size() > 0 && element.child(0).className().equals("row vdivide")).forEach(element -> {
                Element inner = element.child(0);

                Version version = new Version();
                for (int i = 0; i < inner.children().size(); i++) {
                    String selector = i == 0 ? "h2" : "h3";
                    boolean last = i == inner.children().size() - 1;
                    Element loopedElement = inner.child(i);

                    if (last) {
                        String url = loopedElement.select("a").stream().filter(anchorElement -> anchorElement.hasAttr("href") && anchorElement.attr("href").startsWith("https://getbukkit.org/get/")).map(anchorElement -> anchorElement.attr("href")).findFirst().orElse(null);

                        if (url == null) continue;

                        String[] urlParts = url.split("/");

                        if (urlParts.length == 0) continue;

                        String lastPart = urlParts[urlParts.length - 1];

                        version.setUrlHash(lastPart);
                        versions.add(version);
                    } else {
                        Element dataElement = loopedElement.select(selector).stream().findFirst().orElse(null);

                        if ((dataElement == null || dataElement.html() == null) && i == 0) continue;

                        switch (i) {
                            case 0:
                                version.setId(dataElement.html());
                            case 1: {
                                if (dataElement != null && dataElement.html() != null) {
                                    version.setSize(dataElement.html());
                                }
                            }
                            case 2: {
                                if (dataElement != null && dataElement.html() != null) {
                                    version.setDate(dataElement.html());
                                }
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            ConsoleUtil.alert("Couldn't fetch the versions: " + e.getMessage());
            return;
        }

        Collections.reverse(versions);

        ConsoleUtil.info("These are all available versions:");

        int versionsPerRow = 3;
        int maxVersionLength = versions.stream().mapToInt(version -> version.getId().length()).max().orElse(0);
        int padding = 6; // Number of characters to separate from the left

        AtomicInteger index = new AtomicInteger(1);
        AtomicInteger defaultIndex = new AtomicInteger(-1);

        versions.forEach(version -> {
            if (version.getId().equalsIgnoreCase(DEFAULT_VERSION)) {
                defaultIndex.set(index.get());
            }

            String formattedVersion = String.format("%" + padding + "d. %-" + maxVersionLength + "s", index.getAndIncrement(), version.getId());
            System.out.print(formattedVersion);

            // Check if it's time to start a new row
            if (index.get() % versionsPerRow == 1) {
                System.out.println();  // Start a new line after every third version
            } else {
                System.out.print("\t\t");  // Add some space between versions
            }
        });

        // Add a new line at the end, if needed
        if (index.get() % versionsPerRow != 1) {
            System.out.println();
        }

        Version version = askForVersion(versions, defaultIndex);
        String downloadUrl = version.getDownloadUrl();
        String url;

        try {
            Document doc = Jsoup.connect(downloadUrl)
                    .header("User-Agent", "Spigot-Downloader/Downloader")
                    .header("Accept", "*/*")
                    .header("Connection", "keep-alive")
                    .get();

            url = doc.select("a").stream().filter(element -> element.hasAttr("href") && (element.attr("href").startsWith("https://cdn.getbukkit.org/") || element.attr("href").startsWith("https://download.getbukkit.org/"))).findFirst().map(element -> element.attr("href")).orElse(null);
        } catch (Exception e) {
            ConsoleUtil.alert("Couldn't fetch the CDN download url: " + e.getMessage());
            return;
        }

        if (url == null) {
            ConsoleUtil.alert("Couldn't fetch a valid CDN url to download.");
            return;
        }

        ConsoleUtil.info("Downloading Spigot " + version.getId() + "... (" + version.getSize() + ", " + version.getDate() + ")");

        if (!directoryFolder.exists()) {
            directoryFolder.mkdirs();
        }

        File jarFile = new File(directoryFolder, "server.jar");

        try {
            InputStream in = new URL(url).openStream();
            Files.copy(in, jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            ConsoleUtil.alert("Couldn't download the server jar file: " + e.getMessage());
        }

        JavaUtil.createFileWithContent(new File(directoryFolder, "eula.txt"), "eula=true");

        ConsoleUtil.info("Creating all required files...");

        try {
            String jarFilePath = jarFile.getAbsolutePath();

            String javaHome = System.getProperty("java.home");
            String javaExecutablePath = javaHome + File.separator + "bin" + File.separator + "java";

            // Build the command to execute the JAR file
            ProcessBuilder processBuilder = new ProcessBuilder(javaExecutablePath, "-jar", jarFilePath, "nogui");

            // Redirect standard output to capture it
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(directoryFolder);

            // Start the process
            Process process = processBuilder.start();

            // Read the output
            try (InputStream inputStream = process.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    // Check if the line contains "Generating keypair"
                    if (line.contains("Done")) {
                        process.destroy();  // Stop the process
                        break;
                    }
                }

            } catch (IOException e) {
                ConsoleUtil.alert("An unknown error occurred: " + e.getMessage());
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            ConsoleUtil.alert("An unknown error occurred: " + e.getMessage());
        }

        ConsoleUtil.info("Creating simple start script...");

        String extension = "sh";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            extension = "bat";
        }

        String scriptFileName = "start." + extension;
        File scriptFile = new File(directoryFolder, scriptFileName);
        try (InputStream inputStream = SpigotDownloader.class.getResourceAsStream("/" + scriptFileName)) {
            if (inputStream == null) {
                ConsoleUtil.alert("Couldn't find the script!");
                return;
            }

            Files.copy(inputStream, scriptFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            ConsoleUtil.info("Done!");
        } catch (IOException e) {
            ConsoleUtil.alert("An unknown error occurred: " + e.getMessage());
        }
    }

    private static Version askForVersion(List<Version> versions, AtomicInteger defaultIndex) {
        String selectedAsString = ConsoleUtil.prompt("Which version do you want to download?", String.valueOf(defaultIndex.get() != -1 ? defaultIndex.get() : null));

        Integer selectedNumber = JavaUtil.tryParseInt(selectedAsString);

        if (selectedNumber == null || selectedNumber < 1 || selectedNumber > versions.size()) {
            ConsoleUtil.alert("That version couldn't be found!");
            return askForVersion(versions, defaultIndex);
        }

        return versions.get(selectedNumber - 1);
    }

}
