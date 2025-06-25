package com.demo.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;

import com.demo.managers.DifficultyManager;

/**
 * Cancels combustion caused by sunlight when the current difficulty
 * configuration specifies sun immunity.
 */
public class SunImmunityListener implements Listener {
    private final DifficultyManager difficultyManager;

    public SunImmunityListener(DifficultyManager difficultyManager) {
        this.difficultyManager = difficultyManager;
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        if (event.getCause() != EntityCombustEvent.Cause.SUN) {
            return;
        }
        String difficulty = difficultyManager.getDifficultyKey();
        if (difficultyManager.isSunImmune(difficulty)) {
            event.setCancelled(true);
        }
    }
}
