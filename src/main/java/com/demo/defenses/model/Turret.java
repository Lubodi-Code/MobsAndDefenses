package com.demo.defenses.model;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Represents an in world turret.
 */
public class Turret {
    private final UUID standId;
    private final TurretType type;
    private boolean active;
    private long lastShot;

    public Turret(UUID standId, TurretType type) {
        this.standId = standId;
        this.type = type;
    }

    public UUID getStandId() {
        return standId;
    }

    public TurretType getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    public void toggleActive() {
        this.active = !this.active;
    }

    public boolean isReady() {
        return System.currentTimeMillis() - lastShot >= type.getReloadTime();
    }

    public void fire(Player controller, ArmorStand stand) {
        if (!isReady()) {
            controller.sendMessage("§6Torreta recargando... (" +
                ((type.getReloadTime() - (System.currentTimeMillis() - lastShot)) / 1000.0) + "s)");
            return;
        }

        // Obtener la posición de disparo desde la cabeza del ArmorStand
        Location shootLocation = stand.getEyeLocation();

        // Usar la dirección exacta de donde está mirando el jugador
        Vector direction = controller.getEyeLocation().getDirection().normalize();

        // Crear la flecha
        Arrow arrow = stand.getWorld().spawnArrow(shootLocation, direction, 2.0f, 0.1f);
        arrow.setShooter(stand);

        // Configurar propiedades de la flecha según el tipo de torreta
        switch (type) {
            case DISPENSER:
                arrow.setDamage(4.0);
                break;
            case DROPPER:
                arrow.setDamage(2.0);
                break;
            case OBSERVER:
                arrow.setDamage(6.0);
                arrow.setFireTicks(100);
                break;
            case CRAFTER:
                arrow.setDamage(8.0);
                arrow.setCritical(true);
                break;
        }

        // Actualizar tiempo del último disparo
        lastShot = System.currentTimeMillis();

        // Mensaje al jugador
        controller.sendMessage("§e¡Disparo! Tipo: " + type.name());

        // Efecto de sonido
        stand.getWorld().playSound(shootLocation, org.bukkit.Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
    }

    /**
     * Convenience method to get the armor stand if still present.
     */
   public ArmorStand getArmorStand() {
    Entity e = Bukkit.getServer().getEntity(standId);
    if (e instanceof ArmorStand as) {
        return as;
    }
    return null;
}
}
