package com.demo.mobs.ghast;

import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class GhastHomingHandler implements Listener {
    private final JavaPlugin plugin;

    public GhastHomingHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFireballLaunch(ProjectileLaunchEvent ev) {
        if (!(ev.getEntity() instanceof Fireball fireball)) return;
        if (!(fireball.getShooter() instanceof Ghast)) return;

        Player target = plugin.getServer().getOnlinePlayers().stream()
                .min((a, b) -> Double.compare(
                        a.getLocation().distanceSquared(fireball.getLocation()),
                        b.getLocation().distanceSquared(fireball.getLocation())))
                .orElse(null);
        if (target == null) return;

        Vector dir = target.getLocation().toVector()
                .subtract(fireball.getLocation().toVector())
                .normalize()
                .multiply(fireball.getVelocity().length());
        fireball.setVelocity(dir);
    }
}
