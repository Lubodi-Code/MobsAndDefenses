package com.demo.defenses.listener;

import java.util.UUID;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import com.demo.defenses.manager.TurretManager;
import com.demo.defenses.model.Turret;

/**
 * Handles interacting with a placed turret.
 */
public class TurretInteractListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof ArmorStand stand)) return;
        UUID id = stand.getUniqueId();
        Turret turret = TurretManager.getTurret(id);
        if (turret == null) return;
        e.setCancelled(true);
        Player player = e.getPlayer();
        if (player.isSneaking()) {
            turret.toggleActive();
            player.sendMessage("Modo torreta: " + (turret.isActive() ? "activado" : "desactivado"));
            return;
        }
        if (turret.isActive()) {
            turret.fire(player, stand);
        }
    }
}
