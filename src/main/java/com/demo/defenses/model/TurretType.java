package com.demo.defenses.model;

import org.bukkit.Material;

/**
 * Defines the available turret types and their properties.
 */
public enum TurretType {
    // Reduced reload times for faster firing
    DISPENSER(Material.DISPENSER, 2_000L),
    DROPPER(Material.DROPPER, 250L),
    OBSERVER(Material.OBSERVER, 4_000L),
    CRAFTER(Material.CRAFTER, 15_000L);

    private final Material block;
    private final long reloadTime;

    TurretType(Material block, long reloadTime) {
        this.block = block;
        this.reloadTime = reloadTime;
    }

    public Material getBlock() {
        return block;
    }

    public long getReloadTime() {
        return reloadTime;
    }
}
