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
        if (!isReady()) return;
        Location eye = stand.getEyeLocation();
        Vector dir = controller.getEyeLocation().getDirection();
        Arrow arrow = stand.getWorld().spawnArrow(eye, dir, 1f, 0f);
        arrow.setShooter(stand);
        lastShot = System.currentTimeMillis();
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
