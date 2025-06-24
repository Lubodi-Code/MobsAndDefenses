package com.demo.managers;

import org.bukkit.plugin.java.JavaPlugin;

public class PluginManager {
    private static final PluginManager instance = new PluginManager();
    private JavaPlugin plugin;
    private ConfigManager configManager;

    private PluginManager() {}

    public static PluginManager getInstance() {
        return instance;
    }

    public void initialize(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configManager = new ConfigManager(plugin);
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
