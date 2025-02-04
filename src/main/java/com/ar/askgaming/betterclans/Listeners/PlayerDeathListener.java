package com.ar.askgaming.betterclans.Listeners;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.ar.askgaming.betterclans.BetterClans;
import com.ar.askgaming.betterclans.Clan.Clan;

public class PlayerDeathListener implements Listener{

    private BetterClans plugin;

    public PlayerDeathListener(BetterClans plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private HashMap<Player, Long> cooldown = new HashMap<>();

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player p = e.getEntity();
        Clan clan = plugin.getClansManager().getClanByPlayer(p);

        long cd = plugin.getConfig().getLong("points.protection_time",15)*1000*60;
        if (cooldown.containsKey(p)){
            if (System.currentTimeMillis() - cooldown.get(p) < cd){
                return;
            }
        }
        cooldown.put(p, System.currentTimeMillis());
        
        if (clan != null){
            clan.setPoints(clan.getPoints()-1);
            clan.save();
        }
        if (p.getKiller() != null){
            Player killer = p.getKiller();
            Clan killerClan = plugin.getClansManager().getClanByPlayer(killer);
            if (killerClan != null){
                killerClan.setPoints(killerClan.getPoints()+1);
                killerClan.save();
            }
        }
    }
    
}
