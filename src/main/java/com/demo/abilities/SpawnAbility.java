package com.demo.abilities;

import org.bukkit.entity.LivingEntity;

/**
 * Represents an ability that can be applied when a mob spawns.
 */
public interface SpawnAbility {
    /**
     * Apply the ability to the given entity if applicable.
     *
     * @param entity the spawned entity
     */
    void onSpawn(LivingEntity entity);
}
