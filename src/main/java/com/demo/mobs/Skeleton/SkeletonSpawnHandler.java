package com.demo.mobs.Skeleton;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;
import com.demo.mobs.common.BaseSpawnHandler;

/**
 * Handles skeleton spawn and arrow events, applying explosive arrow logic.
 */
public class SkeletonSpawnHandler extends BaseSpawnHandler {
    private static final String META_EXPLOSIVE = "explosive";

    public SkeletonSpawnHandler(JavaPlugin plugin,
                                ConfigManager configManager,
                                DifficultyManager difficultyManager) {
        super(plugin, configManager, difficultyManager);
    }

    private ConfigurationSection getSection() {
        if (difficultyManager.getMobConfig("skeleton") == null) {
            return null;
        }
        String difficulty = difficultyManager.getDifficultyKey();
        return difficultyManager.getMobConfig("skeleton")
                .getConfigurationSection("dificultades." + difficulty);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Skeleton skeleton)) {
            return;
        }
        ConfigurationSection sec = getSection();
        if (sec == null) {
            return;
        }
        new SkeletonConfigurator().configure(skeleton, sec);
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Skeleton)) {
            return;
        }
        if (!(event.getProjectile() instanceof Arrow arrow)) {
            return;
        }
        ConfigurationSection sec = getSection();
        if (sec == null) {
            return;
        }
        double chance = sec.getDouble("explosive-arrow-chance", 0.0);
        if (sec.getBoolean("allow-explosive-arrows", false) && Math.random() < chance) {
            arrow.setMetadata(META_EXPLOSIVE, new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();
        if (!(proj instanceof Arrow)) {
            return;
        }
        if (!proj.hasMetadata(META_EXPLOSIVE)) {
            return;
        }
        ConfigurationSection sec = getSection();
        if (sec == null) {
            return;
        }
        Location loc = proj.getLocation();
        float power = (float) sec.getDouble("explosion-power", 2.0);
        loc.getWorld().createExplosion(loc, power, true, true);
        proj.remove();
    }
}
