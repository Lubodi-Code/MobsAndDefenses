package com.demo.defenses.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.demo.defenses.model.Turret;
import com.demo.defenses.model.TurretType;

/**
 * Central registry for spawned turrets.
 */
public class TurretManager {
    private static final Map<UUID, Turret> TURRETS = new HashMap<>();
    private static NamespacedKey KEY;

    public static void setup(org.bukkit.plugin.Plugin plugin) {
        KEY = new NamespacedKey(plugin, "turret_type");
    }

    public static NamespacedKey getKey() {
        return KEY;
    }

    public static void register(Turret turret) {
        TURRETS.put(turret.getStandId(), turret);
    }

    public static void unregister(UUID id) {
        TURRETS.remove(id);
    }

    public static Turret getTurret(UUID id) {
        return TURRETS.get(id);
    }

    public static ArmorStand spawnTurret(Player player, TurretType type) {
        Location loc = player.getLocation();
        ArmorStand stand = player.getWorld().spawn(loc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setMarker(true);
            as.setGravity(false);
            as.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, type.name());
            as.getEquipment().setHelmet(new ItemStack(type.getBlock()));
        });

        Turret turret = new Turret(stand.getUniqueId(), type);
        register(turret);
        return stand;
    }
}
