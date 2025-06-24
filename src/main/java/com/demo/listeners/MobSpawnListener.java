package com.demo.listeners;

import com.demo.goals.BreakBlockGoal;
import com.demo.managers.ConfigManager;
import net.minecraft.world.entity.EntityCreature;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
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
            EntityCreature nms = ((CraftLivingEntity) entity).getHandle();
            nms.goalSelector.addGoal(3, new BreakBlockGoal(nms, config));
            Bukkit.getLogger().info("Added break block goal to: " + entity.getType());
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to add goal to " + entity.getType() + ": " + e.getMessage());
        }
    }
}
