package com.demo;

import org.bukkit.plugin.java.JavaPlugin;

import com.demo.managers.ConfigManager;
import com.demo.managers.DifficultyManager;
import com.demo.managers.PluginManager;
import com.demo.mobs.Zombie.ZombieSpawnHandler;
import com.demo.mobs.Skeleton.SkeletonSpawnHandler;
import com.demo.mobs.creeper.CreeperSpawnHandler;
import com.demo.mobs.enderman.EndermanSpawnHandler;
import com.demo.mobs.spider.SpiderTrapHandler;
import com.demo.mobs.wither.WitherBlastHandler;
import com.demo.mobs.ghast.GhastHomingHandler;
import com.demo.listeners.SunImmunityListener;

public class MobsAndDefenses extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize managers
        PluginManager.getInstance().initialize(this);
        ConfigManager configManager   = PluginManager.getInstance().getConfigManager();
        DifficultyManager diffManager = PluginManager.getInstance().getDifficultyManager();



        // Register mob spawn handlers based on configuration
        if (diffManager.getMobConfig("zombie") != null) {
            new ZombieSpawnHandler(this, configManager, diffManager).register();
        }
        if (diffManager.getMobConfig("skeleton") != null) {
            new SkeletonSpawnHandler(this, configManager, diffManager).register();
        }
        if (diffManager.getMobConfig("creeper") != null) {
            new CreeperSpawnHandler(this, configManager, diffManager).register();
        }
        if (diffManager.getMobConfig("enderman") != null) {
            getServer().getPluginManager().registerEvents(new EndermanSpawnHandler(this), this);
        }
        if (diffManager.getMobConfig("spider") != null) {
            getServer().getPluginManager().registerEvents(new SpiderTrapHandler(configManager, diffManager), this);
        }
        if (diffManager.getMobConfig("wither") != null) {
            new WitherBlastHandler(this, configManager, diffManager).register();
        }
        if (diffManager.getMobConfig("ghast") != null) {
            getServer().getPluginManager().registerEvents(new GhastHomingHandler(this), this);
        }
        // Register sunlight immunity listener
        getServer().getPluginManager().registerEvents(new SunImmunityListener(diffManager), this);
        // command registration


        getLogger().info("MobsAndDefenses has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MobsAndDefenses has been disabled!");
    }
}

