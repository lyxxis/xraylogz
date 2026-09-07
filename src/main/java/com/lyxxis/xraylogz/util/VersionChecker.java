package com.lyxxis.xraylogz.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lyxxis.xraylogz.XRayLogz;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VersionChecker {

    private final XRayLogz plugin;
    private final Logger logger;
    private final String currentVersion;
    private final String githubRepo;
    private final boolean enabled;
    private final int checkInterval;
    private int taskId;

    public VersionChecker(XRayLogz plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.currentVersion = plugin.getDescription().getVersion();
        this.githubRepo = plugin.getConfig().getString("version-check.github-repo", "lyxxis/XRayLogz");
        this.enabled = plugin.getConfig().getBoolean("version-check.enabled", true);
        this.checkInterval = plugin.getConfig().getInt("version-check.check-interval-minutes", 30) * 60 * 20;

        if (enabled) {
            startTask();
        }
    }

    public void checkForUpdates() {
        if (!enabled) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::fetchLatestVersion);
    }

    private void fetchLatestVersion() {
        try {
            String apiUrl = "https://api.github.com/repos/" + githubRepo + "/releases/latest";
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "XRayLogz/1.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                logger.warning("Failed to check for updates: HTTP " + responseCode);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
            String latestVersion = json.get("tag_name").getAsString();

            if (!latestVersion.startsWith("v")) {
                latestVersion = "v" + latestVersion;
            }

            final String finalLatestVersion = latestVersion;
            Bukkit.getScheduler().runTask(plugin, () -> handleVersionCheck(finalLatestVersion));

        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to check for updates", e);
        }
    }

    private void handleVersionCheck(String latestVersion) {
        if (currentVersion.equals(latestVersion)) {
            notifyOps(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("messages.latest-version", "&aYou are running the latest version!")));
        } else {
            notifyOps(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("messages.update-available", "&aA new version is available: &e{version}")
                    .replace("{version}", latestVersion)));
        }
    }

    private void notifyOps(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.isOp() || player.hasPermission("xraylogz.admin")) {
                player.sendMessage(plugin.getPrefix() + message);
            }
        });
    }

    private void startTask() {
        if (taskId != 0) {
            cancel();
        }

        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::fetchLatestVersion, 
            checkInterval, checkInterval).getTaskId();
    }

    public void cancel() {
        if (taskId != 0) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = 0;
        }
    }
}