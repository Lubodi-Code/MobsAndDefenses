package com.demo.mobs.spider;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;

public class SpiderTrapHandler implements Listener {
    private final ConfigManager cfgMgr;
    private final DifficultyManager difMgr;
    private final Random random = new Random();

    public SpiderTrapHandler(ConfigManager cfgMgr, DifficultyManager difMgr) {
        this.cfgMgr = cfgMgr;
        this.difMgr = difMgr;
    }

    @EventHandler
    public void onSpiderHit(EntityDamageByEntityEvent ev) {
        if (!(ev.getDamager() instanceof Spider || ev.getDamager() instanceof CaveSpider)) return;
        if (!(ev.getEntity() instanceof Player victim)) return;

        var mobCfg = difMgr.getMobConfig("spider");
        if (mobCfg == null) return;
        ConfigurationSection sect = mobCfg.getConfigurationSection("spider");
        if (sect == null || !sect.getBoolean("enabled", true)) return;

        double chance = sect.getDouble("trap-chance", 0.2);
        if (random.nextDouble() > chance) return;

        Location center = victim.getLocation();
        int r = sect.getInt("trap-radius", 1);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = 0; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    Location loc = center.clone().add(dx, dy, dz);
                    if (loc.getBlock().isPassable()) {
                        loc.getBlock().setType(Material.COBWEB);
                    }
                }
            }
        }
    }
}
