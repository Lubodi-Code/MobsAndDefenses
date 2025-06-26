package com.demo.defenses.listener;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import com.demo.defenses.manager.TurretManager;
import com.demo.defenses.model.Turret;
import com.demo.defenses.model.TurretType;

/**
 * Fills turret inventory with basic ammo depending on type.
 */
public class TurretInventoryListener implements Listener {

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        if (!(e.getInventory().getHolder() instanceof ArmorStand stand)) return;
        Turret turret = TurretManager.getTurret(stand.getUniqueId());
        if (turret == null) return;
        Inventory inv = e.getInventory();
        inv.clear();
        TurretType type = turret.getType();
        Material mat = switch(type) {
            case DISPENSER -> Material.ARROW;
            case DROPPER -> Material.COBBLESTONE;
            case OBSERVER -> Material.COAL;
            case CRAFTER -> Material.REDSTONE;
        };
        inv.addItem(new org.bukkit.inventory.ItemStack(mat, inv.getSize()));
    }
}
