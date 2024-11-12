package com.ar.askgaming.betterclans;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UtilityMethods {

    private BetterClans plugin;
    public UtilityMethods(BetterClans plugin) {
        this.plugin = plugin;
    }

    public void teleport(Player p, Location l){

        final int z = p.getLocation().getBlockZ(), x = p.getLocation().getBlockX();
        p.sendMessage(plugin.getFilesManager().getLang("misc.teleport_start", p));
        new BukkitRunnable() {		
            int count = 3;
            
            @Override
            public void run() {	      
                
                if (count == 0) {  
                    p.teleport(l);        		
                    cancel(); 
                    return;
                }	    	    	                                    	    	                        
                if (z != p.getLocation().getBlockZ() || x != p.getLocation().getBlockX()){
                    p.sendMessage(plugin.getFilesManager().getLang("misc.teleport_cancel", p));
                    cancel();
                    return;
                }
                count--;  
            }
        }.runTaskTimer(plugin, 0L, 20L); 
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
    //Change this to other class maybe
    public String getFormattedList(List<UUID> uuids) {
        StringBuilder list = new StringBuilder();
        for (UUID id : uuids) {
            list.append(plugin.getServer().getOfflinePlayer(id).getName()).append(", ");
        }
        if (list.length() > 0) {
            list.setLength(list.length() - 2); // Remove the trailing comma and space
        }
        return list.toString();
    }
    public boolean hasValidLength(String str, int min, int max) {
        return str.length() >= min && str.length() <= max;
    }
    public void listToPage(List<String> list, String[] args, Player p) {
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage(plugin.getFilesManager().getLang("commands.invalid_page", p));
                return;
            }
        }

        int totalPages = (int) Math.ceil(list.size() / 10.0);
        if (page > totalPages || page < 1) {
            p.sendMessage(plugin.getFilesManager().getLang("commands.invalid_page", p));
            return;
        }

        int start = (page - 1) * 10;
        int end = Math.min(start + 10, list.size());
        p.sendMessage(plugin.getFilesManager().getLang("misc.pages", p) + " " + page + "/" + totalPages);
        for (int i = start; i < end; i++) {
            String clan = list.get(i);
            p.sendMessage((i + 1) + ". " + clan);
        }
    }
}
