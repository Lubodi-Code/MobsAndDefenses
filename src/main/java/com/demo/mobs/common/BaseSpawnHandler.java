package com.demo.mobs.common;

import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class BaseSpawnHandler implements Listener {
    protected final JavaPlugin plugin;
    protected final ConfigManager configManager;
    protected final DifficultyManager difficultyManager;

    protected BaseSpawnHandler(JavaPlugin plugin, ConfigManager configManager, DifficultyManager difficultyManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.difficultyManager = difficultyManager;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
