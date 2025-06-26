package com.demo.defenses.listener;

import java.util.UUID;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.demo.defenses.manager.TurretManager;
import com.demo.defenses.model.Turret;

/**
 * Handles interacting with a placed turret.
 */
public class TurretInteractListener implements Listener {

    @EventHandler
    public void onInteractAt(PlayerInteractAtEntityEvent e) {
        handleTurretInteraction(e.getPlayer(), e.getRightClicked(), e);
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        handleTurretInteraction(e.getPlayer(), e.getRightClicked(), e);
    }
    
    private void handleTurretInteraction(Player player, org.bukkit.entity.Entity entity, org.bukkit.event.Cancellable event) {
        if (!(entity instanceof ArmorStand stand)) return;
        
        UUID id = stand.getUniqueId();
        Turret turret = TurretManager.getTurret(id);
        if (turret == null) return;
        
        event.setCancelled(true);
        
        
        
        if (player.isSneaking()) {
            turret.toggleActive();
            player.sendMessage("§aModo torreta: §f" + (turret.isActive() ? "§2activado" : "§cdesactivado"));
            
            // Efecto visual del estado
            if (turret.isActive()) {
                stand.setGlowing(true);
            } else {
                stand.setGlowing(false);
            }
            return;
        }
        
        if (turret.isActive()) {
            if (turret.isReady()) {
                turret.fire(player, stand);
                player.sendMessage("§e¡Pum! Torreta disparada");
            } else {
                player.sendMessage("§6Torreta recargando...");
            }
        } else {
            player.sendMessage("§cLa torreta está desactivada. Usa Shift+Click para activarla");
        }
    }



    @EventHandler 
    public void onTurretDestroy(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        
        Turret turret = TurretManager.getTurret(stand.getUniqueId());
        if (turret == null) return;
        
        // Limpiar la torreta del registro cuando muere
        TurretManager.unregister(stand.getUniqueId());
        
        // Opcional: Mensaje de destrucción
        stand.getWorld().getPlayers().forEach(player -> {
            if (player.getLocation().distance(stand.getLocation()) <= 50) {
                player.sendMessage("§8Una torreta ha sido destruida en " + 
                    stand.getLocation().getBlockX() + ", " + 
                    stand.getLocation().getBlockY() + ", " + 
                    stand.getLocation().getBlockZ());
            }
        });
    }
}

