package com.demo.defenses.model;

import org.bukkit.Material;

/**
 * Defines the available turret types and their properties.
 */
public enum TurretType {
    DISPENSER(Material.DISPENSER, 5_000L),
    DROPPER(Material.DROPPER, 500L),
    OBSERVER(Material.OBSERVER, 8_000L),
    CRAFTER(Material.CRAFTER, 30_000L);

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
