package com.demo.mobs.creeper;

import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;
import com.demo.mobs.common.BaseSpawnHandler;

public class CreeperSpawnHandler extends BaseSpawnHandler {
    private final JavaPlugin plugin;

    public CreeperSpawnHandler(JavaPlugin plugin,
                               ConfigManager cfgMgr,
                               DifficultyManager difMgr) {
        super(plugin, cfgMgr, difMgr);
        this.plugin = plugin;
    }

    private ConfigurationSection sec() {
        var mobCfg = difficultyManager.getMobConfig("creeper");
        if (mobCfg == null) return null;
        return mobCfg.getConfigurationSection("creeper");
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent ev) {
        if (ev.getEntityType() != EntityType.CREEPER) return;

        Creeper creeper = (Creeper) ev.getEntity();
        ConfigurationSection s = sec();
        if (s == null) return;

        double mult = s.getDouble("speed-multiplier", 1.0);
        var attr = creeper.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED);
        if (attr != null) {
            attr.setBaseValue(attr.getBaseValue() * mult);
        }

        startChase(creeper, s);
    }

    private void startChase(Creeper creeper, ConfigurationSection s) {
        double followRange      = s.getDouble("follow-range", 30.0);
        double activationRadius = s.getDouble("activation-radius", 2.5);
        int    maxTicks         = s.getInt("chase-duration-seconds", 5) * 20;
        float  explosionPower   = (float) s.getDouble("explosion-power", 3.0);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!creeper.isValid() || creeper.isDead()) {
                    cancel();
                    return;
                }
                Location loc = creeper.getLocation();
                Player target = findNearestPlayer(loc, followRange);
                if (target != null) {
                    Vector dir = target.getLocation().toVector()
                            .subtract(loc.toVector())
                            .normalize()
                            .multiply(s.getDouble("speed-multiplier", 1.0));
                    creeper.setVelocity(dir);

                    if (loc.distance(target.getLocation()) <= activationRadius) {
                        explodeCreeper(creeper, explosionPower);
                        cancel();
                        return;
                    }
                }

                if (++ticks >= maxTicks) {
                    explodeCreeper(creeper, explosionPower);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private Player findNearestPlayer(Location loc, double maxDist) {
        Player closest = null;
        double best = maxDist * maxDist;
        for (Player p : loc.getWorld().getPlayers()) {
            double d2 = p.getLocation().distanceSquared(loc);
            if (d2 < best) {
                best = d2;
                closest = p;
            }
        }
        return closest;
    }

    private void explodeCreeper(Creeper c, float power) {
        World w = c.getWorld();
        if (Boolean.FALSE.equals(w.getGameRuleValue(GameRule.MOB_GRIEFING))) {
            w.setGameRule(GameRule.MOB_GRIEFING, true);
        }
        w.createExplosion(c.getLocation(), power, false, true);
        c.remove();
    }
}
