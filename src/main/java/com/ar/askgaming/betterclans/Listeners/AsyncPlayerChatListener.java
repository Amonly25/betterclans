package com.ar.askgaming.betterclans.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.ar.askgaming.betterclans.BetterClans;
import com.ar.askgaming.betterclans.Clan.ClanChat;
import com.ar.askgaming.betterclans.Clan.ClanChat.ChatType;

public class AsyncPlayerChatListener implements Listener {

    private BetterClans plugin;
    public AsyncPlayerChatListener(BetterClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        ClanChat chat = plugin.getClanChat();
        ChatType chatType = chat.getChatType().getOrDefault(player, ChatType.GLOBAL);
        switch (chatType) {
            case CLAN:
            case ALLY:
                event.setCancelled(true);
                chat.broadCastClanMessage(chatType, player, message);
                break;
            default:
                chat.broadcastGlobalMessage(event);
                break;
        }
    }
}
