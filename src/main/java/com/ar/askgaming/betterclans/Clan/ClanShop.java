package com.ar.askgaming.betterclans.Clan;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ar.askgaming.betterclans.BetterClans;

public class ClanShop {

    private String name;
    private Integer size;
    private Inventory inv;

    private BetterClans plugin;
    public ClanShop(BetterClans plugin){
        this.plugin = plugin;

        this.name = plugin.getConfig().getString("shop.name", "Clan Shop");
        this.size = plugin.getConfig().getInt("shop.size", 27);

        inv = plugin.getServer().createInventory(null, size, name);

        loadItems();
    }

    public Inventory getInv() {
        return inv;
    }

    private void loadItems(){

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("shop.items");
        if (section == null) return;

        for (String key : section.getKeys(false)){

            String material = plugin.getConfig().getString("shop.items." + key + ".material");
            String name = plugin.getConfig().getString("shop.items." + key + ".name");
            List<String> lore = plugin.getConfig().getStringList("shop.items." + key + ".lore");
            Integer slot = plugin.getConfig().getInt("shop.items." + key + ".slot");
            Integer cost = plugin.getConfig().getInt("shop.items." + key + ".cost");
    
            ItemStack item;
            try {
                item = new ItemStack(Material.valueOf(material));
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid material in gui." + key + ".material");
                item = new ItemStack(Material.STONE);
                return;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

            meta.setDisplayName(name.replace('&', '§'));

            List<String> newLore = new ArrayList<>();
            for (String line : lore) {
                newLore.add(colorize(line.replace("%cost%", cost.toString())));
            }

            meta.setLore(newLore);
            item.setItemMeta(meta);

            inv.setItem(slot, item);
              
            }
        }
    private String colorize(String text) {
        return text != null ? text.replace("&", "§") : "";
    }

    public void readItemToBuy(Player player, Integer clickedSlot) {

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("shop.items");
        if (section == null) return;

        Clan clan = plugin.getClansManager().getClanByPlayer(player);
        if (clan == null) return;

        double balance = clan.getBalance();

        for (String key : section.getKeys(false)){

            Integer slot = plugin.getConfig().getInt("shop.items." + key + ".slot");
            if (slot != clickedSlot) continue;

            Integer cost = plugin.getConfig().getInt("shop.items." + key + ".cost",100);
            if (balance < cost) {
                player.sendMessage(plugin.getFilesManager().getLang("economy.not_enough_clan", player).replace("{amount}", cost.toString()));
                return;
            }
            String name = plugin.getConfig().getString("shop.items." + key + ".name", "Unknown");
            name = name.replace('&', '§');
            List<String> commands = plugin.getConfig().getStringList("shop.items." + key + ".commands");

            for (Player p : plugin.getClansManager().getAllClanMembers(clan)){
                p.sendMessage(plugin.getFilesManager().getLang("economy.bought", p).replace("{player}", player.getName()).replace("{name}", name));
                for (String command : commands) {
                    command = command.replace("{all_clan}", p.getName()).replace("{clan}", clan.getName());
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                    plugin.getLogger().info("Dispatched command: " + command);
                }
            }
            
            clan.setBalance(balance - cost);
            clan.save();
            return;
        }  
    }
}
