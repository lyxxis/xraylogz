package com.lyxxis.xraylogz.commands;

import com.lyxxis.xraylogz.XRayLogz;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class XRayLogzCommand implements CommandExecutor {

    private final XRayLogz plugin;

    public XRayLogzCommand(XRayLogz plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("xraylogz.admin")) {
            sender.sendMessage(plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getPrefix() + ChatColor.YELLOW + "Available commands:");
            sender.sendMessage(ChatColor.GRAY + "  /xraylogz reload - Reload configuration");
            sender.sendMessage(ChatColor.GRAY + "  /xraylogz version - Check for updates");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "version":
                handleVersion(sender);
                break;
            default:
                sender.sendMessage(plugin.getPrefix() + ChatColor.RED + "Unknown command. Use /xraylogz for help.");
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfigurations();
        sender.sendMessage(plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("messages.reload")));
    }

    private void handleVersion(CommandSender sender) {
        plugin.getVersionChecker().checkForUpdates();
        sender.sendMessage(plugin.getPrefix() + ChatColor.YELLOW + "Checking for updates...");
    }
}