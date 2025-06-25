package com.demo.listeners;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftLivingEntity;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.demo.goals.BreakBlockGoal;
import com.demo.goals.BuildPathGoal;
import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;

import net.minecraft.world.entity.PathfinderMob;

public class MobSpawnListener implements Listener {
    private final ConfigManager config;
    private final DifficultyManager difficulty;

    public MobSpawnListener(ConfigManager config, DifficultyManager difficulty) {
        this.config = config;
        this.difficulty = difficulty;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        // filter out non-monsters and creepers
        if (!(entity instanceof Monster) || (entity instanceof Creeper)) {
            return;
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

    
}