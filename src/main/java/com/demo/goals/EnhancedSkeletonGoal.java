package com.demo.goals;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Goal that gives skeletons long range attacks with explosive or fire arrows.
 */
public class EnhancedSkeletonGoal extends Goal {
    private final AbstractSkeleton skeleton;
    private final double enhancedRange;
    private final boolean canUseExplosiveArrows;
    private final boolean canUseFireArrows;
    private LivingEntity target;
    private int attackCooldown = 0;
    private int strafingTime = 0;
    private final Random random = new Random();

    private static final double EXPLOSIVE_CHANCE = 0.3;
    private static final double FIRE_CHANCE = 0.4;
    private static final float EXPLOSION_POWER = 2.0f;
    private static final int FIRE_DURATION = 100;

    public EnhancedSkeletonGoal(AbstractSkeleton skeleton, double enhancedRange,
            boolean explosiveArrows, boolean fireArrows) {
        this.skeleton = skeleton;
        this.enhancedRange = enhancedRange;
        this.canUseExplosiveArrows = explosiveArrows;
        this.canUseFireArrows = fireArrows;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.skeleton.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.target = livingentity;
            return this.skeleton.isHolding(Items.BOW);
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !this.skeleton.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.target = null;
        this.strafingTime = 0;
        this.skeleton.setAggressive(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }

        double distance = this.skeleton.distanceToSqr(this.target);
        boolean canSeeTarget = this.skeleton.getSensing().hasLineOfSight(this.target);
        boolean inRange = distance < (this.enhancedRange * this.enhancedRange);

        if (canSeeTarget != (this.strafingTime > 0)) {
            this.strafingTime = 0;
        }

        if (canSeeTarget) {
            this.strafingTime++;
        } else {
            this.strafingTime--;
        }

        if (distance <= (this.enhancedRange * this.enhancedRange) && this.strafingTime >= 20) {
            this.skeleton.getNavigation().stop();
            this.strafingTime += random.nextInt(20) - 10;
        } else {
            this.skeleton.getNavigation().moveTo(this.target, 1.0D);
        }

        this.skeleton.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

        if (--this.attackCooldown <= 0) {
            if (inRange && canSeeTarget) {
                this.performRangedAttack();
                this.attackCooldown = (int) Math.max(10, 40 - (distance / 4));
            }
        }
    }

    private void performRangedAttack() {
        ItemStack bow = this.skeleton.getItemInHand(this.skeleton.getUsedItemHand());
        if (bow.getItem() != Items.BOW) {
            return;
        }

        Arrow arrow = new Arrow(this.skeleton.level(), this.skeleton);

        double deltaX = this.target.getX() - this.skeleton.getX();
        double deltaY = this.target.getY(0.3333333333333333D) - arrow.getY();
        double deltaZ = this.target.getZ() - this.skeleton.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float velocity = 1.6F + (float) Math.min(horizontalDistance / 20.0, 1.0);

        arrow.shoot(deltaX, deltaY + horizontalDistance * 0.2D, deltaZ, velocity, 1.0F);

        ArrowType arrowType = determineArrowType();
        switch (arrowType) {
        case EXPLOSIVE:
            arrow.getPersistentData().putBoolean("explosive", true);
            arrow.getPersistentData().putFloat("explosionPower", EXPLOSION_POWER);
            if (this.skeleton.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME, arrow.getX(), arrow.getY(), arrow.getZ(), 5, 0.1, 0.1,
                        0.1, 0.02);
            }
            break;
        case FIRE:
            arrow.setSecondsOnFire(FIRE_DURATION);
            arrow.getPersistentData().putBoolean("fireArrow", true);
            if (this.skeleton.level() instanceof ServerLevel serverLevel2) {
                serverLevel2.sendParticles(ParticleTypes.FLAME, arrow.getX(), arrow.getY(), arrow.getZ(), 3, 0.05, 0.05,
                        0.05, 0.01);
            }
            break;
        case NORMAL:
        default:
            arrow.setBaseDamage(arrow.getBaseDamage() * 1.2);
            break;
        }

        if (arrowType == ArrowType.EXPLOSIVE) {
            this.skeleton.level().playSound(null, this.skeleton.getX(), this.skeleton.getY(), this.skeleton.getZ(),
                    SoundEvents.TNT_PRIMED, this.skeleton.getSoundSource(), 0.5F,
                    0.8F + random.nextFloat() * 0.4F);
        } else if (arrowType == ArrowType.FIRE) {
            this.skeleton.level().playSound(null, this.skeleton.getX(), this.skeleton.getY(), this.skeleton.getZ(),
                    SoundEvents.FIRECHARGE_USE, this.skeleton.getSoundSource(), 0.3F, 1.0F);
        }

        this.skeleton.level().playSound(null, this.skeleton.getX(), this.skeleton.getY(), this.skeleton.getZ(),
                SoundEvents.SKELETON_SHOOT, this.skeleton.getSoundSource(), 1.0F,
                1.0F / (random.nextFloat() * 0.4F + 0.8F));

        this.skeleton.level().addFreshEntity(arrow);
        this.skeleton.setAggressive(true);
    }

    private ArrowType determineArrowType() {
        if (!hasSpecialBow()) {
            return ArrowType.NORMAL;
        }

        double rand = random.nextDouble();

        if (canUseExplosiveArrows && rand < EXPLOSIVE_CHANCE) {
            return ArrowType.EXPLOSIVE;
        } else if (canUseFireArrows && rand < (EXPLOSIVE_CHANCE + FIRE_CHANCE)) {
            return ArrowType.FIRE;
        }

        return ArrowType.NORMAL;
    }

    private boolean hasSpecialBow() {
        ItemStack mainHand = this.skeleton.getMainHandItem();
        ItemStack offHand = this.skeleton.getOffhandItem();
        return (mainHand.getItem() == Items.BOW && (mainHand.isEnchanted()
                || mainHand.getOrCreateTag().contains("SpecialSkeleton")))
                || (offHand.getItem() == Items.BOW && (offHand.isEnchanted()
                        || offHand.getOrCreateTag().contains("SpecialSkeleton")));
    }

    private enum ArrowType {
        NORMAL, EXPLOSIVE, FIRE
    }
}
