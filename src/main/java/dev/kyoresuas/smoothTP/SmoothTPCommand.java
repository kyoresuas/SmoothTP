package dev.kyoresuas.smoothTP;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Set;

public class SmoothTPCommand implements CommandExecutor {

    private final SmoothTP plugin;

    public SmoothTPCommand(SmoothTP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("smoothtp.use")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Использование: /" + label + " <start|reload|list> [название]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Использование: /" + label + " start <название>");
                    return true;
                }
                String sequenceName = args[1];
                List<Location> sequence = plugin.getTeleportSequence(sequenceName);
                if (sequence == null || sequence.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "Последовательность '" + sequenceName + "' не найдена.");
                    return true;
                }

                Location firstPoint = sequence.get(0);
                player.teleport(firstPoint);

                if (sequence.size() > 1) {
                    List<Location> remainingPoints = sequence.subList(1, sequence.size());
                    new TeleportTask(plugin, player, remainingPoints).runTaskTimer(plugin, 0L, 1L);
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Последовательность содержит только одну точку.");
                }
                break;

            case "reload":
                plugin.reloadConfig();
                plugin.loadConfigSettings();
                plugin.loadTeleportSequences();
                player.sendMessage(ChatColor.GREEN + "Конфигурация SmoothTP перезагружена.");
                plugin.getLogger().info("[SmoothTP] Конфигурация перезагружена по запросу игрока " + player.getName() + ".");
                break;

            case "list":
                Set<String> sequences = plugin.getAvailableSequences();
                if (sequences.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "Нет доступных последовательностей телепортации.");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Доступные последовательности:");
                    for (String seq : sequences) {
                        player.sendMessage(ChatColor.YELLOW + "- " + seq);
                    }
                }
                break;

            default:
                player.sendMessage(ChatColor.RED + "Неизвестная команда. Использование: /" + label + " <start|reload|list> [название]");
                break;
        }

        return true;
    }
}
