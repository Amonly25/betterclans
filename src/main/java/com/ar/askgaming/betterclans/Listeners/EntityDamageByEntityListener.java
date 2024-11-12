package com.ar.askgaming.betterclans.Listeners;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.ar.askgaming.betterclans.BetterClans;

public class EntityDamageByEntityListener implements Listener {

    private BetterClans plugin;

    public EntityDamageByEntityListener(BetterClans plugin) {
        this.plugin = plugin;
    }

    private HashMap<Player, Long> lastHit = new HashMap<>();
    private long cooldownTime = 50000; // 50 segundos de cooldown

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player damaged = (Player) event.getEntity();

            if (plugin.getClansManager().isAlly(damager, damaged)) {
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
