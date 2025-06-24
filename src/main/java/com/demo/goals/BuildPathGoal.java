package com.demo.goals;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.util.Vector;

import java.util.EnumSet;

/**
 * Goal that allows a mob to place blocks when pathfinding fails.
 */
public class BuildPathGoal extends Goal {
    private final Mob mob;
    private final Material buildMaterial;
    private final int maxBuildHeight;
    private final double buildRange;
    private Block targetBlock;
    private int buildCooldown;

    public BuildPathGoal(Mob mob, Material buildMaterial, int maxBuildHeight, double buildRange) {
        this.mob = mob;
        this.buildMaterial = buildMaterial;
        this.maxBuildHeight = maxBuildHeight;
        this.buildRange = buildRange;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return mob.getTarget() != null
                && mob.getNavigation().createPath(mob.getTarget(), 0) == null
                && isTargetUnreachable();
    }

    private boolean isTargetUnreachable() {
        Location mobLoc = mob.getBukkitEntity().getLocation();
        Location targetLoc = mob.getTarget().getBukkitEntity().getLocation();

        double heightDiff = targetLoc.getY() - mobLoc.getY();
        // Allow unlimited vertical building when maxBuildHeight is zero or negative
        if (heightDiff > 1.5 && (maxBuildHeight <= 0 || heightDiff < maxBuildHeight)) {
            return true;
        }

        Vector direction = targetLoc.toVector().subtract(mobLoc.toVector());
        double distance = direction.length();
        if (distance > buildRange) {
            return false;
        }

        direction.normalize();
        for (double d = 1; d < Math.min(distance, buildRange); d += 0.5) {
            Location checkLoc = mobLoc.clone().add(direction.clone().multiply(d));
            Block block = checkLoc.getBlock();
            if (block.getType().isSolid() || block.isLiquid()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        buildCooldown = 0;
        selectBuildLocation();
    }

    private void selectBuildLocation() {
        Location mobLoc = mob.getBukkitEntity().getLocation();
        Location targetLoc = mob.getTarget().getBukkitEntity().getLocation();
        Vector direction = targetLoc.toVector().subtract(mobLoc.toVector()).normalize();

        Block underMob = mobLoc.clone().subtract(0, 1, 0).getBlock();
        if (underMob.getType().isAir()) {
            targetBlock = underMob;
            return;
        }

        Block frontBlock = mobLoc.clone().add(direction).getBlock();
        if (frontBlock.getType().isAir()) {
            targetBlock = frontBlock;
            return;
        }

        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block relative = frontBlock.getRelative(face);
            if (relative.getType().isSolid()) {
                targetBlock = frontBlock;
                return;
            }
        }

        Block above = mobLoc.clone().add(0, 1, 0).getBlock();
        if (above.getType().isAir()) {
            targetBlock = above;
            return;
        }

        targetBlock = null;
    }

    @Override
    public void tick() {
        if (buildCooldown > 0) {
            buildCooldown--;
            return;
        }

        if (targetBlock == null || !targetBlock.getType().isAir()) {
            selectBuildLocation();
            if (targetBlock == null) {
                return;
            }
        }

        buildBlock(targetBlock);
        buildCooldown = 20;
    }

    private void buildBlock(Block block) {
        if (buildMaterial == Material.LADDER) {
            placeLadder(block);
        } else {
            block.setType(buildMaterial, false);
        }
        block.getWorld().spawnParticle(
                org.bukkit.Particle.BLOCK_DUST,
                block.getLocation().add(0.5, 0.5, 0.5),
                10, 0.3, 0.3, 0.3,
                buildMaterial.createBlockData()
        );
    }

    private void placeLadder(Block block) {
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block adjacent = block.getRelative(face);
            if (adjacent.getType().isSolid()) {
                block.setType(Material.LADDER, false);
                BlockData data = block.getBlockData();
                if (data instanceof Directional dir) {
                    dir.setFacing(face);
                    block.setBlockData(data, false);
                }
                return;
            }
        }
        block.setType(buildMaterial, false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
