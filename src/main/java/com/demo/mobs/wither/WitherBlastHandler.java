package com.demo.mobs.wither;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;
import com.demo.mobs.common.BaseSpawnHandler;

public class WitherBlastHandler extends BaseSpawnHandler implements Listener {
    private static final String META_WITHER_BLAST = "witherBlast";

    public WitherBlastHandler(JavaPlugin plugin, ConfigManager cfgMgr, DifficultyManager difMgr) {
        super(plugin, cfgMgr, difMgr);
    }

    private ConfigurationSection sec() {
        var mobCfg = difficultyManager.getMobConfig("wither");
        return mobCfg != null ? mobCfg : null;
    }

    @EventHandler
    public void onWitherShoot(ProjectileLaunchEvent ev) {
        if (!(ev.getEntity().getShooter() instanceof Wither)) return;
        if (!(ev.getEntity() instanceof WitherSkull skull)) return;

        ConfigurationSection s = sec();
        if (s == null) return;
        skull.setMetadata(META_WITHER_BLAST, new FixedMetadataValue(plugin, true));
    }

    @EventHandler
    public void onSkullHit(ProjectileHitEvent ev) {
        if (!(ev.getEntity() instanceof WitherSkull skull)) return;
        if (!skull.hasMetadata(META_WITHER_BLAST)) return;

        ConfigurationSection s = sec();
        if (s == null) return;

        Location loc = skull.getLocation();
        World w = loc.getWorld();

        float power = (float) s.getDouble("explosion-power", 6.0);
        double kbStr = s.getDouble("knockback-strength", 2.5);
        String soundName = s.getString("sound.type", "ENTITY_WITHER_AMBIENT");
        float vol = (float) s.getDouble("sound.volume", 2.0);
        float pit = (float) s.getDouble("sound.pitch", 0.8f);

        if (!w.getGameRuleValue(org.bukkit.GameRule.MOB_GRIEFING)) {
            w.setGameRule(org.bukkit.GameRule.MOB_GRIEFING, true);
        }

        w.createExplosion(loc, power, false, true);

        double radius = power * 2;
        for (Player p : w.getPlayers()) {
            if (p.getLocation().distance(loc) <= radius) {
                Vector dir = p.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(kbStr);
                p.setVelocity(dir);
            }
        }

        try {
            Sound snd = Sound.valueOf(soundName);
            w.playSound(loc, snd, vol, pit);
        } catch (IllegalArgumentException ex) {
        }

        skull.remove();
    }
}
