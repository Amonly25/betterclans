package com.ar.askgaming.betterclans;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.betterclans.Clan.Clan;
import com.ar.askgaming.betterclans.Clan.ClanChat;
import com.ar.askgaming.betterclans.Listeners.AsyncPlayerChatListener;
import com.ar.askgaming.betterclans.Listeners.EntityDamageByEntityListener;
import com.ar.askgaming.betterclans.Listeners.InventoryCloseListener;
import com.ar.askgaming.betterclans.Managers.ClansManager;
import com.ar.askgaming.betterclans.Managers.FilesManager;

public class BetterClans extends JavaPlugin {


    public void onEnable() {

        saveDefaultConfig();

        ConfigurationSerialization.registerClass(Clan.class,"Clan");

        utilityMethods = new UtilityMethods(this);

        filesManager = new FilesManager(this);

        clansManager = new ClansManager(this);
        clanChat = new ClanChat(this);

        getServer().getPluginCommand("clans").setExecutor(new Commands(this));

        getServer().getPluginManager().registerEvents(new InventoryCloseListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(this), this);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlayerHolders(this).register();
        }

    }
    public void onDisable() {
        if (clansManager == null) {
            //To avoid NPE when disabling the plugin
            return;
        }
        for (Clan clan : clansManager.getClans().values()) {
            clan.save();
        }
    }

    private ClansManager clansManager;
    private FilesManager filesManager;
    private UtilityMethods utilityMethods;
    private ClanChat clanChat;

    public ClanChat getClanChat() {
        return clanChat;
    }
    public UtilityMethods getUtilityMethods() {
        return utilityMethods;
    }
    public FilesManager getFilesManager() {
        return filesManager;
    }
    public ClansManager getClansManager() {
        return clansManager;
    }
}