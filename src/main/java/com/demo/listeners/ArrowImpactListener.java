package com.demo.listeners;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import net.minecraft.nbt.CompoundTag;

/**
 * Listener to handle special arrow impacts like explosions and fire.
 */
public class ArrowImpactListener implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) {
            return;
        }

        CraftArrow craftArrow = (CraftArrow) arrow;
        net.minecraft.world.entity.projectile.Arrow nmsArrow = craftArrow.getHandle();
        CompoundTag nbt = nmsArrow.getPersistentData();
        Location hitLocation = arrow.getLocation();

        if (nbt.getBoolean("explosive")) {
            float explosionPower = nbt.getFloat("explosionPower");
            hitLocation.getWorld().createExplosion(hitLocation, explosionPower, false, false);
            hitLocation.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, hitLocation, 1);
            hitLocation.getWorld().playSound(hitLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            arrow.remove();
        } else if (nbt.getBoolean("fireArrow")) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Location fireLocation = hitLocation.clone().add(x, 0, z);
                    if (fireLocation.getBlock().getType().isAir()
                            && fireLocation.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                        fireLocation.getBlock().setType(org.bukkit.Material.FIRE);
                    }
                }
            }
            hitLocation.getWorld().spawnParticle(Particle.FLAME, hitLocation, 10, 0.5, 0.5, 0.5, 0.1);
            hitLocation.getWorld().playSound(hitLocation, Sound.ITEM_FIRECHARGE_USE, 0.5f, 1.0f);
        }
    }

    @EventHandler
    public void onEntityDamageByArrow(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        CraftArrow craftArrow = (CraftArrow) arrow;
        net.minecraft.world.entity.projectile.Arrow nmsArrow = craftArrow.getHandle();
        CompoundTag nbt = nmsArrow.getPersistentData();

        if (nbt.getBoolean("fireArrow")) {
            target.setFireTicks(100);
            event.setDamage(event.getDamage() * 1.5);
        }

        if (nbt.getBoolean("explosive")) {
            target.setVelocity(target.getVelocity().multiply(1.5).setY(0.5));
        }
    }
}
