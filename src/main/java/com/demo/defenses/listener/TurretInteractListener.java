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
     * Ahora incluye tanto click izquierdo como derecho
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        
        // Procesar clicks izquierdos y derechos
        if (!e.getAction().name().contains("LEFT_CLICK") && !e.getAction().name().contains("RIGHT_CLICK")) {
            return;
        }
        
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
                    player.sendMessage("§eUsa click izquierdo o derecho para disparar");
                    player.sendMessage("§eDistancia máxima: 50 bloques");
                    player.sendMessage("§eRango de disparo: 90 grados frontales");
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
        
        // Click normal sin Shift - abrir inventario si está inactiva
        if (!turret.isActive()) {
            player.openInventory(stand.getInventory());
            player.sendMessage("§6Accediendo al inventario de la torreta");
        } else {
            player.sendMessage("§6Esta torreta está siendo controlada. Usa Shift+Click para activar/desactivar.");
        }
    }

    @EventHandler
    public void onTurretDestroy(org.bukkit.event.entity.EntityDeathEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;

        Turret turret = TurretManager.getTurret(stand.getUniqueId());
        if (turret == null) return;

        // Detener el control si alguien está controlando esta torreta
        // Buscar el jugador que controla esta torreta
        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            Turret controlledTurret = TurretControlManager.getControlledTurret(player);
            if (controlledTurret != null && controlledTurret.getStandId().equals(stand.getUniqueId())) {
                TurretControlManager.stopControlling(player);
                player.sendMessage("§cLa torreta que controlabas ha sido destruida.");
                break;
            }
        }

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
