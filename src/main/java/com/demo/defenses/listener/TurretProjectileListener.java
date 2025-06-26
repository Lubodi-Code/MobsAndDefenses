package com.demo.defenses.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.entity.Projectile;

/**
 * Handles custom damage for turret projectiles such as stones or fireballs.
 */
public class TurretProjectileListener implements Listener {
    private static final String META_DAMAGE = "turret-damage";

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Projectile proj)) return;
        if (!proj.hasMetadata(META_DAMAGE)) return;
        double dmg = proj.getMetadata(META_DAMAGE).get(0).asDouble();
        e.setDamage(dmg);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Projectile proj = e.getEntity();
        if (proj.hasMetadata(META_DAMAGE)) {
            proj.remove();
        }
    }
}
