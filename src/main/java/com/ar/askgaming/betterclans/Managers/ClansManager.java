package com.ar.askgaming.betterclans.Managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.ar.askgaming.betterclans.BetterClans;
import com.ar.askgaming.betterclans.UtilityMethods;
import com.ar.askgaming.betterclans.Clan.Clan;

public class ClansManager {

    private BetterClans plugin;

    private HashMap<String, Player> invited = new HashMap<>();
    private HashMap<Clan, Clan> invitedAlly = new HashMap<>();

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
    }


    public boolean createClan(String name, Player owner){
        if (clans.containsKey(name)){
            owner.sendMessage(plugin.getFilesManager().getLang("clan.already_exists", owner));
            return false;
        }
        new Clan(name, owner);
        return true;
    }
    public boolean removeClan(Clan clan){
        try {
            clan.getClanFile().delete();
            clans.remove(clan.getName());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
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
    public HashMap<String, Player> getInvited() {
        return invited;
    }
    public HashMap<Clan, Clan> getInvitedAlly() {
        return invitedAlly;
    }
    public boolean hasClan(Player p){
        return getClanByPlayer(p) != null;
    }
    public boolean isInClan(Clan clan, OfflinePlayer p){
        UUID playerId = p.getUniqueId();
        if (clan.getOwner().equals(playerId)){
            return true;
        } 
        if (clan.getMembers().contains(playerId)){
            return true;
        }
        if (clan.getOfficers().contains(playerId)){
            return true;
        }
        if (clan.getRecruits().contains(playerId)){
            return true;
        }
        return false;
    }

    public Clan getClanByOwner(Player p) {
        Clan clan = getClanByPlayer(p);
        if (clan != null && clan.getOwner().equals(p.getUniqueId())){
            return clan;
        }
        return null;
    } 

    public void sendInfo(Clan clan, Player p) {

        UtilityMethods u = plugin.getUtilityMethods();

        p.sendMessage("§6Clan: §e" + clan.getName());
        p.sendMessage("§6Tag: §e" + clan.getTag());
        p.sendMessage("§6Description: §e" + clan.getDescription());
        p.sendMessage("§6Owner: §e" + plugin.getServer().getOfflinePlayer(clan.getOwner()).getName());
    
        p.sendMessage("§6Officers: §e" + u.getFormattedList(clan.getOfficers()));
        p.sendMessage("§6Members: §e" + u.getFormattedList(clan.getMembers()));
        p.sendMessage("§6Recruits: §e" + u.getFormattedList(clan.getRecruits()));
    
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
    public enum Permission {
        INVITE, KICK, SET, HOME, ALLY, ENEMY, REMOVE, PROMOTE, DEMOTE, INVENTORY, DEPOSIT, WITHDRAW, BUY, WAR
    }
    public boolean hasClanPermission(Player p, Permission permission){
        Clan clan = getClanByPlayer(p);
        if (clan == null) {
            p.sendMessage(plugin.getFilesManager().getLang("clan.no_clan", p));
            return false;
        }
        if (p.isOp() || clan.getOwner().equals(p.getUniqueId())) {
            return true;
        }
    
        Map<String, List<UUID>> roles = Map.of(
            "officer", clan.getOfficers(),
            "member", clan.getMembers(),
            "recruit", clan.getRecruits()
        );
    
        for (Map.Entry<String, List<UUID>> role : roles.entrySet()) {
            if (role.getValue().contains(p.getUniqueId())) {
                List<String> permissions = plugin.getConfig().getStringList("permissions." + role.getKey());
                if (permissions.contains(permission.toString())) {
                    return true;
                }
            }
        }
        p.sendMessage(plugin.getFilesManager().getLang("clan.no_permission", p));
        return false;
    }
    public List<String> getAllClans(){
        return List.copyOf(clans.keySet());
    }
    public List<Player> getAllClanMembers(Clan clan) {
        List<Player> players = new ArrayList<>();
        addOnlinePlayer(players, clan.getOwner());
        addOnlinePlayers(players, clan.getMembers());
        addOnlinePlayers(players, clan.getOfficers());
        addOnlinePlayers(players, clan.getRecruits());
        return players;
    }
    public List<Player> getAllAlliedClanMembers(Clan clan) {
        List<Player> players = new ArrayList<>();
        for (String allyName : clan.getAllies()) {
            Clan allyClan = getClanByName(allyName);
            if (allyClan != null) {
                addOnlinePlayer(players, allyClan.getOwner());
                addOnlinePlayers(players, allyClan.getMembers());
                addOnlinePlayers(players, allyClan.getOfficers());
                addOnlinePlayers(players, allyClan.getRecruits());
            }
        }
        return players;
    }
    
    private void addOnlinePlayer(List<Player> players, UUID playerId) {
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            players.add(player);
        }
    }
    
    private void addOnlinePlayers(List<Player> players, List<UUID> playerIds) {
        for (UUID id : playerIds) {
            addOnlinePlayer(players, id);
        }
    }
}
