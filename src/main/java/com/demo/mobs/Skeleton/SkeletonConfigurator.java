package com.demo.mobs.Skeleton;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

/**
 * Configures skeleton attributes and equipment based on a configuration
 * section.
 */
public class SkeletonConfigurator {

    /**
     * Applies configuration values to the given skeleton.
     *
     * @param skeleton the entity to configure
     * @param sect     configuration section for the current difficulty
     */
    public void configure(Skeleton skeleton, ConfigurationSection sect) {
        if (sect == null) {
            return;
        }
        double range = sect.getDouble("follow-range", 40.0);
        skeleton.getAttribute(Attribute.FOLLOW_RANGE)
                .setBaseValue(range);
        skeleton.setRemoveWhenFarAway(false);

        ItemStack bow = new ItemStack(Material.BOW);
        if (sect.getBoolean("enchanted-bow", true)) {
            int power = sect.getInt("power-level", 2);
            bow.addUnsafeEnchantment(Enchantment.POWER, power);
            if (sect.getBoolean("allow-fire-arrows", false)) {
                bow.addUnsafeEnchantment(Enchantment.FLAME, 1);
            }
        }
        skeleton.getEquipment().setItemInMainHand(bow);
        skeleton.getEquipment().setHelmet(null);
    }
}
