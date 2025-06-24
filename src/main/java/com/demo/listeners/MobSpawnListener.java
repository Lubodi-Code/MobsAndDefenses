package com.demo.listeners;

import com.demo.goals.BreakBlockGoal;
import com.demo.managers.ConfigManager;
import net.minecraft.world.entity.PathfinderMob; // Updated to PathfinderMob
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftLivingEntity; // Updated version
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobSpawnListener implements Listener {
    private final ConfigManager config;

    public MobSpawnListener(ConfigManager config) {
        this.config = config;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Monster) || entity instanceof Creeper) {
            return;
        }
        if (!BreakBlockGoal.isAllowedMob(entity)) {
            return;
        }
        try {
            CraftLivingEntity craftEntity = (CraftLivingEntity) entity;
            // Use PathfinderMob instead of EntityCreature for 1.21.6
            PathfinderMob nms = (PathfinderMob) craftEntity.getHandle();
            nms.goalSelector.addGoal(3, new BreakBlockGoal(nms, config));
            Bukkit.getLogger().info("Added break block goal to: " + entity.getType());
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to add goal to " + entity.getType() + ": " + e.getMessage());
        }
    }
}