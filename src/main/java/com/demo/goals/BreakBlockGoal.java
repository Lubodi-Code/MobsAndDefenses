package com.demo.goals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
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

public class BreakBlockGoal extends Goal {
    private final Mob mob;
    private final Set<Material> soft;
    private final Set<Material> hard;
    private final long ticksSoft;
    private final long ticksHard;
    private final boolean showParticles;
    private final double distance;
    private Block targetBlock;
    private int progress;
    private boolean debugMode = true;
    private String lastDebugMessage = "";
    
    // Estados para debugging
    private enum BreakState {
        WAITING_FOR_TARGET,
        CHECKING_CONDITIONS,
        BREAKING,
        COMPLETED,
        FAILED
    }
    
    private BreakState currentState = BreakState.WAITING_FOR_TARGET;
    
    private static final Map<Block, Integer> progressMap = new ConcurrentHashMap<>();

    public BreakBlockGoal(Mob mob, ConfigManager config) {
        this.mob = mob;
        this.soft = config.getSoftBlocks();
        this.hard = config.getHardBlocks();
        this.ticksSoft = (long) (config.getSoftTime() * 20);
        this.ticksHard = (long) (config.getHardTime() * 20);
        this.showParticles = config.isShowParticles();
        this.distance = config.getDetectionDistance();
        
        debug("BreakBlockGoal initialized for " + mob.getBukkitEntity().getType());
        debug("Soft blocks: " + soft.size() + ", Hard blocks: " + hard.size());
        debug("Break times - Soft: " + ticksSoft + " ticks, Hard: " + ticksHard + " ticks");
    }

    @Override
    public boolean canUse() {
        setState(BreakState.CHECKING_CONDITIONS);
        
        org.bukkit.entity.Entity bukkit = mob.getBukkitEntity();
        if (!(bukkit instanceof LivingEntity living)) {
            debug("Entity is not LivingEntity");
            return false;
        }
        
        if (!isAllowedMob(living)) {
            debug("Mob type not allowed: " + living.getType());
            return false;
        }
        
        if (mob.getTarget() == null) {
            setState(BreakState.WAITING_FOR_TARGET);
            debug("No target found");
            return false;
        }
        
        debug("Target found: " + mob.getTarget().getBukkitEntity().getType());
        
        // CAMBIO CRÍTICO: Solo romper bloques si NO son obstáculos verticales
        // Dejar que BuildPathGoal maneje obstáculos verticales
        if (isTargetUnreachableVertically()) {
            debug("Target unreachable vertically, letting BuildPathGoal handle it");
            return false;
        }
        
        Location loc = bukkit.getLocation();
        Vector dir = loc.getDirection().setY(0).normalize();
        Block block = loc.add(dir.multiply(distance)).getBlock();
        
        debug("Checking block at " + block.getLocation() + ": " + block.getType());
        
        if (!block.getType().isSolid() || block.getType() == Material.AIR) {
            debug("Block is not solid or is air");
            return false;
        }
        
        if (!soft.contains(block.getType()) && !hard.contains(block.getType())) {
            debug("Block type not configured for breaking: " + block.getType());
            return false;
        }
        
        targetBlock = block;
        debug("Target block selected for breaking: " + block.getType() + 
              " at " + block.getLocation());
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        boolean hasTarget = mob.getTarget() != null;
        boolean blockExists = targetBlock != null && targetBlock.getType() != Material.AIR;
        boolean blockSolid = targetBlock != null && targetBlock.getType().isSolid();
        
        boolean result = hasTarget && blockExists && blockSolid;
        
        if (!result) {
            debug("Stopping BreakBlockGoal - hasTarget: " + hasTarget + 
                  ", blockExists: " + blockExists + ", blockSolid: " + blockSolid);
        }
        
        return result;
    }

