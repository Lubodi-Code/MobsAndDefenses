package com.demo;

import org.bukkit.plugin.java.JavaPlugin;
import com.demo.listeners.MobSpawnListener;
import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;
import com.demo.managers.PluginManager;

public class MobsAndDefenses extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize managers
        PluginManager.getInstance().initialize(this);
        ConfigManager configManager   = PluginManager.getInstance().getConfigManager();
        DifficultyManager diffManager = PluginManager.getInstance().getDifficultyManager();

        // Register the mob-spawn listener
        getServer().getPluginManager()
            .registerEvents(
               new MobSpawnListener(configManager, diffManager, this),
               this
            );

        getLogger().info("MobsAndDefenses has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MobsAndDefenses has been disabled!");
    }
}

