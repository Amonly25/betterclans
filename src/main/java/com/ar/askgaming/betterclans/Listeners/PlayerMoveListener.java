package com.ar.askgaming.betterclans.Listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.ar.askgaming.betterclans.BetterClans;
import com.ar.askgaming.betterclans.Clan.Clan;
import com.ar.askgaming.betterclans.Managers.ClansManager;

public class PlayerMoveListener implements Listener{

    private final BetterClans plugin;
    private final ClansManager manager;
    public PlayerMoveListener(BetterClans plugin){
        this.plugin = plugin;
        this.manager = plugin.getClansManager();
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
        Clan clan = manager.getClanByPlayer(p);
        if (clan == null){
            return;
        }
        List<Clan> war = manager.getWarsWith(clan);
        if (war.isEmpty()){
            return;
        }
        List<Player> nearbyPlayers = new ArrayList<>();
        Integer radius = plugin.getConfig().getInt("war.alert_player_distance", 32);
        for (Player player : p.getWorld().getPlayers()) {
            if (player.getLocation().distance(p.getLocation()) < radius) {
                nearbyPlayers.add(player);
            }
        }
        if (nearbyPlayers.isEmpty()){
            return;
        }
        List<Player> playersInWar = new ArrayList<>();
        for (Clan c : war){
            manager.getAllClanMembers(c).forEach(m -> {
                if (nearbyPlayers.contains(m)){
                    playersInWar.add(m);
                }
            });
        }
        if (playersInWar.isEmpty()){
            return;
        }
        if (System.currentTimeMillis() - cooldown.getOrDefault(p, 0L) < 10000){
            return;
        }
        cooldown.put(p, System.currentTimeMillis());
        StringBuilder sb = new StringBuilder();
        for (Player player : playersInWar){
            sb.append(player.getName()).append(",");
        }
        p.sendMessage(plugin.getFilesManager().getLang("war.near", p).replace("{player}", sb.toString()));
    }
}
