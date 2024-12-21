package dev.kyoresuas.smoothTP;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class TeleportTask extends BukkitRunnable {

    private final SmoothTP plugin;
    private final Player player;
    private final List<Location> points;
    private int currentPointIndex = 0;
    private double speed;
    private int totalSteps = 0;
    private int currentStep = 0;
    private GameMode originalGameMode;

    private Location startLocation;
    private Location endLocation;
    private Location controlPoint1;
    private Location controlPoint2;

    public TeleportTask(SmoothTP plugin, Player player, List<Location> points) {
        this.plugin = plugin;
        this.player = player;
        this.points = points;
    }

    @Override
    public void run() {
        if (currentPointIndex >= points.size()) {
            player.setGameMode(originalGameMode);
            player.sendMessage("§aТелепортация завершена!");
            plugin.getLogger().info("[SmoothTP] Телепортация игрока " + player.getName() + " завершена.");
            this.cancel();
            return;
        }

        if (currentStep == 0) {
            originalGameMode = player.getGameMode();
            player.setGameMode(GameMode.SPECTATOR);

            startLocation = player.getLocation().clone();
            endLocation = points.get(currentPointIndex).clone();

            speed = plugin.getTeleportSpeed();
            double distance = startLocation.distance(endLocation);
            totalSteps = (int) Math.ceil(distance / speed);
            if (totalSteps < 1) totalSteps = 1;

            controlPoint1 = calculateControlPoint(startLocation, endLocation, 0.25);
            controlPoint2 = calculateControlPoint(startLocation, endLocation, 0.75);
        }

        if (currentStep > totalSteps) {
            currentPointIndex++;
            currentStep = 0;
            return;
        }

        double t = (double) currentStep / totalSteps;
        t = cubicInterpolation(t);

        Location targetLoc = calculateCubicBezier(startLocation, endLocation, controlPoint1, controlPoint2, t);

        float yaw = (float) (startLocation.getYaw() + (endLocation.getYaw() - startLocation.getYaw()) * t);
        float pitch = (float) (startLocation.getPitch() + (endLocation.getPitch() - startLocation.getPitch()) * t);

        Location newLoc = new Location(startLocation.getWorld(), targetLoc.getX(), targetLoc.getY(), targetLoc.getZ(), yaw, pitch);
        player.teleport(newLoc);

        currentStep++;

        if (currentStep > totalSteps) {
            currentPointIndex++;
            currentStep = 0;
        }
    }

    private double cubicInterpolation(double t) {
        return t * t * (3 - 2 * t);
    }

    private Location calculateCubicBezier(Location start, Location end, Location cp1, Location cp2, double t) {
        double x = bezier(start.getX(), cp1.getX(), cp2.getX(), end.getX(), t);
        double y = bezier(start.getY(), cp1.getY(), cp2.getY(), end.getY(), t);
        double z = bezier(start.getZ(), cp1.getZ(), cp2.getZ(), end.getZ(), t);
        return new Location(start.getWorld(), x, y, z);
    }

    private double bezier(double p0, double p1, double p2, double p3, double t) {
        double oneMinusT = 1 - t;
        return oneMinusT * oneMinusT * oneMinusT * p0 +
                3 * oneMinusT * oneMinusT * t * p1 +
                3 * oneMinusT * t * t * p2 +
                t * t * t * p3;
    }

    private Location calculateControlPoint(Location start, Location end, double factor) {
        double dx = end.getX() - start.getX();
        double dz = end.getZ() - start.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length == 0) {
            return start.clone();
        }
        double px = -dz / length;
        double pz = dx / length;
        double offset = length * factor;
        return start.clone().add(px * offset, 0, pz * offset);
    }
}