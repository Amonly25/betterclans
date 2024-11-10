package com.ar.askgaming.betterclans;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.betterclans.Listeners.EntityDamageByEntityListener;
import com.ar.askgaming.betterclans.Listeners.InventoryCloseListener;

public class BetterClans extends JavaPlugin {


    public void onEnable() {

        saveDefaultConfig();

        ConfigurationSerialization.registerClass(Clan.class,"Clan");

        filesManager = new FilesManager(this);

        clansManager = new ClansManager(this);

        getServer().getPluginCommand("clans").setExecutor(new Commands(this));

        getServer().getPluginManager().registerEvents(new InventoryCloseListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);

    }
    public void onDisable() {
        for (Clan clan : clansManager.getClans().values()){
 
            clan.save();
        }
    }

    private ClansManager clansManager;
    private FilesManager filesManager;

    public FilesManager getFilesManager() {
        return filesManager;
    }
    public ClansManager getClansManager() {
        return clansManager;
    }
}