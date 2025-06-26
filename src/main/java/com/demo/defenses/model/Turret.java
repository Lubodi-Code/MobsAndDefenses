package com.demo.defenses.model;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Represents an in world turret.
 */
public class Turret {
    private final UUID standId;
    private final TurretType type;
    private final Inventory inventory;
    private boolean active;
    private long lastShot;

    public Turret(UUID standId, TurretType type, Inventory inventory) {
        this.standId = standId;
        this.type = type;
        this.inventory = inventory;
        this.active = false; // Inicialmente desactivada
    }

    public Inventory getInventory() {
        return inventory;
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

    public void setActive(boolean active) {
        this.active = active;
    }

    public void toggleActive() {
        this.active = !this.active;
    }

    public boolean isReady() {
        return System.currentTimeMillis() - lastShot >= type.getReloadTime();
    }

    /**
     * Obtiene el tipo de munición que usa esta torreta
     */
    public Material getAmmoType() {
        return switch (type) {
            case DISPENSER -> Material.ARROW;
            case DROPPER -> Material.COBBLESTONE;
            case OBSERVER -> Material.COAL;
            case CRAFTER -> Material.REDSTONE;
        };
    }

    /**
     * Verifica si hay munición en el inventario de la torreta
     */
    public boolean hasAmmo() {
        Inventory inventory = this.inventory;
        Material ammoType = getAmmoType();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == ammoType && item.getAmount() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Consume una unidad de munición
     */
    private boolean consumeAmmo() {
        Inventory inventory = this.inventory;
        Material ammoType = getAmmoType();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == ammoType && item.getAmount() > 0) {
                if (item.getAmount() == 1) {
                    inventory.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Devuelve la munición restante en el inventario
     */
    public int getRemainingAmmo() {
        Inventory inventory = this.inventory;
        Material ammoType = getAmmoType();
        int total = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == ammoType) {
                total += item.getAmount();
            }
        }
        return total;
    }

    public void fire(Player controller, ArmorStand stand) {
        if (!isReady()) {
            double remainingTime = (type.getReloadTime() - (System.currentTimeMillis() - lastShot)) / 1000.0;
            controller.sendMessage("§6Torreta recargando... (" + String.format("%.1f", remainingTime) + "s)");
            return;
        }

        // Verificar munición disponible
        if (!hasAmmo()) {
            String ammoName = switch (getAmmoType()) {
                case ARROW -> "flechas";
                case COBBLESTONE -> "piedra";
                case COAL -> "carbón";
                case REDSTONE -> "redstone";
                default -> "munición";
            };
            controller.sendMessage("§c¡Sin munición! La torreta necesita " + ammoName + ".");
            return;
        }

        if (!consumeAmmo()) {
            controller.sendMessage("§cError al consumir munición.");
            return;
        }

        // Obtener la posición de disparo desde la cabeza del ArmorStand
        Location shootLocation = stand.getEyeLocation();

        // Usar la dirección exacta de donde está mirando el jugador
        Vector direction = controller.getEyeLocation().getDirection().normalize();

        // Verificar que el disparo está dentro del rango permitido
        if (!isDirectionValid(controller, stand)) {
            controller.sendMessage("§cNo puedes disparar en esa dirección - fuera del rango de la torreta");
            return;
        }

        double speed = 3.0; // projectiles travel faster now

        switch (type) {
            case DISPENSER -> {
                Arrow arr = stand.getWorld().spawnArrow(shootLocation, direction, (float) speed, 0f);
                arr.setShooter(stand);
                arr.setDamage(4.0);
            }
            case DROPPER -> {
                var stone = stand.getWorld().spawn(shootLocation, org.bukkit.entity.Snowball.class);
                stone.setItem(new ItemStack(Material.COBBLESTONE));
                stone.setShooter(stand);
                stone.setVelocity(direction.multiply(speed));
                stone.setMetadata("turret-damage", new org.bukkit.metadata.FixedMetadataValue(com.demo.managers.PluginManager.getInstance().getPlugin(), 2.0));
            }
            case OBSERVER -> {
                var fireball = stand.getWorld().spawn(shootLocation, org.bukkit.entity.SmallFireball.class);
                fireball.setShooter(stand);
                fireball.setDirection(direction);
                fireball.setVelocity(direction.multiply(speed));
                fireball.setIsIncendiary(true);
                fireball.setMetadata("turret-damage", new org.bukkit.metadata.FixedMetadataValue(com.demo.managers.PluginManager.getInstance().getPlugin(), 6.0));
            }
            case CRAFTER -> {
                Arrow laser = stand.getWorld().spawnArrow(shootLocation, direction, (float) speed, 0f);
                laser.setShooter(stand);
                laser.setDamage(10.0);
                laser.setCritical(true);
            }
        }

        // Actualizar tiempo del último disparo
        lastShot = System.currentTimeMillis();

        // Mensaje al jugador con munición restante
        int remaining = getRemainingAmmo();
        controller.sendMessage("§e¡Disparo! §7(" + remaining + " munición restante)");

        // Efectos de sonido y visuales
        stand.getWorld().playSound(shootLocation, org.bukkit.Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);

        // Efecto de partículas en la cabeza de la torreta
        stand.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, shootLocation, 5, 0.1, 0.1, 0.1, 0.05);
    }

    /**
     * Verifica si la dirección del disparo es válida (dentro del rango de la torreta)
     */
    private boolean isDirectionValid(Player controller, ArmorStand stand) {
        Location playerLoc = controller.getEyeLocation();
        Location turretLoc = stand.getEyeLocation();

        // Calcular la orientación real de la cabeza de la torreta
        float baseYaw = turretLoc.getYaw();
        float headOffset = (float) Math.toDegrees(stand.getHeadPose().getY());
        float turretYaw = baseYaw + headOffset;

        // Diferencia entre la vista del jugador y la de la torreta
        float playerYaw = playerLoc.getYaw();
        float yawDiff = Math.abs(normalizeAngle(playerYaw - turretYaw));

        // Verificar si está dentro del rango horizontal permitido
        if (yawDiff > 45.0) {
            return false;
        }

        // Direcciones para el producto punto
        Location turretLook = turretLoc.clone();
        turretLook.setYaw(turretYaw);

        Vector playerDirection = playerLoc.getDirection();
        Vector turretDirection = turretLook.getDirection();

        // Si es positivo, están mirando en la misma dirección general
        return playerDirection.dot(turretDirection) > 0;
    }

    /**
     * Normaliza un ángulo para que esté entre -180 y 180
     */
    private float normalizeAngle(float angle) {
        while (angle > 180f) angle -= 360f;
        while (angle < -180f) angle += 360f;
        return angle;
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

    /**
     * Obtiene información detallada de la torreta
     */
    public String getInfo() {
        return String.format("§6Torreta %s§r - Estado: %s - Recarga: %.1fs",
            type.name(),
            active ? "§aActiva" : "§cInactiva",
            type.getReloadTime() / 1000.0);
    }
}
