package com.demo.listeners;

import com.demo.goals.BreakBlockGoal;
import com.demo.goals.BuildPathGoal;
import com.demo.goals.EnhancedSkeletonGoal;
import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;

import net.minecraft.world.entity.PathfinderMob;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftLivingEntity;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

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

        if (entity instanceof Skeleton skeleton) {
            handleSkeletonSpawn(skeleton);
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

    private void handleSkeletonSpawn(Skeleton skeleton) {
        try {
            CraftLivingEntity craftEntity = (CraftLivingEntity) skeleton;
            net.minecraft.world.entity.monster.AbstractSkeleton nmsSkeleton =
                    (net.minecraft.world.entity.monster.AbstractSkeleton) craftEntity.getHandle();

            String current = difficulty.getConfig().getString("dificultad_activa", "facil");
            double chance = difficulty.getConfig()
                    .getDouble("dificultades." + current + ".mobs.skeleton.probabilidad_especial", 0.0);

            boolean isSpecial = Math.random() < chance;
            if (isSpecial) {
                net.minecraft.world.item.ItemStack bow = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOW);
                bow.getOrCreateTag().putBoolean("SpecialSkeleton", true);
                nmsSkeleton.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, bow);

                nmsSkeleton.goalSelector.addGoal(2,
                        new EnhancedSkeletonGoal(nmsSkeleton, 32.0, true, true));

                nmsSkeleton.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE)
                        .setBaseValue(32.0);
                nmsSkeleton.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                        .setBaseValue(0.28);
                Bukkit.getLogger().info("Enhanced skeleton spawned with special abilities!");
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to enhance skeleton: " + e.getMessage());
        }
    }
}