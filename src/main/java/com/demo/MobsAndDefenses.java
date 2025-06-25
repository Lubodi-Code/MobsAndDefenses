package com.demo;

import org.bukkit.plugin.java.JavaPlugin;

import com.demo.mobs.Zombie.ZombieSpawnHandler;
import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;
import com.demo.managers.PluginManager;

public class MobsAndDefenses extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize managers
        PluginManager.getInstance().initialize(this);
        ConfigManager configManager = PluginManager.getInstance().getConfigManager();
        DifficultyManager difficultyManager = PluginManager.getInstance().getDifficultyManager();

        // Register mob spawn handlers based on configuration
        if (difficultyManager.getMobConfig("zombie") != null) {
            new ZombieSpawnHandler(this, configManager, difficultyManager).register();
        }
        // command registration
     

        getLogger().info("MobBreakBlocks has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MobBreakBlocks has been disabled!");
    }
}
