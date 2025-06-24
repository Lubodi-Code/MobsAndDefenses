package com.demo.managers;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.stream.Collectors;

public class ConfigManager {
    private final JavaPlugin plugin;
    private Set<Material> softBlocks;
    private Set<Material> hardBlocks;
    private double softTime;
    private double hardTime;
    private boolean showParticles;
    private double detectionDistance;
    private Material buildMaterial;
    private double buildRange;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();
        softBlocks = cfg.getStringList("bloques.suaves").stream()
                .map(Material::valueOf)
                .collect(Collectors.toSet());
        hardBlocks = cfg.getStringList("bloques.duros").stream()
                .map(Material::valueOf)
                .collect(Collectors.toSet());
        softTime = cfg.getDouble("tiempos.suave", 2.0);
        hardTime = cfg.getDouble("tiempos.duro", 5.0);
        showParticles = cfg.getBoolean("configuracion.mostrar_particulas", true);
        detectionDistance = cfg.getDouble("configuracion.distancia_deteccion", 1.5);
        buildMaterial = Material.valueOf(cfg.getString("construccion.material", "DIRT").toUpperCase());
        buildRange = cfg.getDouble("construccion.rango", 5.0);
    }

    public Set<Material> getSoftBlocks() {
        return softBlocks;
    }

    public Set<Material> getHardBlocks() {
        return hardBlocks;
    }

    public double getSoftTime() {
        return softTime;
    }

    public double getHardTime() {
        return hardTime;
    }

    public boolean isShowParticles() {
        return showParticles;
    }

    public double getDetectionDistance() {
        return detectionDistance;
    }

    public Material getBuildMaterial() {
        return buildMaterial;
    }

    public double getBuildRange() {
        return buildRange;
    }
}
