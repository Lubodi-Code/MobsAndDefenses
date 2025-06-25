package com.demo;

import org.bukkit.plugin.java.JavaPlugin;

import com.demo.listeners.MobSpawnListener;
import com.demo.managers.ConfigManager;
import com.demo.managers.PluginManager;

public class MobsAndDefenses extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize managers
        PluginManager.getInstance().initialize(this);
        ConfigManager configManager = PluginManager.getInstance().getConfigManager();

        // Register listeners
        getServer().getPluginManager().registerEvents(new MobSpawnListener(configManager), this);
        //comand registration
     

        getLogger().info("MobBreakBlocks has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MobBreakBlocks has been disabled!");
    }
}
