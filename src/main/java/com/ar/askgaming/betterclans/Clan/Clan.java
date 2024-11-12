package com.ar.askgaming.betterclans.Clan;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.betterclans.BetterClans;
import com.ar.askgaming.betterclans.UtilityMethods;

public class Clan implements ConfigurationSerializable{

    private BetterClans plugin = BetterClans.getPlugin(BetterClans.class);

    private File clanFile;
    private FileConfiguration clanConfig;

    public File getClanFile() {
        return clanFile;
    }

    public void setClanFile(File clanFile) {
        this.clanFile = clanFile;
    }

    public FileConfiguration getClanConfig() {
        return clanConfig;
    }

    public void setClanConfig(FileConfiguration clanConfig) {
        this.clanConfig = clanConfig;
    }

    public Clan(String name, Player owner){
        
        clanFile = new File(plugin.getDataFolder() + "/clans/"+name+".yml");

        if (!clanFile.exists()){
            try {
                clanFile.createNewFile();
                Bukkit.getOnlinePlayers().forEach(p -> {
                    p.sendMessage(plugin.getFilesManager().getLang("misc.create_broadcast", p).replace("{owner}", owner.getName()).replace("{clan}", name));
                });
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        clanConfig = new YamlConfiguration();

        plugin.getClansManager().getClans().put(name, this);

        this.name = name;
        this.owner = owner.getUniqueId();
        level = 1;
        balance = 0;
        description = "";
        tag = name.substring(0, 3);
        allies = new ArrayList<>();
        enemies = new ArrayList<>();
        members = new ArrayList<>();
        officers = new ArrayList<>();
        recruits = new ArrayList<>();
        loadInventory(null, 27);
        
        clanConfig.set(name, this);
        save();
    }

    @SuppressWarnings("unchecked")
    public Clan(Map<String, Object> map) {
        name = (String) map.get("name");
        tag = (String) map.get("tag");
        description = (String) map.get("description");
        owner = UUID.fromString((String) map.get("owner"));
        level = (int) map.get("level");
        home = (Location) map.get("home");
        balance = (double) map.get("balance");

        UtilityMethods u = plugin.getUtilityMethods();
        members = u.loadUUIDList(map.get("members"));
        officers = u.loadUUIDList(map.get("officers"));
        recruits = u.loadUUIDList(map.get("recruits"));
        allies = u.loadStringList(map.get("allies"));
        enemies = u.loadStringList(map.get("enemies"));
        
        List<ItemStack> items = (List<ItemStack>) map.get("inventory_content");
        loadInventory(items, (int) map.get("inventory_size"));

    }
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("name", name);
        map.put("tag", tag);
        map.put("description", description);
        map.put("owner", owner.toString());
        map.put("members", members.toString());
        map.put("officers", officers.toString());
        map.put("recruits", recruits.toString());
        map.put("inventory_content", inventory.getContents());
        map.put("inventory_size", inventory.getSize());
        map.put("level", level);
        map.put("home", home);
        map.put("balance", balance);
        map.put("allies", allies);
        map.put("enemies", enemies);
        return map;
        
    }

    public void loadInventory(List<ItemStack> items,int size){
        inventory = plugin.getServer().createInventory(null, size, name);
        if (items != null) {
            ItemStack[] i = items.toArray(new ItemStack[0]); // Convertir a arreglo
            inventory.setContents(i)   ; 
        }
    }

    public Clan deserialize(Map<String, Object> map) {
        return new Clan(map);
    }

    public void removePlayerFromClan(Clan clan, Player p){
        UUID playerId = p.getUniqueId();
        getOfficers().remove(playerId);
        getMembers().remove(playerId);
        getRecruits().remove(playerId);

        save();
    }
    public void promotePlayer(Clan clan, OfflinePlayer p){
        UUID playerId = p.getUniqueId();
        if (getRecruits().remove(playerId)) {
            getMembers().add(playerId);
        } else if (getMembers().remove(playerId)) {
            getOfficers().add(playerId);
        }

        save() ;
    }
    public void demotePlayer(Clan clan, OfflinePlayer p){
        UUID playerId = p.getUniqueId();
        if (getOfficers().remove(playerId)) {
            getMembers().add(playerId);
        } else if (getMembers().remove(playerId)) {
            getRecruits().add(playerId);
        }

        save();
    }
    public void addAlly(Clan ally){
        allies.add(ally.getName());
        save();
    }
    public void removeAlly(Clan ally){
        allies.remove(ally.getName());
        save();
    }
    public void addEnemy(Clan enemy){
        enemies.add(enemy.getName());
        save();
    }
    public void removeEnemy(Clan enemy){
        enemies.remove(enemy.getName());
        save();
    }

    public void save(){
        try {
            clanConfig.save(clanFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String name;
    private String tag;
    private String description;
    private UUID owner;
    private List<UUID> members;
    private List<UUID> officers;
    private List<UUID> recruits;
    private Inventory inventory;
    private int level;
    private Location home;
    private double balance;
    private List<String> allies;
    private List<String> enemies;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        File newFile = new File(plugin.getDataFolder() + "/clans/" + name + ".yml");
        if (clanFile.renameTo(newFile)) {
            plugin.getClansManager().getClans().remove(this.name.toLowerCase());
            clanConfig.set(this.name, null);
            this.name = name;
            clanFile = newFile;
            plugin.getClansManager().getClans().put(name.toLowerCase(), this);
            clanConfig.set(name, this);
            save();
        } else {
            plugin.getLogger().severe("Failed to rename clan file to " + name + ".yml");
        }
    }
    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
        save();
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
        save();
    }
    public UUID getOwner() {
        return owner;
    }
    public void setOwner(UUID owner) {
        this.owner = owner;
        save();
    }
    public List<UUID> getMembers() {
        return members;
    }
  
    public List<UUID> getOfficers() {
        return officers;
    }

    public List<UUID> getRecruits() {
        return recruits;
    }

    public Inventory getInventory() {
        return inventory;
    }
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
        save();
    }
    public Location getHome() {
        return home;
    }
    public void setHome(Location home) {
        this.home = home;
        save();
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
        save();
    }
    public List<String> getAllies() {
        return allies;
    }
   
    public List<String> getEnemies() {
        return enemies;
    }

}
