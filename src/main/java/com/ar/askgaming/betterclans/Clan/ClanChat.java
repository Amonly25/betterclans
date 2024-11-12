package com.ar.askgaming.betterclans.Clan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.checkerframework.checker.units.qual.C;

import com.ar.askgaming.betterclans.BetterClans;

public class ClanChat {

    private BetterClans plugin;
    public ClanChat(BetterClans plugin) {
        this.plugin = plugin;
    }

    public enum ChatType {
        CLAN, GLOBAL, ALLY
    }

    private HashMap<Player, ChatType> chatType = new HashMap<>();

    public HashMap<Player, ChatType> getChatType() {
        return chatType;
    }
    public void setChatType(Player player, ChatType type) {
        chatType.put(player, type);
    }

    public void broadcastGlobalMessage(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Clan clan = plugin.getClansManager().getClanByPlayer(player);
        if (!plugin.getConfig().getBoolean("chat_feature.enable")){
            return;
        }
        String format = plugin.getConfig().getString("chat_feature.global");
        String pName = player.getDisplayName();
        String clanName = "";
        if (clan != null) {
            clanName = clan.getTag();
        }
        String colored = ChatColor.translateAlternateColorCodes('&', format);
        event.setFormat(colored.replace("{player}", pName).replace("{clan}", clanName).replace("{message}", event.getMessage()));

    }
    public void broadCastClanMessage(ChatType chatType, Player player, String message) {
        Clan clan = plugin.getClansManager().getClanByPlayer(player);

        String format = plugin.getConfig().getString("chat_feature."+chatType.toString().toLowerCase());
        format = ChatColor.translateAlternateColorCodes('&', format);
        String pName = player.getName();

        List<Player> players = new ArrayList<>();
        switch (chatType) {
            case ALLY:
                players = plugin.getClansManager().getAllAlliedClanMembers(clan);
                break;
        
            default:
                players = plugin.getClansManager().getAllClanMembers(clan);
                break;
        }
        // Send message to all online ops
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp() && !players.contains(p)) {
                players.add(p);
            }
        }
        // Send message to all players in the list
        for (Player p : players) {
            p.sendMessage(format.replace("{player}", pName).replace("{message}", message));
        }
        plugin.getLogger().log(Level.INFO, "Clan chat: " +pName + ": " +message);
    }
}
