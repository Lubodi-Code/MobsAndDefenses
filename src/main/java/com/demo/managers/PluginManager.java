package com.demo.managers;

import org.bukkit.plugin.java.JavaPlugin;

public class PluginManager {
    private static final PluginManager instance = new PluginManager();
    private JavaPlugin plugin;
    private ConfigManager configManager;
    private DifficultyManager difficultyManager;

    /** Register crafting and event handlers for turret defenses. */
    public void registerDefenses() {
        com.demo.defenses.crafting.TurretCrafting crafting = new com.demo.defenses.crafting.TurretCrafting(plugin);
        crafting.registerRecipes();

        com.demo.defenses.manager.TurretManager.setup(plugin);

        var pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new com.demo.defenses.spawn.TurretSpawnHandler(plugin), plugin);
        pm.registerEvents(new com.demo.defenses.listener.TurretInteractListener(), plugin);
        pm.registerEvents(new com.demo.defenses.listener.TurretInventoryListener(), plugin);
    }

    private PluginManager() {}

    public static PluginManager getInstance() {
        return instance;
    }

    public void initialize(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configManager = new ConfigManager(plugin);
        this.difficultyManager = new DifficultyManager(plugin);
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DifficultyManager getDifficultyManager() {
        return difficultyManager;
    }
}
