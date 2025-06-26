package com.demo.defenses.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
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
            // ArmorStand ahora es visible y vulnerable
            as.setVisible(true);           // Visible
            as.setMarker(false);          // No es marker (puede ser golpeado)
            as.setGravity(true);          // Tiene gravedad
            as.setBasePlate(true);        // Muestra la base
            as.setArms(false);            // Sin brazos por defecto
            as.setSmall(true);           // Tamaño normal
            
            // Configuración de persistencia
            as.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, type.name());
            
            // Equipamiento visual
            as.getEquipment().setHelmet(new ItemStack(type.getBlock()));
            
            // Hacer que el ArmorStand sea menos frágil (opcional)
            as.setHealth(20.0); // 10 corazones de vida
        });

        Turret turret = new Turret(stand.getUniqueId(), type);
        register(turret);
        return stand;
    }

    /**
     * Destruye la torreta y la elimina del registro
     */
    public static void destroyTurret(UUID id) {
        Turret turret = TURRETS.remove(id);
        if (turret != null) {
            ArmorStand stand = turret.getArmorStand();
            if (stand != null) {
                stand.remove();
            }
        }
    }
}