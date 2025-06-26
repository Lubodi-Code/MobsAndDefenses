package com.demo;

import com.demo.listeners.MobSpawnListener;
import com.demo.managers.ConfigManager;
import com.demo.managers.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MobsAndDefenses extends JavaPlugin {
    /**
     * Singleton instance used by managers that require a plugin reference.
     * This avoids passing {@code null} when scheduling tasks.
     */
    private static MobsAndDefenses instance;

    /** Returns the running plugin instance. */
    public static MobsAndDefenses getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // Save the instance for global access
        instance = this;

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
