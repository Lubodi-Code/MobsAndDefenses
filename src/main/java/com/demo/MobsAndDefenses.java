package com.demo;

import com.demo.listeners.MobSpawnListener;
import com.demo.managers.ConfigManager;
import com.demo.managers.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MobsAndDefenses extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize managers
        PluginManager.getInstance().initialize(this);
        ConfigManager configManager = PluginManager.getInstance().getConfigManager();

        // Register listeners
        getServer().getPluginManager().registerEvents(new MobSpawnListener(configManager), this);

        getLogger().info("MobBreakBlocks has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MobBreakBlocks has been disabled!");
    }
}
