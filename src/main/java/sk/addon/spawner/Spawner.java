package sk.addon.spawner;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.block.Action;

import java.io.File;
import java.io.IOException;

public class Spawner extends JavaPlugin implements Listener {

    private File dataFile;
    private static FileConfiguration dataConfig;
    private static double spawnCap;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("spawner").setExecutor(new ReloadCMD(this));

        saveDefaultConfig();
        spawnCap = getConfig().getDouble("spawner-cap");

        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block != null && block.getType() == Material.SPAWNER) {
            if (!player.isSneaking()) {
                String locKey = getLocationKey(block.getLocation());
                int currentCount = dataConfig.getInt(locKey, 1);
                spawnCap = getConfig().getDouble("spawner-cap");

                if (heldItem.getType() == Material.SPAWNER) {
                    event.setCancelled(true);
                    if (currentCount >= spawnCap) {
                        player.sendMessage("§cYou cannot place more than " + (int) spawnCap + " spawners in a block!");
                        player.playSound(player, Sound.ENTITY_PLAYER_BURP, 1, 0.5f);
                    } else {
                        dataConfig.set(locKey, currentCount + 1);
                        saveData();

                        if (player.getGameMode() == GameMode.SURVIVAL) {
                            heldItem.setAmount(heldItem.getAmount() - 1);
                        }

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a" + (currentCount + 1) + "/" + (int) spawnCap));
                        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    }
                } else {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§7" + currentCount + "/" + (int) spawnCap));
                }
            }
        }
    }

    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (block.getType() == Material.SPAWNER) {
            String locKey = getLocationKey(block.getLocation());
            int currentCount = dataConfig.getInt(locKey, 1);

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c" + (currentCount - 1) + "/" + (int) spawnCap));
            player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1, 0.5f);

            if (currentCount > 1) {
                dataConfig.set(locKey, currentCount - 1);
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                EntityType entityType = spawner.getSpawnedType();

                Location location = block.getLocation();
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    block.getWorld().getBlockAt(location).setType(Material.SPAWNER);
                    spawner.setSpawnedType(entityType);
                    spawner.update();
                }, 1);
            } else {
                dataConfig.set(locKey, null);
            }

            saveData();
        }
    }

    private String getLocationKey(Location location) {
        return location.getWorld().getName() + "." +
                location.getBlockX() + "." +
                location.getBlockY() + "." +
                location.getBlockZ();
    }

    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
