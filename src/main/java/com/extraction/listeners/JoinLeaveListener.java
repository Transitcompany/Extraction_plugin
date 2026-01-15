package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.extract.ExtractManager;
import com.extraction.data.PlayerDataManager;
import com.extraction.data.PlayerDataManager.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.time.LocalDate;
import java.util.Arrays;

public class JoinLeaveListener implements Listener {

    private static final NamespacedKey ownerKey = new NamespacedKey("extraction", "owner");
    private static final NamespacedKey creationDateKey = new NamespacedKey("extraction", "creation_date");

    private final ExtractionPlugin plugin;
    private final ExtractManager extractManager;

    public JoinLeaveListener(ExtractionPlugin plugin, ExtractManager extractManager) {
        this.plugin = plugin;
        this.extractManager = extractManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(event.getPlayer());
        String prefix = data.getRank().getPrefix();
        event.setJoinMessage(ChatColor.BLACK + "[" + ChatColor.AQUA + "+" + ChatColor.BLACK + "] " + prefix + " " + ChatColor.AQUA + event.getPlayer().getName() + " joined");
        if (!event.getPlayer().hasPlayedBefore()) {
            giveStartingKit(event.getPlayer());
        }
        plugin.assignPlayerToTeam(event.getPlayer());

        // Teleport back to lobby if rejoining in lobby world
        String lobbyWorld = extractManager.getLobbyWorld();
        String extractWorld = extractManager.getExtractWorld();
        if (lobbyWorld != null && event.getPlayer().getWorld().getName().equals(lobbyWorld)) {
            World world = Bukkit.getWorld(lobbyWorld);
            if (world != null) {
                Location spawn = world.getSpawnLocation();
                event.getPlayer().teleport(spawn);
            }
        }
        // Do not teleport if in extraction world
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(event.getPlayer());
        String prefix = data.getRank().getPrefix();
        event.setQuitMessage(ChatColor.BLACK + "[" + ChatColor.AQUA + "-" + ChatColor.BLACK + "] " + prefix + " " + ChatColor.AQUA + event.getPlayer().getName() + " left");
    }

    private void giveStartingKit(Player player) {
        String date = LocalDate.now().toString();
        String owner = player.getName();

        // Leather boots with Netherite Tide trim
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        ArmorMeta bootsMeta = (ArmorMeta) boots.getItemMeta();
        bootsMeta.setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.TIDE));
        bootsMeta.setDisplayName(ChatColor.GOLD + "Starter Boots");
        bootsMeta.setLore(Arrays.asList(
            ChatColor.AQUA + "Starter Gear",
            ChatColor.AQUA + "Created on: " + date,
            ChatColor.AQUA + "Owned by: " + owner
        ));
        PersistentDataContainer bootsContainer = bootsMeta.getPersistentDataContainer();
        bootsContainer.set(ownerKey, PersistentDataType.STRING, owner);
        bootsContainer.set(creationDateKey, PersistentDataType.STRING, date);
        boots.setItemMeta(bootsMeta);

        // Leather leggings with Netherite Host trim
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ArmorMeta leggingsMeta = (ArmorMeta) leggings.getItemMeta();
        leggingsMeta.setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.HOST));
        leggingsMeta.setDisplayName(ChatColor.GOLD + "Starter Leggings");
        leggingsMeta.setLore(Arrays.asList(
            ChatColor.AQUA + "Starter Gear",
            ChatColor.AQUA + "Created on: " + date,
            ChatColor.AQUA + "Owned by: " + owner
        ));
        PersistentDataContainer leggingsContainer = leggingsMeta.getPersistentDataContainer();
        leggingsContainer.set(ownerKey, PersistentDataType.STRING, owner);
        leggingsContainer.set(creationDateKey, PersistentDataType.STRING, date);
        leggings.setItemMeta(leggingsMeta);

        // Stone tools
        ItemStack pickaxe = new ItemStack(Material.STONE_PICKAXE);
        ItemStack sword = new ItemStack(Material.STONE_SWORD);
        ItemStack shovel = new ItemStack(Material.STONE_SHOVEL);

        // 15 cooked beef
        ItemStack beef = new ItemStack(Material.COOKED_BEEF, 15);

        // Add to inventory
        player.getInventory().setBoots(boots);
        player.getInventory().setLeggings(leggings);
        player.getInventory().addItem(pickaxe, sword, shovel, beef);
    }
}