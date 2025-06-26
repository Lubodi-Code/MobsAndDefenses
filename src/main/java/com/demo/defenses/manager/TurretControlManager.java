package com.demo.defenses.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import com.demo.managers.PluginManager;

import com.demo.defenses.model.Turret;

/**
 * Maneja el control activo de torretas por jugadores.
 */
public class TurretControlManager {

    // Referencia al plugin manager para obtener la instancia del plugin
    private static final PluginManager PLUGIN_MANAGER = PluginManager.getInstance();
    
    // Mapa de jugadores controlando torretas: Player UUID -> Turret UUID
    private static final Map<UUID, UUID> ACTIVE_CONTROLLERS = new HashMap<>();
    
    // Mapa de torretas siendo controladas: Turret UUID -> Player UUID
    private static final Map<UUID, UUID> CONTROLLED_TURRETS = new HashMap<>();
    
    // Task para actualizar las rotaciones
    private static BukkitRunnable updateTask;
    
    // Límites de rotación y distancia
    private static final double MAX_YAW_OFFSET = 45.0;  // 45 grados a cada lado (90 total)
    private static final double MAX_PITCH_UP = -90.0;   // 90 grados hacia arriba
    private static final double MAX_PITCH_DOWN = 20.0;  // 20 grados hacia abajo
    private static final double MAX_CONTROL_DISTANCE = 50.0; // Distancia máxima para controlar
    private static final double MIN_FORWARD_DOT = 0.0;  // Mínimo producto punto para estar "adelante"
    
