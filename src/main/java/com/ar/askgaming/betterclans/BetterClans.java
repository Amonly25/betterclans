package com.ar.askgaming.betterclans;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.ar.askgaming.betterclans.Clan.Clan;
import com.ar.askgaming.betterclans.Clan.ClanChat;
import com.ar.askgaming.betterclans.Clan.ClanShop;
import com.ar.askgaming.betterclans.Listeners.AsyncPlayerChatListener;
import com.ar.askgaming.betterclans.Listeners.EntityDamageByEntityListener;
import com.ar.askgaming.betterclans.Listeners.InventoryClickListener;
import com.ar.askgaming.betterclans.Listeners.InventoryCloseListener;
import com.ar.askgaming.betterclans.Listeners.PlayerDeathListener;
import com.ar.askgaming.betterclans.Listeners.PlayerMoveListener;
import com.ar.askgaming.betterclans.Managers.ClansManager;
import com.ar.askgaming.betterclans.Managers.FilesManager;

import net.milkbowl.vault.economy.Economy;

public class BetterClans extends JavaPlugin {

    public void onEnable() {

        saveDefaultConfig();
        instance = this;

        ConfigurationSerialization.registerClass(Clan.class,"Clan");

        utilityMethods = new UtilityMethods(this);

        filesManager = new FilesManager(this);

        clansManager = new ClansManager(this);
        clanChat = new ClanChat(this);
        clanShop = new ClanShop(this);

        getServer().getPluginCommand("clans").setExecutor(new Commands(this));

        getServer().getPluginManager().registerEvents(new InventoryCloseListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(this), this);

        new InventoryClickListener(this);
        new PlayerDeathListener(this);
        new PlayerMoveListener(this);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlayerHolders(this).register();
        }

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            getLogger().info("Vault found!");
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                getLogger().info("Non economy plugin found!");
            } else {
                vaultEconomy = rsp.getProvider();
                getLogger().info("Vault Economy found!");
            }

        } else {
            getLogger().info("Vault not found!");
            return;
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
    private Economy vaultEconomy;
    private ClanShop clanShop;
    private static BetterClans instance;

    public void setClanShop(ClanShop clanShop) {
        this.clanShop = clanShop;
    }
    public ClanShop getClanShop() {
        return clanShop;
    }
    public Economy getVaultEconomy() {
        return vaultEconomy;
    }

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
    public static BetterClans getInstance() {
        return instance;
    }
}