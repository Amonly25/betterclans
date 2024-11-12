package com.ar.askgaming.betterclans.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.ar.askgaming.betterclans.BetterClans;
import com.ar.askgaming.betterclans.Clan.Clan;

public class InventoryCloseListener implements Listener{

    private BetterClans plugin;
    public InventoryCloseListener(BetterClans plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){

        Player p = (Player) e.getPlayer();
        Clan clan = plugin.getClansManager().getClanByPlayer(p);
        if (clan == null) return;
        if (e.getInventory().equals(clan.getInventory())){
            clan.save();
        }       
    }  
}
