package com.ar.askgaming.betterclans.Listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.ar.askgaming.betterclans.BetterClans;
import com.ar.askgaming.betterclans.Clan.Clan;

public class PlayerMoveListener implements Listener{

    private final BetterClans plugin;
    public PlayerMoveListener(BetterClans plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private HashMap<Player, Long> cooldown = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        Player p = e.getPlayer();
        Location from = e.getFrom();
        Location to = e.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ() && from.getBlockY() == to.getBlockY()){
            return;
        }
        Clan clan = plugin.getClansManager().getClanByPlayer(p);
        if (clan == null){
            return;
        }
        List<Clan> war = plugin.getClansManager().getWarsWith(clan);
        if (war.isEmpty()){
            return;
        }
        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player player : p.getWorld().getPlayers()) {
            if (player.getLocation().distance(p.getLocation()) < 32) {
                nearbyPlayers.add(player);
            }
        }
        if (nearbyPlayers.isEmpty()){
            return;
        }
        List<Player> playersInWar = new ArrayList<>();
        for (Clan c : war){
            List<UUID> members = c.getMembers();
            for (Player player : nearbyPlayers){
                if (members.contains(player.getUniqueId())){
                    playersInWar.add(player);
                }
            }
        }
        if (playersInWar.isEmpty()){
            return;
        }
        if (System.currentTimeMillis() - cooldown.getOrDefault(p, 0L) < 10000){
            return;
        }
        cooldown.put(p, System.currentTimeMillis());
        for (Player player : playersInWar){
            p.sendMessage(plugin.getFilesManager().getLang("war.near_player", p).replace("{player}", player.getDisplayName()));
        }
    }
}
