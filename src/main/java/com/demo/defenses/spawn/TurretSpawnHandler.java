package com.demo.defenses.spawn;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.demo.defenses.manager.TurretManager;
import com.demo.defenses.model.TurretType;

/**
 * Handles player placing turret spawn items.
 */
public class TurretSpawnHandler implements Listener {
    private final NamespacedKey key;

    public TurretSpawnHandler(org.bukkit.plugin.Plugin plugin) {
        this.key = TurretManager.getKey();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getHand() != EquipmentSlot.HAND) return;
        PersistentDataContainer pdc = e.getItem().getItemMeta().getPersistentDataContainer();
        String typeName = pdc.getOrDefault(key, PersistentDataType.STRING, null);
        if (typeName == null) return;
        e.setCancelled(true);
        Player player = e.getPlayer();
        TurretType type = TurretType.valueOf(typeName);
        TurretManager.spawnTurret(player, type);
        ItemStack hand = e.getItem();
        if (hand.getAmount() > 1) hand.setAmount(hand.getAmount() - 1); else player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
    }
}
