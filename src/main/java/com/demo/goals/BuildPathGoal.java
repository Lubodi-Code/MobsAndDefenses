package com.demo.goals;

import java.util.EnumSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.util.Vector;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Goal mejorado que permite a un mob construir bloques cuando el pathfinding falla.
 * Incluye debug detallado para diagnosticar problemas.
 */
public class BuildPathGoal extends Goal {
    private final Mob mob;
    private final Material buildMaterial;
    private final int maxBuildHeight;
    private final double buildRange;
    private Block targetBlock;
    private int buildCooldown;
    private int buildHeight;
    private boolean debugMode = true; // Activar para debug
    private String lastDebugMessage = "";
    private int ticksSinceLastBuild = 0;
    
    // Estados para debugging
    private enum BuildState {
        WAITING_FOR_TARGET,
        TARGET_FOUND,
        CHECKING_REACHABILITY,
        SELECTING_BUILD_LOCATION,
        BUILDING,
        COOLDOWN,
        FAILED
    }
    
    private BuildState currentState = BuildState.WAITING_FOR_TARGET;

    public BuildPathGoal(Mob mob, Material buildMaterial, int maxBuildHeight, double buildRange) {
        this.mob = mob;
        this.buildMaterial = buildMaterial;
        this.maxBuildHeight = maxBuildHeight;
        this.buildRange = buildRange;
        // CRÍTICO: Cambiar las flags para evitar conflictos
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.LOOK)); // Removido MOVE para evitar conflictos
        debug("BuildPathGoal initialized for " + mob.getBukkitEntity().getType());
    }

    @Override
    public boolean canUse() {
        ticksSinceLastBuild++;
        
        if (mob.getTarget() == null) {
            setState(BuildState.WAITING_FOR_TARGET);
            debug("No target found");
            return false;
        }
        
        setState(BuildState.TARGET_FOUND);
        debug("Target found: " + mob.getTarget().getBukkitEntity().getType() + 
              " at " + mob.getTarget().getBukkitEntity().getLocation());
        
        setState(BuildState.CHECKING_REACHABILITY);
        boolean unreachable = isTargetUnreachable();
        
        if (!unreachable) {
            debug("Target is reachable, BuildPathGoal not needed");
            return false;
        }
        
        debug("Target is unreachable, BuildPathGoal activated");
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        boolean hasTarget = mob.getTarget() != null;
        boolean stillUnreachable = isTargetUnreachable();
        boolean result = hasTarget && stillUnreachable;
        
        if (!result) {
            debug("Stopping BuildPathGoal - hasTarget: " + hasTarget + 
                  ", stillUnreachable: " + stillUnreachable);
        }
        
        return result;
    }

    private boolean isTargetUnreachable() {
        Location mobLoc = mob.getBukkitEntity().getLocation();
        Location targetLoc = mob.getTarget().getBukkitEntity().getLocation();

        double heightDiff = targetLoc.getY() - mobLoc.getY();
        double horizontalDistance = mobLoc.distance(targetLoc);

        debug("Distance check - Height diff: " + String.format("%.2f", heightDiff) + 
              ", Horizontal: " + String.format("%.2f", horizontalDistance));

        // 1. Target demasiado alto
        boolean isTooHigh = heightDiff > 1.5 && 
                           (maxBuildHeight <= 0 || heightDiff <= maxBuildHeight);
        
        if (isTooHigh) {
            debug("Target is too high (height diff: " + heightDiff + ")");
        }

        // 2. Obstáculos verticales
        boolean hasVerticalObstacle = false;
        if (isTooHigh) {
            for (double y = 1; y <= Math.min(heightDiff, maxBuildHeight > 0 ? maxBuildHeight : 10); y++) {
                Location checkLoc = mobLoc.clone().add(0, y, 0);
                Block block = checkLoc.getBlock();
                if (block.getType().isSolid() && !block.isLiquid()) {
                    hasVerticalObstacle = true;
                    debug("Vertical obstacle found at y+" + y + ": " + block.getType());
                    break;
                }
            }
        }

        // 3. Obstáculos horizontales
        boolean hasHorizontalObstacle = hasHorizontalObstacle();
        
        boolean unreachable = isTooHigh || hasVerticalObstacle || hasHorizontalObstacle;
        debug("Unreachable result: " + unreachable + 
              " (tooHigh: " + isTooHigh + 
              ", verticalObstacle: " + hasVerticalObstacle + 
              ", horizontalObstacle: " + hasHorizontalObstacle + ")");
        
        return unreachable;
    }

    private boolean hasHorizontalObstacle() {
        Location mobLoc = mob.getBukkitEntity().getLocation();
        Location targetLoc = mob.getTarget().getBukkitEntity().getLocation();
        Vector direction = targetLoc.toVector().subtract(mobLoc.toVector());
        double distance = direction.length();

        if (distance > buildRange) {
            debug("Target too far for building (distance: " + String.format("%.2f", distance) + ")");
            return false;
        }

        direction.normalize();
        for (double d = 1; d < Math.min(distance, buildRange); d += 0.5) {
            Location checkLoc = mobLoc.clone().add(direction.clone().multiply(d));
            Block block = checkLoc.getBlock();
            if (block.getType().isSolid() && !block.isLiquid()) {
                debug("Horizontal obstacle found at distance " + d + ": " + block.getType());
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        buildCooldown = 0;
        buildHeight = 0;
        ticksSinceLastBuild = 0;
        debug("BuildPathGoal started");
        setState(BuildState.SELECTING_BUILD_LOCATION);
        selectBuildLocation();
    }

    private void selectBuildLocation() {
        Location mobLoc = mob.getBukkitEntity().getLocation();
        Location targetLoc = mob.getTarget().getBukkitEntity().getLocation();
        Vector direction = targetLoc.toVector().subtract(mobLoc.toVector()).normalize();
        double heightDiff = targetLoc.getY() - mobLoc.getY();

        debug("Selecting build location - Height diff: " + heightDiff + 
              ", Current build height: " + buildHeight);

        // Prioridad 1: Construir debajo del mob si está en el aire
        Block underMob = mobLoc.clone().subtract(0, 1, 0).getBlock();
        if (underMob.getType().isAir()) {
            targetBlock = underMob;
            debug("Building under mob (falling prevention)");
            return;
        }

        // Prioridad 2: Construir hacia arriba si el target está alto
        if (heightDiff > 1.5 && (maxBuildHeight <= 0 || buildHeight < maxBuildHeight)) {
            // Intentar construir un escalón hacia adelante y arriba
            Block stepBlock = mobLoc.clone().add(direction).add(0, buildHeight, 0).getBlock();
            if (stepBlock.getType().isAir()) {
                targetBlock = stepBlock;
                debug("Building step block at height " + buildHeight);
                return;
            }

            // Construir directamente arriba
            Block above = mobLoc.clone().add(0, 1 + buildHeight, 0).getBlock();
            if (above.getType().isAir()) {
                targetBlock = above;
                debug("Building directly above at height " + (1 + buildHeight));
                return;
            }
        }

        // Prioridad 3: Construir hacia adelante
        Block frontBlock = mobLoc.clone().add(direction).getBlock();
        if (frontBlock.getType().isAir()) {
            targetBlock = frontBlock;
            debug("Building in front");
            return;
        }

        // Prioridad 4: Colocar escalera en pared
        if (buildMaterial == Material.LADDER) {
            for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
                Block wall = frontBlock.getRelative(face);
                if (wall.getType().isSolid()) {
                    Block ladderPos = frontBlock.getRelative(face.getOppositeFace());
                    if (ladderPos.getType().isAir()) {
                        targetBlock = ladderPos;
                        debug("Placing ladder against " + face + " wall");
                        return;
                    }
                }
            }
        }

        targetBlock = null;
        setState(BuildState.FAILED);
        debug("Failed to find suitable build location");
    }

    @Override
    public void tick() {
        if (buildCooldown > 0) {
            buildCooldown--;
            setState(BuildState.COOLDOWN);
            debug("Build cooldown: " + buildCooldown + " ticks remaining");
            return;
        }

        if (targetBlock == null || !targetBlock.getType().isAir()) {
            setState(BuildState.SELECTING_BUILD_LOCATION);
            selectBuildLocation();
            if (targetBlock == null) {
                debug("No valid build location found");
                return;
            }
        }

        setState(BuildState.BUILDING);
        debug("Building block at " + targetBlock.getLocation() + 
              " (material: " + buildMaterial + ")");
        
        buildBlock(targetBlock);
        buildCooldown = 20; // 1 segundo de cooldown
        buildHeight++; // Incrementar altura de construcción
        ticksSinceLastBuild = 0;
        
        // Reseleccionar ubicación para el próximo bloque
        targetBlock = null;
    }

    private void buildBlock(Block block) {
        try {
            if (buildMaterial == Material.LADDER) {
                placeLadder(block);
                debug("Ladder placed successfully");
            } else {
                block.setType(buildMaterial, false);
                debug("Block placed successfully: " + buildMaterial);
            }
            
            // Efectos visuales
            block.getWorld().spawnParticle(
                    org.bukkit.Particle.BLOCK,
                    block.getLocation().add(0.5, 0.5, 0.5),
                    10, 0.3, 0.3, 0.3,
                    buildMaterial.createBlockData()
            );
            
            // Sonido de colocación
            block.getWorld().playSound(
                    block.getLocation(),
                    org.bukkit.Sound.BLOCK_STONE_PLACE,
                    1.0f, 1.0f
            );
            
        } catch (Exception e) {
            debug("Error building block: " + e.getMessage());
        }
    }

    private void placeLadder(Block block) {
        // Buscar una pared adyacente para la escalera
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block adjacent = block.getRelative(face);
            if (adjacent.getType().isSolid()) {
                block.setType(Material.LADDER, false);
                BlockData data = block.getBlockData();
                if (data instanceof Directional dir) {
                    dir.setFacing(face);
                    block.setBlockData(data, false);
                }
                debug("Ladder placed facing " + face);
                return;
            }
        }
        // Si no hay pared, colocar bloque normal
        block.setType(Material.DIRT, false);
        debug("No wall found for ladder, placed dirt instead");
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        debug("BuildPathGoal stopped. Built " + buildHeight + " blocks. " +
              " Ticks since last build: " + ticksSinceLastBuild);
        setState(BuildState.WAITING_FOR_TARGET);
    }

    // Métodos de debug
    private void debug(String message) {
        if (debugMode && !message.equals(lastDebugMessage)) {
            Bukkit.getLogger().info("[BuildPathGoal] " + 
                                  mob.getBukkitEntity().getType() + 
                                  " (" + mob.getBukkitEntity().getEntityId() + "): " + 
                                  message);
            lastDebugMessage = message;
        }
    }

    private void setState(BuildState newState) {
        if (currentState != newState) {
            debug("State changed: " + currentState + " -> " + newState);
            currentState = newState;
        }
    }

    // Método para activar/desactivar debug desde fuera
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }

    // Getters para monitoreo
    public BuildState getCurrentState() {
        return currentState;
    }

    public int getBuildHeight() {
        return buildHeight;
    }

    public int getTicksSinceLastBuild() {
        return ticksSinceLastBuild;
    }
}