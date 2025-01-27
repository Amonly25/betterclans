package com.ar.askgaming.betterclans.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.ar.askgaming.betterclans.BetterClans;

public class InventoryClickListener implements Listener{

    private BetterClans plugin;
    public InventoryClickListener(BetterClans plugin){
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onRewardsGuid(InventoryClickEvent e){
       
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();
        Inventory actual = e.getInventory();
        Inventory check = plugin.getClanShop().getInv();
        if (check.equals(actual)){
            e.setCancelled(true);

            ItemStack item = e.getCurrentItem();
            if (item == null || item.getType().equals(Material.AIR)) {
                return;
            }
 
            int slot = e.getSlot();

            plugin.getClanShop().readItemToBuy(p, slot);            

        }
    }
}
