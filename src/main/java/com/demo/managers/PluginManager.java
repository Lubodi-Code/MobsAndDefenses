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

    /**
     * Initializes the manager with the running plugin instance.
     * This should only be called once from the plugin's {@code onEnable}.
     */
    public void initialize(JavaPlugin plugin) {
        if (this.plugin != null) {
            return; // already initialised
        }
        this.plugin = java.util.Objects.requireNonNull(plugin, "plugin");
        this.configManager = new ConfigManager(this.plugin);
    }

    /** Returns the stored plugin instance. */
    public JavaPlugin getPlugin() {
        if (plugin == null) {
            throw new IllegalStateException("PluginManager not initialized");
        }
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
