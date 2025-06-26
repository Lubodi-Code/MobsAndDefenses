package com.demo.defenses.crafting;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.demo.defenses.manager.TurretManager;
import com.demo.defenses.model.TurretType;

/**
 * Registers crafting recipes for turrets.
 */
public class TurretCrafting {
    private final org.bukkit.plugin.Plugin plugin;

    public TurretCrafting(org.bukkit.plugin.Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerRecipes() {
        for (TurretType type : TurretType.values()) {
            ItemStack result = new ItemStack(Material.DISPENSER);
            ItemMeta meta = result.getItemMeta();
            meta.setDisplayName(type.name() + " Turret");
            meta.getPersistentDataContainer().set(TurretManager.getKey(), PersistentDataType.STRING, type.name());
            result.setItemMeta(meta);

            NamespacedKey key = new NamespacedKey(plugin, "turret_" + type.name().toLowerCase());
            ShapelessRecipe recipe = new ShapelessRecipe(key, result);
            recipe.addIngredient(Material.ARMOR_STAND);
            recipe.addIngredient(type.getBlock());
            Bukkit.addRecipe(recipe);
        }
    }
}