    @Override
    public void start() {
        progress = progressMap.getOrDefault(targetBlock, 0);
        setState(BreakState.BREAKING);
        debug("Started breaking " + targetBlock.getType() + 
              " (existing progress: " + progress + ")");
    }

    @Override
    public void tick() {
        if (targetBlock == null || targetBlock.getType() == Material.AIR || mob.getTarget() == null) {
            setState(BreakState.FAILED);
            cleanup();
            return;
        }

        if (showParticles) {
            mob.getBukkitEntity().getWorld().spawnParticle(
                    Particle.BLOCK,
                    targetBlock.getLocation().add(0.5, 0.5, 0.5),
                    3, 0.3, 0.3, 0.3,
                    targetBlock.getBlockData()
            );
        }

        boolean isSoft = soft.contains(targetBlock.getType());
        long threshold = isSoft ? ticksSoft : ticksHard;
        
        progress++;
        progressMap.put(targetBlock, progress);
        
        // Debug cada 20 ticks (1 segundo)
        if (progress % 20 == 0) {
            debug("Breaking progress: " + progress + "/" + threshold + 
                  " (" + String.format("%.1f", (progress * 100.0 / threshold)) + "%)");
        }
        
        if (progress >= threshold) {
            setState(BreakState.COMPLETED);
            debug("Block broken successfully: " + targetBlock.getType());
            targetBlock.breakNaturally();
            progressMap.remove(targetBlock);
            targetBlock = null;
            progress = 0;
        }
    }

    @Override
    public void stop() {
        debug("BreakBlockGoal stopped (progress: " + progress + ")");
        cleanup();
    }

    private void cleanup() {
        if (targetBlock != null) {
            progressMap.remove(targetBlock);
        }
        targetBlock = null;
        progress = 0;
        setState(BreakState.WAITING_FOR_TARGET);
    }

    private boolean isTargetUnreachableVertically() {
        if (mob.getTarget() == null) {
            return false;
        }
        Location mobLoc = mob.getBukkitEntity().getLocation();
        Location targetLoc = mob.getTarget().getBukkitEntity().getLocation();
        double heightDiff = targetLoc.getY() - mobLoc.getY();
        
        boolean isUnreachable = heightDiff > 1.5;
        if (isUnreachable) {
            debug("Target is vertically unreachable (height diff: " + 
                  String.format("%.2f", heightDiff) + ")");
        }
        
        return isUnreachable;
    }

    // Lista de mobs permitidos
    private static final Set<Class<? extends LivingEntity>> ALLOWED = new HashSet<>(Arrays.asList(
            Zombie.class,
            Husk.class,
            Drowned.class,
            ZombieVillager.class,
            Enderman.class,
            IronGolem.class,
            Ravager.class,
            Hoglin.class,
            Zoglin.class,
            Warden.class,
            Giant.class,
            Piglin.class,
            PiglinBrute.class
    ));

    public static boolean isAllowedMob(LivingEntity entity) {
        for (Class<? extends LivingEntity> clazz : ALLOWED) {
            if (clazz.isInstance(entity)) {
                if (entity instanceof Drowned drowned && 
                    drowned.getEquipment().getItemInMainHand().getType() == Material.TRIDENT) {
                    return false;
                }
                if (entity instanceof Piglin piglin && 
                    piglin.getEquipment().getItemInMainHand().getType() == Material.CROSSBOW) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    // Métodos de debug
    private void debug(String message) {
        if (debugMode && !message.equals(lastDebugMessage)) {
            Bukkit.getLogger().info("[BreakBlockGoal] " + 
                                  mob.getBukkitEntity().getType() + 
                                  " (" + mob.getBukkitEntity().getEntityId() + "): " + 
                                  message);
            lastDebugMessage = message;
        }
    }

    private void setState(BreakState newState) {
        if (currentState != newState) {
            debug("State changed: " + currentState + " -> " + newState);
            currentState = newState;
        }
    }

    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }

    public BreakState getCurrentState() {
        return currentState;
    }
}