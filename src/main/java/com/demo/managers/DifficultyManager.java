package com.demo.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DifficultyManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File file;

    public DifficultyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // Ensure default file is copied if not present
        plugin.saveResource("difficulty.yml", false);
        reload();
    }

    public void reload() {
        if (file == null) {
            file = new File(plugin.getDataFolder(), "difficulty.yml");
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
