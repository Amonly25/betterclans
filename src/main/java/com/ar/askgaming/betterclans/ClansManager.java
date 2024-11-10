package com.ar.askgaming.betterclans;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ClansManager {

    private BetterClans plugin;

    private HashMap<String, Player> invited = new HashMap<>();
    private final Map<String, FileConfiguration> clanConfigurations = new HashMap<>();

    public HashMap<String, Player> getInvited() {
        return invited;
    }

    public ClansManager(BetterClans plugin) {
        this.plugin = plugin;

        File folder = new File(plugin.getDataFolder(), "/clans");

        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }

        // Listar todos los archivos .yml en la carpeta
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }

        // Cargar cada archivo y almacenarlo en el mapa
        for (File file : files) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String clanName = file.getName().replace(".yml", ""); // Remover la extensión .yml
            Object obj = config.get(clanName);
            if (obj instanceof Clan) {
                Clan clan = (Clan) obj;
                clan.setClanFile(file);
                clan.setClanConfig(config);
                // Guardar cada Protection en el mapa con su clave
                clans.put(clanName, clan);
            }

        }

            // FileConfiguration config = plugin.getFilesManager().getClansConfig();

            // // Obtener todas las claves del nivel raíz
            // Set<String> clansKeys = config.getKeys(false);
    
            // // Iterar sobre todas las keys y cargar cada Protection
            // for (String key : clansKeys) {
            //     Object obj = config.get(key);
            //     if (obj instanceof Clan) {
            //         Clan clan = (Clan) obj;
    
            //         // Guardar cada Protection en el mapa con su clave
            //         clans.put(key.toLowerCase(), clan);

            //         // for (Player p : Bukkit.getOnlinePlayers()){
            //         //     //Only load clans for online players, Test it
            //         //     if (clan.getMembers().contains(p.getUniqueId())){
            //         //         clans.put(key, clan);
            //         //     }
            //         // }
            //     }
            // }
    }
  

    public boolean createClan(String name, Player owner){
        // This are checking two times ??
        if (clans.containsKey(name)){
            return false;
        }
        new Clan(name, owner);
        return true;
    }
    public boolean removeClan(Clan clan){

        clan.getClanFile().delete();
        clans.remove(clan.getName());
        return true;
    }

    private HashMap<String, Clan> clans = new HashMap<>();

    public HashMap<String, Clan> getClans() {
        return clans;
    }

    public Clan getClanByPlayer(Player p){
        UUID playerId = p.getUniqueId();
        for (Clan clan : clans.values()) {
            if (clan.getOwner().equals(playerId) || clan.getMembers().contains(playerId) ||
                clan.getOfficers().contains(playerId) || clan.getRecruits().contains(playerId)) {
                return clan;
            }
        }
        return null;
    }
    public Clan getClanByName(String name){
        return clans.get(name);
    }
    public boolean hasInventoryPermission(Player p){
        Clan clan = getClanByPlayer(p);
        if (clan == null){
            return false;
        }
        if (clan.getRecruits().contains(p.getUniqueId())){
            return false;
        }

        return true;
    }

    public boolean hasHomePermission(Player p) {
        Clan clan = getClanByPlayer(p);
        if (clan == null){
            return false;
        }
        if (clan.getRecruits().contains(p.getUniqueId())){
            return false;
        }

        return true;
    }
    public void teleport(Player p, Location l){

        if (l == null) {
            p.sendMessage("There is no location available");
            return;
        }

        final int z = p.getLocation().getBlockZ(), x = p.getLocation().getBlockX();

        new BukkitRunnable() {		
            int count = 3;
            
            @Override
            public void run() {	      
                
                if (count == 0) {  
                    p.sendMessage("Seras teletransportado en 3 segundos");
                    p.teleport(l);        		
                    cancel(); 
                    return;
                }	    	    	                                    	    	                        
                if (z != p.getLocation().getBlockZ() || x != p.getLocation().getBlockX()){
                    p.sendMessage("No te puedes mover al teleportarte");
                    cancel();
                    return;
                }
                count--;  
            }
        }.runTaskTimer(plugin, 0L, 20L); 
    }


    public boolean hasKickInvitePermission(Player p) {
        Clan clan = getClanByPlayer(p);
        if (clan == null){
            return false;
        }
        if (clan.getOwner().equals(p.getUniqueId()) || clan.getOfficers().contains(p.getUniqueId())){
            return true;
        }   
        return false;
    }
    public boolean hasSetHomePermission(Player p) {
        Clan clan = getClanByPlayer(p);
        if (clan == null){
            return false;
        }
        if (clan.getOwner().equals(p.getUniqueId()) || clan.getOfficers().contains(p.getUniqueId())){
            return true;
        }   
        return false;
    }
    public boolean hasClan(Player p){
        return getClanByPlayer(p) != null;
    }
    public boolean isInClan(Clan clan, OfflinePlayer p){
        UUID playerId = p.getUniqueId();
        return clan.getOwner().equals(playerId) || clan.getMembers().contains(playerId) ||
               clan.getOfficers().contains(playerId) || clan.getRecruits().contains(playerId);
    }

    public Clan getClanByOwner(Player p) {
        Clan clan = getClanByPlayer(p);
        if (clan != null && clan.getOwner().equals(p.getUniqueId())){
            return clan;
        }
        return null;
    } 

    public void sendInfo(Clan clan, Player p ){
        p.sendMessage("§6Clan: §e" + clan.getName());
        p.sendMessage("§6Tag: §e" + clan.getTag());
        p.sendMessage("§6Description: §e" + clan.getDescription());
        p.sendMessage("§6Owner: §e" + plugin.getServer().getOfflinePlayer(clan.getOwner()).getName());

        StringBuilder officersList = new StringBuilder();
        for (UUID officerId : clan.getOfficers()) {
            officersList.append(plugin.getServer().getOfflinePlayer(officerId).getName()).append(", ");
        }
        if (officersList.length() > 0) {
            officersList.setLength(officersList.length() - 2); // Remove the trailing comma and space
        }
        p.sendMessage("§6Officers: §e" + officersList.toString());
        StringBuilder membersList = new StringBuilder();
        for (UUID memberId : clan.getMembers()) {
            membersList.append(plugin.getServer().getOfflinePlayer(memberId).getName()).append(", ");
        }
        if (membersList.length() > 0) {
            membersList.setLength(membersList.length() - 2); // Remove the trailing comma and space
        }
        p.sendMessage("§6Members: §e" + membersList.toString());

        StringBuilder recruitsList = new StringBuilder();
        for (UUID recruitId : clan.getRecruits()) {
            recruitsList.append(plugin.getServer().getOfflinePlayer(recruitId).getName()).append(", ");
        }
        if (recruitsList.length() > 0) {
            recruitsList.setLength(recruitsList.length() - 2); // Remove the trailing comma and space
        }
        p.sendMessage("§6Recruits: §e" + recruitsList.toString());
        p.sendMessage("§6Level: §e" + clan.getLevel());
        p.sendMessage("§6Balance: §e" + clan.getBalance());
        p.sendMessage("§6Allies: §e" + clan.getAllies().toString());
        p.sendMessage("§6Enemies: §e" + clan.getEnemies().toString());
    }
    public boolean isAlly(Player p1, Player p2){
        Clan clan1 = getClanByPlayer(p1);
        Clan clan2 = getClanByPlayer(p2);
        if (clan1 == null || clan2 == null){
            return false;
        }
        if (clan1.equals(clan2)){
            return true;
        }
        if (clan1.getAllies().contains(clan2.getName()) || clan2.getAllies().contains(clan1.getName())){
            return true;
        }
        return false;
    }
}
