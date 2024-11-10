package com.ar.askgaming.betterclans;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FilesManager {

    private BetterClans plugin;
    public FilesManager(BetterClans plugin) {
        this.plugin = plugin;

        File protectionsFolder = new File(plugin.getDataFolder() + "/clans");
        if (!protectionsFolder.exists()) {
            protectionsFolder.mkdirs();
        }
    }
    public List<UUID> loadUUIDList(Object obj) {
    List<UUID> list = new ArrayList<>();
    if (obj instanceof String) {
        String str = (String) obj;
        String[] array = str.replace("[", "").replace("]", "").split(",\\s*");
        for (String item : array) {
            if (item.isEmpty()) {
                continue;
            }
            list.add(UUID.fromString(item));
        }
    }
    return list;
    }

    public List<String> loadStringList(Object obj) {
        List<String> list = new ArrayList<>();
        if (obj instanceof String) {
            String str = (String) obj;
            String[] array = str.replace("[", "").replace("]", "").split(",\\s*");
            for (String item : array) {
                if (item.isEmpty()) {
                    continue;
                }
                list.add(item);
            }
        }
        return list;
    }
}
