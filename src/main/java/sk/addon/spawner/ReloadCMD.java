package sk.addon.spawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
public class ReloadCMD implements CommandExecutor {

    private final JavaPlugin plugin;

    public ReloadCMD(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("spawner")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("spawner.reload")) {
                        plugin.reloadConfig();
                        player.sendMessage("Spawner configuration reloaded.");
                    } else {
                        player.sendMessage("You do not have permission to execute this command.");
                    }
                } else {
                    plugin.reloadConfig();
                    sender.sendMessage("Spawner configuration reloaded.");
                }
                return true;
            }
        }
        return false;
    }
}