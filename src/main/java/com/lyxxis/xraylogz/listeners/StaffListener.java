package com.lyxxis.xraylogz.listeners;

import com.lyxxis.xraylogz.XRayLogz;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StaffListener implements Listener {

    private final XRayLogz plugin;

    public StaffListener(XRayLogz plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("xraylogz.staff")) {
            plugin.getStaffOnline().add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getStaffOnline().remove(event.getPlayer().getUniqueId());
    }
}