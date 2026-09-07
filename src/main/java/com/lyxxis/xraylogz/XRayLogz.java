package com.lyxxis.xraylogz;

import com.lyxxis.xraylogz.commands.XRayLogzCommand;
import com.lyxxis.xraylogz.listeners.MiningListener;
import com.lyxxis.xraylogz.listeners.StaffListener;
import com.lyxxis.xraylogz.util.DiscordWebhook;
import com.lyxxis.xraylogz.util.VersionChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class XRayLogz extends JavaPlugin {

    private static XRayLogz instance;
    private DiscordWebhook discordWebhook;
    private VersionChecker versionChecker;
    private Set<String> trackedOres;
    private Set<UUID> staffOnline;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        trackedOres = new HashSet<>(getConfig().getStringList("ores.tracked-ores"));
        staffOnline = new HashSet<>();
        
        discordWebhook = new DiscordWebhook(this);
        versionChecker = new VersionChecker(this);
        
        getServer().getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("xraylogz.staff")) {
                staffOnline.add(player.getUniqueId());
            }
        });
        
        getServer().getPluginManager().registerEvents(new MiningListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffListener(this), this);
        getCommand("xraylogz").setExecutor(new XRayLogzCommand(this));
        
        if (getConfig().getBoolean("version-check.enabled")) {
            versionChecker.checkForUpdates();
        }
        
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[XRayLogz] Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (versionChecker != null) {
            versionChecker.cancel();
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[XRayLogz] Plugin disabled!");
    }

    public static XRayLogz getInstance() {
        return instance;
    }

    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }

    public VersionChecker getVersionChecker() {
        return versionChecker;
    }

    public Set<String> getTrackedOres() {
        return trackedOres;
    }

    public Set<UUID> getStaffOnline() {
        return staffOnline;
    }

    public void reloadConfigurations() {
        reloadConfig();
        trackedOres = new HashSet<>(getConfig().getStringList("ores.tracked-ores"));
        discordWebhook.reload();
    }

    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.prefix", "&8[&cXRayLogz&8] "));
    }
}