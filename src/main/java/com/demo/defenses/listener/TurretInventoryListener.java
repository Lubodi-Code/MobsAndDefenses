package com.demo.defenses.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

import com.demo.defenses.manager.TurretManager;
import com.demo.defenses.model.Turret;
import com.demo.defenses.model.TurretInventoryHolder;


/**
 * Handles turret inventory interactions - now requires manual ammo loading.
 */
public class TurretInventoryListener implements Listener {

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        if (!(e.getInventory().getHolder() instanceof TurretInventoryHolder holder)) return;
        Turret turret = TurretManager.getTurret(holder.getStandId());
        if (turret == null) return;
        // Ya no llenamos automáticamente el inventario
        // El jugador debe agregar manualmente la munición apropiada

        // Opcional: Mostrar mensaje sobre qué munición necesita
        if (e.getPlayer() != null) {
            String ammoType = switch(turret.getType()) {
                case DISPENSER -> "§eFlecha";
                case DROPPER -> "§7Piedra";
                case OBSERVER -> "§8Carbón";
                case CRAFTER -> "§cRedstone";
            };
            e.getPlayer().sendMessage("§6Esta torreta usa munición de tipo: " + ammoType);
        }
    }
}
