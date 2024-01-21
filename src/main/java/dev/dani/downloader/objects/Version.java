package dev.dani.downloader.objects;

import lombok.Data;

/*
 * Project: Spigot-Downloader
 * Created at: 20/1/24 13:53
 * Created by: Dani-error
 */
@Data
public class Version {

    private String id;
    private String date;
    private String size;
    private String urlHash;

    public String getDownloadUrl() {
        return "https://getbukkit.org/get/" + urlHash;
    }
}
