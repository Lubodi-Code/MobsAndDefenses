package com.demo.goals;

import java.util.Arrays;
import java.util.HashMap; // Updated import
import java.util.HashSet; // Updated import
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Husk;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.util.Vector;

import com.demo.managers.ConfigManager;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class BreakBlockGoal extends Goal { // Changed from PathfinderGoal
    private final Mob mob; // Changed from EntityCreature
    private final Set<Material> soft;
    private final Set<Material> hard;
    private final long ticksSoft;
    private final long ticksHard;
    private final boolean showParticles;
    private final double distance;
    private Block targetBlock;
    private int progress;
    private static final Map<Block, Integer> progressMap = new HashMap<>();

    public BreakBlockGoal(Mob mob, ConfigManager config) { // Changed parameter type
        this.mob = mob;
        this.soft = config.getSoftBlocks();
        this.hard = config.getHardBlocks();
        this.ticksSoft = (long) (config.getSoftTime() * 20);
        this.ticksHard = (long) (config.getHardTime() * 20);
        this.showParticles = config.isShowParticles();
        this.distance = config.getDetectionDistance();
    }

    @Override
    public boolean canUse() {
        org.bukkit.entity.Entity bukkit = mob.getBukkitEntity();
        if (!(bukkit instanceof LivingEntity living)) {
            return false;
        }
        if (!isAllowedMob(living)) {
            return false;
        }
        if (mob.getTarget() == null) { // Updated method name
            return false;
        }
        if (isTargetUnreachable()) {
            return false; // let BuildPathGoal handle vertical obstacles
        }
        Location loc = bukkit.getLocation();
        Vector dir = loc.getDirection().setY(0).normalize();
        Block block = loc.add(dir.multiply(distance)).getBlock();
        if (!block.getType().isSolid() || block.getType() == Material.AIR) {
            return false;
        }
        if (!soft.contains(block.getType()) && !hard.contains(block.getType())) {
            return false;
        }
        targetBlock = block;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return targetBlock != null &&
                targetBlock.getType() != Material.AIR &&
                mob.getTarget() != null && // Updated method name
                targetBlock.getType().isSolid();
    }

    @Override
    public void start() {
        progress = progressMap.getOrDefault(targetBlock, 0);
    }

    @Override
    public void tick() {
        if (targetBlock == null || targetBlock.getType() == Material.AIR || mob.getTarget() == null) {
            cleanup();
            return;
        }
        if (showParticles) {
            mob.getBukkitEntity().getWorld().spawnParticle(
                    Particle.BLOCK, // Updated particle type
                    targetBlock.getLocation().add(0.5, 0.5, 0.5),
                    3, 0.3, 0.3, 0.3,
                    targetBlock.getBlockData()
            );
        }
        boolean isSoft = soft.contains(targetBlock.getType());
        long threshold = isSoft ? ticksSoft : ticksHard;
        progress++;
        progressMap.put(targetBlock, progress);
        if (progress >= threshold) {
            targetBlock.breakNaturally();
            progressMap.remove(targetBlock);
            targetBlock = null;
            progress = 0;
        }
    }

    @Override
    public void stop() {
        cleanup();
    }

    private void cleanup() {
        if (targetBlock != null) {
            progressMap.remove(targetBlock);
        }
        targetBlock = null;
        progress = 0;
    }

    private boolean isTargetUnreachable() {
        if (mob.getTarget() == null) {
            return false;
        }
        Location mobLoc = mob.getBukkitEntity().getLocation();
        Location targetLoc = mob.getTarget().getBukkitEntity().getLocation();
        return targetLoc.getY() - mobLoc.getY() > 1.5;
    }

    // Allowed mobs list
    private static final Set<Class<? extends LivingEntity>> ALLOWED = new HashSet<>(Arrays.asList(
            Zombie.class,
            Husk.class,
            Drowned.class,
            ZombieVillager.class,
            Enderman.class,
            IronGolem.class,
            Ravager.class,
            Hoglin.class,
            Zoglin.class, // Replaced ZombifiedPiglin
            Warden.class,
            Giant.class,
            Piglin.class,
            PiglinBrute.class
    ));

    public static boolean isAllowedMob(LivingEntity entity) {
        for (Class<? extends LivingEntity> clazz : ALLOWED) {
            if (clazz.isInstance(entity)) {
                if (entity instanceof Drowned drowned && drowned.getEquipment().getItemInMainHand().getType() == Material.TRIDENT) {
                    return false;
                }
                if (entity instanceof Piglin piglin && piglin.getEquipment().getItemInMainHand().getType() == Material.CROSSBOW) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}