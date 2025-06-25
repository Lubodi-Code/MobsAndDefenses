package com.demo.abilities;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;

import com.demo.goals.EnhancedSkeletonGoal;
import com.demo.managers.DifficultyManager;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Applies special abilities to skeletons when they spawn.
 */
public class EnhancedSkeletonAbility implements SpawnAbility {

    private final DifficultyManager difficulty;

    public EnhancedSkeletonAbility(DifficultyManager difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public void onSpawn(LivingEntity entity) {
        if (!(entity instanceof Skeleton skeleton)) {
            return;
        }
        try {
            CraftLivingEntity craftEntity = (CraftLivingEntity) skeleton;
            AbstractSkeleton nmsSkeleton = (AbstractSkeleton) craftEntity.getHandle();

            String current = difficulty.getConfig().getString("dificultad_activa", "facil");
            double chance = difficulty.getConfig()
                    .getDouble("dificultades." + current + ".mobs.skeleton.probabilidad_especial", 0.0);

            boolean isSpecial = Math.random() < chance;
            if (isSpecial) {
                ItemStack bow = new ItemStack(Items.BOW);
                bow.getOrCreateTag().putBoolean("SpecialSkeleton", true);
                nmsSkeleton.setItemInHand(InteractionHand.MAIN_HAND, bow);

                nmsSkeleton.goalSelector.addGoal(2, new EnhancedSkeletonGoal(nmsSkeleton, 32.0, true, true));
                nmsSkeleton.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(32.0);
                nmsSkeleton.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.28);
                Bukkit.getLogger().info("Enhanced skeleton spawned with special abilities!");
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to enhance skeleton: " + e.getMessage());
        }
    }
}
