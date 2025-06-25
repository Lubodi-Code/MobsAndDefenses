package com.demo.mobs.common;

import net.minecraft.world.entity.PathfinderMob;
import com.demo.managers.DifficultyManager;

public interface IMobConfigurator {
    void configure(PathfinderMob mob, String difficulty, DifficultyManager difficultyManager);
}
