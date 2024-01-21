package dev.dani.downloader.utils;

import lombok.experimental.UtilityClass;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/*
 * Project: Spigot-Downloader
 * Created at: 20/1/24 09:56
 * Created by: Dani-error
 */
@UtilityClass
public class ConsoleUtil {

    private final Scanner scanner = new Scanner(System.in);

    public void printHeader() {
        InputStream stream = ConsoleUtil.class.getResourceAsStream("/version");
        System.out.println("""
                    
                    =()=
                ,/'\\_||_      Spigot Downloader
                ( (___  `.
                `\\./  `=='    Author: Dani-error
                       |||    $ver$
                       |||
                       |||
                """.indent(4).replace("$ver$", stream != null ? "Version: " + new Scanner(stream, StandardCharsets.UTF_8).useDelimiter("\\A").next() : ""));
    }

    public String prompt(String message) {
        return prompt(message, null);
    }

    public String prompt(String message, String defaultResponse) {
        System.out.print("  [?] " + message + (defaultResponse != null ? " (" + defaultResponse + ")" : "") + ": ");
        String result = scanner.nextLine();

        return result.isEmpty() ? defaultResponse : result;
    }

    public void alert(String message) {
        System.out.println("  [!] " + message);
    }

    public void info(String message) {
        System.out.println("  [i] " + message);
    }
}
