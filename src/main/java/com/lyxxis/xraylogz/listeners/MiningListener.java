package com.lyxxis.xraylogz.listeners;

import com.lyxxis.xraylogz.XRayLogz;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MiningListener implements Listener {

    private final XRayLogz plugin;
    private final Map<UUID, VeinInfo> playerVeins;
    private final Map<UUID, Integer> pendingTasks;

    public MiningListener(XRayLogz plugin) {
        this.plugin = plugin;
        this.playerVeins = new HashMap<>();
        this.pendingTasks = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Material material = block.getType();

        String oreName = material.name().toLowerCase();
        
        if (!plugin.getTrackedOres().contains(oreName)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        VeinInfo veinInfo = playerVeins.getOrDefault(playerId, new VeinInfo());

        // Check if this is a new vein or different ore type
        if (veinInfo.oreType == null || !oreName.equals(veinInfo.oreType)) {
            // Send notification for previous vein if it exists
            if (veinInfo.count > 0) {
                sendVeinNotification(player, veinInfo);
            }
            
            // Start new vein
            veinInfo = new VeinInfo();
            veinInfo.oreType = oreName;
            veinInfo.startLocation = block.getLocation();
            veinInfo.count = 1;
            veinInfo.lastLocation = block.getLocation();
        } else {
            // Check if player moved too far (vein ended)
            double distance = block.getLocation().distance(veinInfo.startLocation);
            if (distance > 10) {
                // Send notification for previous vein
                if (veinInfo.count > 0) {
                    sendVeinNotification(player, veinInfo);
                }
                
                // Start new vein
                veinInfo = new VeinInfo();
                veinInfo.oreType = oreName;
                veinInfo.startLocation = block.getLocation();
                veinInfo.count = 1;
                veinInfo.lastLocation = block.getLocation();
            } else {
                // Continue counting current vein
                veinInfo.count++;
                veinInfo.lastLocation = block.getLocation();
            }
        }

        playerVeins.put(playerId, veinInfo);

        // Cancel any pending notification task for this player
        cancelPendingTask(playerId);

        // Schedule a notification after 3 seconds of no mining
        int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            VeinInfo currentVein = playerVeins.get(playerId);
            if (currentVein != null && currentVein.count > 0) {
                sendVeinNotification(player, currentVein);
                playerVeins.remove(playerId);
            }
            pendingTasks.remove(playerId);
        }, 60L).getTaskId(); // 60 ticks = 3 seconds

        pendingTasks.put(playerId, taskId);
    }

    private void sendVeinNotification(Player player, VeinInfo veinInfo) {
        int minVeinSize = plugin.getConfig().getInt("tracking.min-vein-size", 1);
        if (veinInfo.count < minVeinSize) {
            return;
        }

        String displayName = formatOreName(veinInfo.oreType);
        String location = formatLocation(veinInfo.startLocation);

        if (plugin.getConfig().getBoolean("tracking.log-to-staff")) {
            notifyStaff(player, displayName, veinInfo.count, location);
        }

        if (plugin.getConfig().getBoolean("tracking.log-to-discord")) {
            plugin.getDiscordWebhook().sendMiningAlert(player, displayName, veinInfo.count, location);
        }
    }

    private void cancelPendingTask(UUID playerId) {
        Integer taskId = pendingTasks.get(playerId);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
            pendingTasks.remove(playerId);
        }
    }

    private String formatOreName(String oreName) {
        String formatted = oreName.replace("_", " ");
        formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        return formatted;
    }

    private String formatLocation(Location location) {
        return String.format("X: %d, Y: %d, Z: %d, World: %s", 
            location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    private void notifyStaff(Player player, String oreName, int veinSize, String location) {
        String message = String.format("&c%s &7mined &e%s &7in a &e%d &7vein at &f%s", 
            player.getName(), oreName, veinSize, location);

        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);

        // Send to all online players with the permission
        plugin.getServer().getOnlinePlayers().forEach(staff -> {
            if (staff.hasPermission("xraylogz.staff") && !staff.getUniqueId().equals(player.getUniqueId())) {
                staff.sendMessage(formattedMessage);
            }
        });
    }

    private static class VeinInfo {
        String oreType = null;
        int count = 0;
        Location startLocation = null;
        Location lastLocation = null;
    }
}