    /**
     * Inicia el control de una torreta por un jugador
     */
    public static boolean startControlling(Player player, Turret turret) {
        UUID playerId = player.getUniqueId();
        UUID turretId = turret.getStandId();
        
        // Verificar si el jugador ya está controlando otra torreta
        if (ACTIVE_CONTROLLERS.containsKey(playerId)) {
            stopControlling(player);
        }
        
        // Verificar si la torreta ya está siendo controlada
        if (CONTROLLED_TURRETS.containsKey(turretId)) {
            return false;
        }
        
        // Registrar el control
        ACTIVE_CONTROLLERS.put(playerId, turretId);
        CONTROLLED_TURRETS.put(turretId, playerId);
        
        // Inmovilizar al jugador
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, -10, false, false));
        
        // Mensaje al jugador
        player.sendMessage("§aControlando torreta. Click izquierdo/derecho para disparar, Shift+Click para salir.");
        
        // Iniciar el task de actualización si no está activo
        startUpdateTask();
        
        return true;
    }
    
    /**
     * Termina el control de torreta de un jugador
     */
    public static void stopControlling(Player player) {
        UUID playerId = player.getUniqueId();
        UUID turretId = ACTIVE_CONTROLLERS.get(playerId);
        
        if (turretId != null) {
            // Remover del control
            ACTIVE_CONTROLLERS.remove(playerId);
            CONTROLLED_TURRETS.remove(turretId);
            
            // Liberar al jugador
            player.removePotionEffect(PotionEffectType.SLOWNESS);
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            
            // Resetear la cabeza de la torreta
            Turret turret = TurretManager.getTurret(turretId);
            if (turret != null) {
                ArmorStand stand = turret.getArmorStand();
                if (stand != null) {
                    stand.setHeadPose(new EulerAngle(0, 0, 0));
                    stand.setGlowing(false);
                }
                turret.setActive(false); // Desactivar la torreta
            }
            
            player.sendMessage("§cHas dejado de controlar la torreta.");
            
            // Detener el task si no hay más controladores
            if (ACTIVE_CONTROLLERS.isEmpty()) {
                stopUpdateTask();
            }
        }
    }
    
    /**
     * Verifica si un jugador está controlando una torreta
     */
    public static boolean isControlling(Player player) {
        return ACTIVE_CONTROLLERS.containsKey(player.getUniqueId());
    }
    
    /**
     * Obtiene la torreta que está controlando un jugador
     */
    public static Turret getControlledTurret(Player player) {
        UUID turretId = ACTIVE_CONTROLLERS.get(player.getUniqueId());
        if (turretId != null) {
            return TurretManager.getTurret(turretId);
        }
        return null;
    }

    /**
     * Verifica si el jugador puede disparar la torreta basado en su dirección
     */
    public static boolean canFireTurret(Player player, Turret turret) {
        ArmorStand stand = turret.getArmorStand();
        if (stand == null) return false;

        Location playerLoc = player.getEyeLocation();
        Location turretLoc = stand.getLocation();

        // Verificar distancia
        if (playerLoc.distance(turretLoc) > MAX_CONTROL_DISTANCE) {
            return false;
        }

        // Calcular la dirección desde la torreta hacia el jugador
        org.bukkit.util.Vector turretToPlayer = playerLoc.toVector().subtract(turretLoc.toVector()).normalize();

        // Calcular la dirección frontal de la torreta
        org.bukkit.util.Vector turretForward = turretLoc.getDirection();

        // Producto punto para verificar si el jugador está en el rango frontal
        double dot = turretForward.dot(turretToPlayer);

        return dot >= MIN_FORWARD_DOT;
    }

    /**
     * Verifica si la dirección del jugador está dentro del rango de la torreta
     */
    private static boolean isWithinTurretRange(Player player, ArmorStand stand) {
        Location playerLoc = player.getEyeLocation();
        Location turretLoc = stand.getEyeLocation();

        // Calcular la diferencia de yaw (rotación horizontal)
        float playerYaw = playerLoc.getYaw();
        float turretYaw = turretLoc.getYaw();
        float yawDiff = Math.abs(normalizeAngle(playerYaw - turretYaw));

        // Verificar si está dentro del rango horizontal
        if (yawDiff > MAX_YAW_OFFSET) {
            return false;
        }

        // Verificar si está mirando hacia la torreta (no hacia atrás)
        org.bukkit.util.Vector playerDirection = playerLoc.getDirection();
        org.bukkit.util.Vector toTurret = turretLoc.toVector().subtract(playerLoc.toVector()).normalize();

        // Si el producto punto es negativo, está mirando hacia atrás
        return playerDirection.dot(toTurret) >= 0;
    }
    
    /**
     * Inicia el task de actualización de rotaciones
     */
    private static void startUpdateTask() {
        if (updateTask != null && !updateTask.isCancelled()) {
            return;
        }
        
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, UUID> entry : ACTIVE_CONTROLLERS.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    Turret turret = TurretManager.getTurret(entry.getValue());
                    
                    if (player == null || !player.isOnline() || turret == null) {
                        // Limpiar entradas inválidas
                        ACTIVE_CONTROLLERS.remove(entry.getKey());
                        CONTROLLED_TURRETS.remove(entry.getValue());
                        continue;
                    }
                    
                    ArmorStand stand = turret.getArmorStand();
                    if (stand == null) {
                        stopControlling(player);
                        continue;
                    }

                    // Verificar distancia
                    if (player.getLocation().distance(stand.getLocation()) > MAX_CONTROL_DISTANCE) {
                        player.sendMessage("§cTe has alejado demasiado de la torreta. Control desactivado.");
                        stopControlling(player);
                        continue;
                    }

                    // Actualizar rotación solo si está dentro del rango
                    if (isWithinTurretRange(player, stand)) {
                        updateTurretRotation(player, turret);
                    } else {
                        // Resetear rotación si está fuera del rango
                        stand.setHeadPose(new EulerAngle(0, 0, 0));
                    }
                }
                
                // Si no hay más controladores, detener el task
                if (ACTIVE_CONTROLLERS.isEmpty()) {
                    cancel();
                }
            }
        };
        
        updateTask.runTaskTimer(PLUGIN_MANAGER.getPlugin(), 0L, 1L); // Actualizar cada tick
    }
    
    /**
     * Detiene el task de actualización
     */
    private static void stopUpdateTask() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
    }
    
    /**
     * Actualiza la rotación de la cabeza de la torreta basada en la vista del jugador
     */
    private static void updateTurretRotation(Player player, Turret turret) {
        ArmorStand stand = turret.getArmorStand();
        if (stand == null) return;
        
        Location playerLoc = player.getEyeLocation();
        Location turretLoc = stand.getEyeLocation();
        
        // Calcular la diferencia de yaw (rotación horizontal)
        float playerYaw = playerLoc.getYaw();
        float turretYaw = turretLoc.getYaw();
        float yawDiff = normalizeAngle(playerYaw - turretYaw);
        
        // Limitar el yaw dentro del rango permitido (45 grados a cada lado)
        yawDiff = Math.max((float)-MAX_YAW_OFFSET, Math.min((float)MAX_YAW_OFFSET, yawDiff));
        
        // Calcular el pitch (rotación vertical)
        float playerPitch = playerLoc.getPitch();
        
        // Limitar el pitch dentro del rango permitido
        float limitedPitch = (float) Math.max(MAX_PITCH_UP, Math.min(MAX_PITCH_DOWN, playerPitch));
        
        // Convertir a radianes para EulerAngle
        double yawRadians = Math.toRadians(yawDiff);
        double pitchRadians = Math.toRadians(limitedPitch);
        
        // Aplicar la rotación a la cabeza del ArmorStand
        stand.setHeadPose(new EulerAngle(pitchRadians, yawRadians, 0));
    }
    
    /**
     * Normaliza un ángulo para que esté entre -180 y 180
     */
    private static float normalizeAngle(float angle) {
        while (angle > 180f) angle -= 360f;
        while (angle < -180f) angle += 360f;
        return angle;
    }
    
    /**
     * Maneja el disparo de una torreta controlada
     */
    public static void fireTurret(Player player) {
        Turret turret = getControlledTurret(player);
        if (turret != null && turret.isActive()) {
            ArmorStand stand = turret.getArmorStand();
            if (stand != null) {
                // Verificar si puede disparar (distancia y dirección)
                if (canFireTurret(player, turret) && isWithinTurretRange(player, stand)) {
                    turret.fire(player, stand);
                } else {
                    player.sendMessage("§cNo puedes disparar en esa dirección o estás demasiado lejos.");
                }
            }
        }
    }
    
    /**
     * Limpia todos los controladores (usar al desactivar el plugin)
     */
    public static void cleanup() {
        for (UUID playerId : ACTIVE_CONTROLLERS.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                stopControlling(player);
            }
        }
        ACTIVE_CONTROLLERS.clear();
        CONTROLLED_TURRETS.clear();
        stopUpdateTask();
    }
    
}

