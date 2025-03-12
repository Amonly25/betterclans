package com.ar.askgaming.betterclans.Clan;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.ar.askgaming.betterclans.BetterClans;

import me.clip.placeholderapi.PlaceholderAPI;

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
        
        if (!plugin.getConfig().getBoolean("chat_feature.enable")) {
            return;
        }
        
        String format = plugin.getConfig().getString("chat_feature.global");
        String pName = player.getDisplayName();
        String clanName = (clan != null) ? clan.getTag() : "";
        
        // Escapa el mensaje para evitar errores de formato
        String escapedMessage = event.getMessage().replace("%", "%%");
        if (player.hasPermission("betterclans.chat.color")) {
            escapedMessage = ChatColor.translateAlternateColorCodes('&', escapedMessage);
        }
        
        // Reemplaza los placeholders en el formato
        format = format.replace("{player}", pName)
                       .replace("{clan}", clanName);
        
        // Aplica traducción de colores y placehoders
        String coloredFormat = ChatColor.translateAlternateColorCodes('&', format);
        coloredFormat = PlaceholderAPI.setPlaceholders(player, coloredFormat);
        
        // Usa setFormat con la cadena corregida
        event.setFormat(coloredFormat + escapedMessage);
    }
    
    public void broadCastClanMessage(ChatType chatType, Player player, String message) {
        Clan clan = plugin.getClansManager().getClanByPlayer(player);

        if (clan == null) {
            player.sendMessage(plugin.getFilesManager().getLang("clan.no_clan", player));
            this.chatType.put(player, ChatType.GLOBAL);
            return;
        }

        String format = plugin.getConfig().getString("chat_feature."+chatType.toString().toLowerCase());
        format = ChatColor.translateAlternateColorCodes('&', format);
        String pName = player.getName();

        List<Player> players = plugin.getClansManager().getAllClanMembers(clan);

        if (chatType == ChatType.ALLY) {
            plugin.getClansManager().getAllAlliedClanMembers(clan).forEach(p -> players.add(p));

        }

        // Send message to all online ops
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("betterclans.admin")) {
                p.sendMessage("§8Clan Chat Whisper: " +pName + ": " +message);
            }
        }
        // Send message to all players in the list
        for (Player p : players) {
            p.sendMessage(format.replace("{player}", pName).replace("{message}", message));
        }
        plugin.getLogger().log(Level.INFO, "Clan chat: " +pName + ": " +message);
    }
}
