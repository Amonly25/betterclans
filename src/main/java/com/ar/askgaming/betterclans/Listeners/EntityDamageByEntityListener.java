package com.ar.askgaming.betterclans.Listeners;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.ar.askgaming.betterclans.BetterClans;
import com.ar.askgaming.betterclans.Clan.Clan;
import com.ar.askgaming.betterclans.Managers.ClansManager;

public class EntityDamageByEntityListener implements Listener {

    private BetterClans plugin;
    private ClansManager clansManager;

    public EntityDamageByEntityListener(BetterClans plugin) {
        this.plugin = plugin;
        this.clansManager = plugin.getClansManager();
    }

    private HashMap<Player, Long> lastHit = new HashMap<>();
    private long cooldownTime = 50000; // 50 segundos de cooldown

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player damaged = (Player) event.getEntity();

            List<String> disabledWorlds = plugin.getConfig().getStringList("disabled_worlds");
            for (String world : disabledWorlds) {
                if (damager.getWorld().getName().equalsIgnoreCase(world)) {
                    return;
                }
            }

            Clan damagerClan = clansManager.getClanByPlayer(damager);
            Clan damagedClan = clansManager.getClanByPlayer(damaged);
            if (damagerClan == null || damagedClan == null) {
                return;
            }
            if (clansManager.isInWarWith(damagerClan, damagedClan) || clansManager.isInWarWith(damagedClan, damagerClan)) {
                if (plugin.getConfig().getBoolean("war.override_pvp",true)){
                    event.setCancelled(false);
                    return;
                }
            }

            if (clansManager.isAlly(damager, damaged)) {
                event.setCancelled(true);

               // Añadir un mensaje de cooldown
                long lastHitTime = lastHit.getOrDefault(damager, 0L);

                // Calcular el tiempo transcurrido desde el último golpe
                long timeSinceLastHit = System.currentTimeMillis() - lastHitTime;

                if (timeSinceLastHit > cooldownTime) {
                    // Si el tiempo transcurrido es menor que el cooldown, enviar el mensaje y actualizar el último golpe
                    damager.sendMessage(plugin.getFilesManager().getLang("misc.cant_damage_ally", damaged));
                    lastHit.put(damager, System.currentTimeMillis());
                }
            }   
        }
    }
}
