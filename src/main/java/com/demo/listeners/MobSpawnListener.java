package com.demo.listeners;

import java.util.ArrayList;
import java.util.List;

import com.demo.goals.BreakBlockGoal;
import com.demo.goals.BuildPathGoal;
import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;
import com.demo.abilities.SpawnAbility;
import org.bukkit.plugin.java.JavaPlugin;
import com.demo.abilities.EnhancedSkeletonAbility;

import net.minecraft.world.entity.PathfinderMob;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftLivingEntity;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobSpawnListener implements Listener {
    private final ConfigManager config;
    private final DifficultyManager difficulty;
    private final List<SpawnAbility> abilities = new ArrayList<>();

    public MobSpawnListener(ConfigManager config, DifficultyManager difficulty, JavaPlugin plugin) {
        this.config = config;
        this.difficulty = difficulty;
        // register default abilities
        abilities.add(new EnhancedSkeletonAbility(difficulty));
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        // filter out non-monsters and creepers
        if (!(entity instanceof Monster) || (entity instanceof Creeper)) {
            return;
        }

        // Apply custom abilities
        for (SpawnAbility ability : abilities) {
            ability.onSpawn(entity);
        }

        if (!BreakBlockGoal.isAllowedMob(entity)) {
            return;
        }
        try {
            // Cast a CraftLivingEntity y obtén el handle NMS
            CraftLivingEntity craftEntity = (CraftLivingEntity) entity;
            PathfinderMob nms = (PathfinderMob) craftEntity.getHandle();
            
            nms.goalSelector.addGoal(3, new BreakBlockGoal(nms, config));
            nms.goalSelector.addGoal(4, new BuildPathGoal(
                    nms,
                    config.getBuildMaterial(),
                    config.getMaxBuildHeight(),
                    config.getBuildRange()
            ));
            Bukkit.getLogger().info("Added break and build goals to: " + entity.getType());
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to add goal to " + entity.getType() + ": " + e.getMessage());
        }
    }

    // Additional ability handlers can be added without modifying the core logic
}
