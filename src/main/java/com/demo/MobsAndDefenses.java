package com.demo;

import org.bukkit.plugin.java.JavaPlugin;
import com.demo.managers.PluginManager;
import com.demo.listeners.PlayerListener;

public class MobsAndDefenses extends JavaPlugin {
    
    @Override
    public void onEnable() {
        
        // Initialize managers
        PluginManager.getInstance().initialize();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        
        getLogger().info("MobsAndDefenses has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MobsAndDefenses has been disabled!");
    }
    
}