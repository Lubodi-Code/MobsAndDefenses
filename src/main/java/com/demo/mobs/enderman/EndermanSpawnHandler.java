package com.demo.mobs.enderman;

import java.io.File;

import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EndermanSpawnHandler implements Listener {
    private final JavaPlugin plugin;
    private final FileConfiguration cfg;

    public EndermanSpawnHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        File configFile = new File(plugin.getDataFolder(), "mobs/enderman.yml");
        this.cfg = YamlConfiguration.loadConfiguration(configFile);
    }

    @EventHandler
    public void onEndermanAttack(EntityDamageByEntityEvent ev) {
        if (!(ev.getDamager() instanceof Enderman) || !(ev.getEntity() instanceof Player)) return;
        Enderman end = (Enderman) ev.getDamager();
        Player player = (Player) ev.getEntity();

        Location loc = player.getLocation();
        float volume = (float) cfg.getDouble("enderman.sound.volume", 1.0);
        float pitch  = (float) cfg.getDouble("enderman.sound.pitch", 1.0);
        player.playSound(loc, Sound.AMBIENT_CAVE, volume, pitch);

        int duration  = cfg.getInt("enderman.effect-duration", 100);
        int amplifier = cfg.getInt("enderman.effect-amplifier", 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, duration, amplifier), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, amplifier), true);

        player.getWorld().setGameRule(GameRule.MOB_GRIEFING, true);
    }
}
