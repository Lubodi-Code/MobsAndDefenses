package com.demo.defenses.model;

import java.util.UUID;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Custom InventoryHolder to associate an inventory with a turret armor stand.
 */
public class TurretInventoryHolder implements InventoryHolder {
    private final UUID standId;
    private Inventory inventory;

    public TurretInventoryHolder(UUID standId) {
        this.standId = standId;
    }

    public UUID getStandId() {
        return standId;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
