package com.demo.mobs.Zombie;

import com.demo.managers.DifficultyManager;
import com.demo.mobs.common.IMobConfigurator;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.configuration.ConfigurationSection;

public class ZombieConfigurator implements IMobConfigurator {
    @Override
    public void configure(PathfinderMob mob, String difficulty, DifficultyManager difficultyManager) {
        ConfigurationSection global = difficultyManager.getConfig()
                .getConfigurationSection("dificultades." + difficulty + ".global");
        ConfigurationSection mobSec = null;
        if (difficultyManager.getMobConfig("zombie") != null) {
            mobSec = difficultyManager.getMobConfig("zombie")
                    .getConfigurationSection("dificultades." + difficulty);
        }

        double healthMult = 1.0;
        double speedMult = 1.0;
        double followRange = mob.getAttributeValue(Attributes.FOLLOW_RANGE);

        if (global != null) {
            healthMult *= global.getDouble("multiplicador_vida", 1.0);
            speedMult *= global.getDouble("multiplicador_velocidad", 1.0);
            followRange = global.getDouble("rango_deteccion", followRange);
        }
        if (mobSec != null) {
            healthMult *= mobSec.getDouble("multiplicador_vida", 1.0);
            speedMult *= mobSec.getDouble("multiplicador_velocidad", 1.0);
            followRange = mobSec.getDouble("rango_deteccion", followRange);
        }

        double baseHealth = mob.getAttributeBaseValue(Attributes.MAX_HEALTH);
        double baseSpeed = mob.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);

        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth * healthMult);
        mob.setHealth((float) (baseHealth * healthMult));
        mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(baseSpeed * speedMult);
        mob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(followRange);
    }
}
