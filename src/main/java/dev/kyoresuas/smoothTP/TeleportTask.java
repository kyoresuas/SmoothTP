package dev.kyoresuas.smoothTP;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class TeleportTask extends BukkitRunnable {

    private final SmoothTP plugin;
    private final Player player;
    private final List<Location> points;
    private int currentPointIndex = 0;
    private final double speed;
    private double progress = 0.0;
    private final double step = 0.05;

    public TeleportTask(SmoothTP plugin, Player player, List<Location> points) {
        this.plugin = plugin;
        this.player = player;
        this.points = points;
        this.speed = plugin.getTeleportSpeed();
    }

    @Override
    public void run() {
        if (currentPointIndex >= points.size()) {
            player.sendMessage(org.bukkit.ChatColor.GREEN + "Телепортация завершена!");
            plugin.getLogger().info("[SmoothTP] Телепортация игрока " + player.getName() + " завершена.");
            this.cancel();
            return;
        }

        Location start = player.getLocation();
        Location end = points.get(currentPointIndex);

        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double dz = end.getZ() - start.getZ();

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < speed) {
            player.teleport(end);
            currentPointIndex++;
            progress = 0.0;
        } else {
            double x = start.getX() + (dx / distance) * speed;
            double y = start.getY() + (dy / distance) * speed;
            double z = start.getZ() + (dz / distance) * speed;
            float yaw = end.getYaw();
            float pitch = end.getPitch();

            Location newLoc = new Location(start.getWorld(), x, y, z, yaw, pitch);
            player.teleport(newLoc);
        }
    }
}
