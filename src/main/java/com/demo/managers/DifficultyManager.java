package com.demo.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DifficultyManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File file;
    private final Map<String, FileConfiguration> mobConfigs = new HashMap<>();

    public DifficultyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // Ensure default files are copied if not present
        plugin.saveResource("difficulty.yml", false);
        // Copy example mob config
        plugin.saveResource("mobs/zombie.yml", false);
        reload();
    }

    public void reload() {
        if (file == null) {
            file = new File(plugin.getDataFolder(), "difficulty.yml");
        }
        config = YamlConfiguration.loadConfiguration(file);

        mobConfigs.clear();
        File mobsDir = new File(plugin.getDataFolder(), "mobs");
        if (mobsDir.exists() && mobsDir.isDirectory()) {
            for (File mobFile : mobsDir.listFiles((d, name) -> name.endsWith(".yml"))) {
                String key = mobFile.getName().replaceFirst("\\.yml$", "").toLowerCase();
                mobConfigs.put(key, YamlConfiguration.loadConfiguration(mobFile));
            }
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public ConfigurationSection getGlobalSection(String difficulty) {
        return config.getConfigurationSection("dificultades." + difficulty + ".global");
    }

    public FileConfiguration getMobConfig(String mob) {
        return mobConfigs.get(mob.toLowerCase());
    }
}
