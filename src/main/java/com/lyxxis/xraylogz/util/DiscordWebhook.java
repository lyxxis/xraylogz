package com.lyxxis.xraylogz.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.lyxxis.xraylogz.XRayLogz;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class DiscordWebhook {

    private final XRayLogz plugin;
    private String webhookUrl;
    private String username;
    private String avatarUrl;
    private String embedColor;
    private boolean enabled;
    private final Gson gson;

    public DiscordWebhook(XRayLogz plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().create();
        reload();
    }

    public void reload() {
        this.enabled = plugin.getConfig().getBoolean("discord.enabled", true);
        this.webhookUrl = plugin.getConfig().getString("discord.webhook-url", "");
        this.username = plugin.getConfig().getString("discord.username", "XRayLogz");
        this.avatarUrl = plugin.getConfig().getString("discord.avatar-url", "");
        this.embedColor = plugin.getConfig().getString("discord.embed-color", "#FF5555");
    }

    public void sendMiningAlert(Player player, String oreName, int veinSize, String location) {
        if (!enabled || webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("YOUR_WEBHOOK_URL_HERE")) {
            return;
        }

        try {
            JsonObject embed = new JsonObject();
            embed.addProperty("title", "Ore Mining Alert");
            embed.addProperty("color", parseColor(embedColor));
            embed.addProperty("description", String.format("**%s** mined **%s** in a **%d** vein at %s", 
                player.getName(), oreName, veinSize, location));

            JsonObject footer = new JsonObject();
            footer.addProperty("text", "XRayLogz");
            embed.add("footer", footer);

            JsonObject json = new JsonObject();
            json.addProperty("username", username);
            if (!avatarUrl.isEmpty()) {
                json.addProperty("avatar_url", avatarUrl);
            }
            json.add("embeds", gson.toJsonTree(new JsonObject[]{embed}));

            sendWebhook(json);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to send Discord webhook message", e);
        }
    }

    private void sendWebhook(JsonObject json) throws Exception {
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "XRayLogz/1.0");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = gson.toJson(json).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 204 && responseCode != 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                plugin.getLogger().warning("Discord webhook returned error: " + responseCode + " - " + response);
            }
        }

        connection.disconnect();
    }

    private int parseColor(String hexColor) {
        try {
            if (hexColor.startsWith("#")) {
                hexColor = hexColor.substring(1);
            }
            return Integer.parseInt(hexColor, 16);
        } catch (NumberFormatException e) {
            return 0xFF5555;
        }
    }
}