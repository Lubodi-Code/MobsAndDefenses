package com.demo;

import org.bukkit.plugin.java.JavaPlugin;

import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;
import com.demo.managers.PluginManager;
import com.demo.mobs.Zombie.ZombieSpawnHandler;

public class MobsAndDefenses extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize managers
        PluginManager.getInstance().initialize(this);
        ConfigManager configManager   = PluginManager.getInstance().getConfigManager();
        DifficultyManager diffManager = PluginManager.getInstance().getDifficultyManager();



        // Register mob spawn handlers based on configuration
        if (diffManager.getMobConfig("zombie") != null) {
            new ZombieSpawnHandler(this, configManager, diffManager).register();
        }
        // command registration


        getLogger().info("MobsAndDefenses has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MobsAndDefenses has been disabled!");
    }
}

