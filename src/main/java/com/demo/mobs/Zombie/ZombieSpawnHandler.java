package com.demo.mobs.Zombie;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftLivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.demo.goals.BreakBlockGoal;
import com.demo.goals.BuildPathGoal;
import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;
import com.demo.mobs.common.BaseSpawnHandler;

import net.minecraft.world.entity.PathfinderMob;

public class ZombieSpawnHandler extends BaseSpawnHandler {
    private final ZombieConfigurator configurator = new ZombieConfigurator();

    public ZombieSpawnHandler(JavaPlugin plugin, ConfigManager configManager, DifficultyManager difficultyManager) {
        super(plugin, configManager, difficultyManager);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Zombie)) {
            return;
        }
        String difficulty = configManager.getCurrentDifficulty();
        CraftLivingEntity craft = (CraftLivingEntity) event.getEntity();
        PathfinderMob nms = (PathfinderMob) craft.getHandle();
        configurator.configure(nms, difficulty, difficultyManager);

        ConfigurationSection mobSec = null;
        if (difficultyManager.getMobConfig("zombie") != null) {
            mobSec = difficultyManager.getMobConfig("zombie")
                    .getConfigurationSection("dificultades." + difficulty);
        }
        if (mobSec == null) {
            return;
        }
        if (mobSec.getBoolean("romper_bloques", false)) {
            nms.goalSelector.addGoal(3, new BreakBlockGoal(nms, configManager));
        }
        if (mobSec.getBoolean("construir", false)) {
            nms.goalSelector.addGoal(4, new BuildPathGoal(
                    nms,
                    configManager.getBuildMaterial(),
                    configManager.getMaxBuildHeight(),
                    configManager.getBuildRange()
            ));
        }
        
    }
}
