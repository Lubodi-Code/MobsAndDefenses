package com.demo.defenses.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

/**
 * Simple inventory provider for turrets.
 */
public class TurretMenuProvider {
    public static Inventory createMenu(String title, int size) {
        return Bukkit.createInventory(null, size, title);
    }
}
