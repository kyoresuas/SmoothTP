package dev.kyoresuas.smoothTP;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class SmoothTP extends JavaPlugin {

    private final Map<String, List<Location>> teleportSequences = new HashMap<>();
    private double teleportSpeed;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        loadConfigSettings();
        loadTeleportSequences();
        getLogger().info("[SmoothTP] Плагин успешно загружен!");

        if (getCommand("SmoothTP") != null) {
            getCommand("SmoothTP").setExecutor(new SmoothTPCommand(this));
        } else {
            getLogger().severe("[SmoothTP] Не удалось зарегистрировать команду /SmoothTP");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("[SmoothTP] Плагин отключен.");
    }

    public void loadConfigSettings() {
        teleportSpeed = getConfig().getDouble("teleportSettings.speed", 0.5);
    }

    public void loadTeleportSequences() {
        teleportSequences.clear();
        ConfigurationSection section = getConfig().getConfigurationSection("teleportSequences");
        if (section != null) {
            for (String sequenceName : section.getKeys(false)) {
                ConfigurationSection pointsSection = section.getConfigurationSection(sequenceName + ".points");
                if (pointsSection != null) {
                    List<Location> points = new ArrayList<>();
                    for (String pointKey : pointsSection.getKeys(false)) {
                        String path = "teleportSequences." + sequenceName + ".points." + pointKey;
                        String worldName = getConfig().getString(path + ".world");
                        double x = getConfig().getDouble(path + ".x");
                        double y = getConfig().getDouble(path + ".y");
                        double z = getConfig().getDouble(path + ".z");
                        float yaw = (float) getConfig().getDouble(path + ".yaw", 0.0);
                        float pitch = (float) getConfig().getDouble(path + ".pitch", 0.0);
                        if (worldName != null) {
                            if (Bukkit.getWorld(worldName) != null) {
                                Location loc = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                                points.add(loc);
                            }
                        }
                    }
                    if (!points.isEmpty()) {
                        teleportSequences.put(sequenceName.toLowerCase(), points);
                    }
                }
            }
        }
    }

    public List<Location> getTeleportSequence(String name) {
        return teleportSequences.get(name.toLowerCase());
    }

    public double getTeleportSpeed() {
        return teleportSpeed;
    }

    public Set<String> getAvailableSequences() {
        return teleportSequences.keySet();
    }
}
