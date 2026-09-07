package com.lyxxis.xraylogz.listeners;

import com.lyxxis.xraylogz.XRayLogz;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MiningListener {

    private final XRayLogz plugin;
    private final Map<UUID, VeinInfo> playerVeins;

    public MiningListener(XRayLogz plugin) {
        this.plugin = plugin;
        this.playerVeins = new HashMap<>();
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

        if (veinInfo.lastLocation == null || veinInfo.lastLocation.getWorld() == null) {
            veinInfo = new VeinInfo();
            veinInfo.oreType = oreName;
            veinInfo.lastLocation = block.getLocation();
            veinInfo.count = 1;
        } else {
            double distance = block.getLocation().distance(veinInfo.lastLocation);
            if (distance > 5 || !oreName.equals(veinInfo.oreType)) {
                veinInfo.count = 1;
                veinInfo.oreType = oreName;
                veinInfo.lastLocation = block.getLocation();
            } else {
                veinInfo.count++;
                veinInfo.lastLocation = block.getLocation();
            }
        }

        playerVeins.put(playerId, veinInfo);

        int minVeinSize = plugin.getConfig().getInt("tracking.min-vein-size", 1);
        if (veinInfo.count < minVeinSize) {
            return;
        }

        String displayName = formatOreName(oreName);
        String location = formatLocation(block);

        if (plugin.getConfig().getBoolean("tracking.log-to-staff")) {
            notifyStaff(player, displayName, veinInfo.count, location);
        }

        if (plugin.getConfig().getBoolean("tracking.log-to-discord")) {
            plugin.getDiscordWebhook().sendMiningAlert(player, displayName, veinInfo.count, location);
        }

        veinInfo.count = 0;
        playerVeins.put(playerId, veinInfo);
    }

    private String formatOreName(String oreName) {
        String formatted = oreName.replace("_", " ");
        formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        return formatted;
    }

    private String formatLocation(Block block) {
        return String.format("X: %d, Y: %d, Z: %d, World: %s", 
            block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
    }

    private void notifyStaff(Player player, String oreName, int veinSize, String location) {
        String message = String.format("&c%s &7mined &e%s &7in a &e%d &7vein at &f%s", 
            player.getName(), oreName, veinSize, location);

        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);

        plugin.getStaffOnline().forEach(uuid -> {
            Player staff = plugin.getServer().getPlayer(uuid);
            if (staff != null && staff.isOnline() && staff.hasPermission("xraylogz.staff")) {
                staff.sendMessage(formattedMessage);
            }
        });
    }

    private static class VeinInfo {
        String oreType = "";
        int count = 0;
        Location lastLocation = null;
    }
}