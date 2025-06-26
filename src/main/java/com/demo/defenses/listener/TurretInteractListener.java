package com.demo.defenses.listener;

import java.util.UUID;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.demo.defenses.manager.TurretControlManager;
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
    
    /**
     * Maneja los clicks en el aire para disparar cuando se controla una torreta
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        
        // Solo procesar clicks derechos
        if (!e.getAction().name().contains("RIGHT_CLICK")) return;
        
        // Verificar si el jugador está controlando una torreta
        if (TurretControlManager.isControlling(player)) {
            e.setCancelled(true);
            TurretControlManager.fireTurret(player);
        }
    }
    
    /**
     * Limpia el control cuando un jugador se desconecta
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        TurretControlManager.stopControlling(e.getPlayer());
    }
    
    private void handleTurretInteraction(Player player, org.bukkit.entity.Entity entity, org.bukkit.event.Cancellable event) {
        if (!(entity instanceof ArmorStand stand)) return;
        
        UUID id = stand.getUniqueId();
        Turret turret = TurretManager.getTurret(id);
        if (turret == null) return;
        
        event.setCancelled(true);
        
        // Si el jugador está controlando esta torreta y hace Shift+Click, salir del control
        if (TurretControlManager.isControlling(player) && player.isSneaking()) {
            Turret controlledTurret = TurretControlManager.getControlledTurret(player);
            if (controlledTurret != null && controlledTurret.getStandId().equals(id)) {
                TurretControlManager.stopControlling(player);
                turret.toggleActive(); // Desactivar la torreta
                stand.setGlowing(false);
                return;
            }
        }
        
        // Verificar si el jugador está en modo creativo y tiene un ítem que podría romper la torreta
        if (player.isOp() && player.isSneaking() && player.getInventory().getItemInMainHand().getType().name().contains("AXE")) {
            // Los ops pueden destruir torretas con un hacha mientras se agachan
            player.sendMessage("§cTorreta destruida por administrador");
            
            // Si alguien está controlando esta torreta, detener el control
            if (TurretControlManager.getControlledTurret(player) != null) {
                TurretControlManager.stopControlling(player);
            }
            
            TurretManager.destroyTurret(id);
            return;
        }
        
        // Si el jugador ya está controlando otra torreta, no puede activar esta
        if (TurretControlManager.isControlling(player)) {
            player.sendMessage("§cYa estás controlando otra torreta. Usa Shift+Click para salir del control primero.");
            return;
        }
        
        if (player.isSneaking()) {
            // Activar/desactivar torreta
            turret.toggleActive();
            
            if (turret.isActive()) {
                // Intentar iniciar el control
                if (TurretControlManager.startControlling(player, turret)) {
                    player.sendMessage("§aModo torreta: §2activado §f- Ahora controlas la torreta");
                    stand.setGlowing(true);
                } else {
                    // Si no se puede controlar, desactivar
                    turret.toggleActive();
                    player.sendMessage("§cNo se pudo activar la torreta (puede estar siendo controlada por otro jugador)");
                }
            } else {
                // Desactivar torreta
                TurretControlManager.stopControlling(player);
                player.sendMessage("§aModo torreta: §cdesactivado");
                stand.setGlowing(false);
            }
            return;
        }
        
        // Click normal sin Shift
        if (turret.isActive()) {
            player.sendMessage("§6Esta torreta ya está siendo controlada. Usa Shift+Click para activar/desactivar.");
        } else {
            player.sendMessage("§cLa torreta está desactivada. Usa Shift+Click para activarla y controlarla.");
        }
    }

    @EventHandler
    public void onTurretDestroy(org.bukkit.event.entity.EntityDeathEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;

        Turret turret = TurretManager.getTurret(stand.getUniqueId());
        if (turret == null) return;

        // Limpiar la torreta del registro cuando muere
        TurretManager.unregister(stand.getUniqueId());

        // Mensaje de destrucción opcional
        stand.getWorld().getPlayers().forEach(p -> {
            if (p.getLocation().distance(stand.getLocation()) <= 50) {
                p.sendMessage("§8Una torreta ha sido destruida en " +
                        stand.getLocation().getBlockX() + ", " +
                        stand.getLocation().getBlockY() + ", " +
                        stand.getLocation().getBlockZ());
            }
        });
    }
}
