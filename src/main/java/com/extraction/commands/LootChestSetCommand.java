package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.loot.LootContainerManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

// This class handles the /lootchestset command.
public class LootChestSetCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;
    private final LootContainerManager lootContainerManager;
    private static final Random RANDOM = new Random();

    public LootChestSetCommand(
        ExtractionPlugin plugin,
        LootContainerManager lootContainerManager
    ) {
        this.plugin = plugin;
        this.lootContainerManager = lootContainerManager;
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Command: /lootchestset all <percentage> <block_type>
        if (args.length >= 3 && args[0].equalsIgnoreCase("all")) {
            return handleWorldSetCommand(player, args);
        }

        // Command: /lootchestset (single block setup)
        if (args.length == 0) {
            player.sendMessage(
                "Right-click a chest, barrel, or furnace within 30 seconds to set up a loot container!"
            );

            // Timeout Runnable
            new BukkitRunnable() {
                int timer = 30;

                @Override
                public void run() {
                    if (timer-- <= 0) {
                        player.sendMessage("Loot chest setup timed out.");
                        cancel();
                    }
                }
            }
                .runTaskTimer(plugin, 0L, 20L);

            // Listener for single-block registration
            plugin
                .getServer()
                .getPluginManager()
                .registerEvents(
                    new org.bukkit.event.Listener() {
                        @org.bukkit.event.EventHandler
                        public void onPlayerInteract(
                            org.bukkit.event.player.PlayerInteractEvent event
                        ) {
                            if (!event.getPlayer().equals(player)) return;
                            if (event.getHand() != EquipmentSlot.HAND) return;
                            Block block = event.getClickedBlock();
                            if (block == null) return;

                            if (isLootContainerType(block.getType())) {
                                lootContainerManager.registerLootContainer(
                                    block.getLocation(),
                                    "default"
                                );
                                player.sendMessage(
                                    "Registered loot container at " +
                                        block.getType() +
                                        "!"
                                );
                                org.bukkit.event.HandlerList.unregisterAll(
                                    this
                                );
                                event.setCancelled(true);
                            }
                        }
                    },
                    plugin
                );
            return true;
        }

        player.sendMessage(
            "Usage: /lootchestset or /lootchestset all <percentage> <block_type>"
        );
        return true;
    }

    private boolean handleWorldSetCommand(Player player, String[] args) {
        int percentage;
        try {
            percentage = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(
                "Invalid percentage. Please enter a number (e.g., 50)."
            );
            return true;
        }

        if (percentage < 0 || percentage > 100) {
            player.sendMessage("Percentage must be between 0 and 100.");
            return true;
        }

        Material material;
        try {
            material = Material.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(
                "Invalid block type. Supported types: CHEST, BARREL, FURNACE."
            );
            return true;
        }

        if (!isLootContainerType(material)) {
            player.sendMessage(
                "Unsupported block type. Supported types: CHEST, BARREL, FURNACE."
            );
            return true;
        }

        player.sendMessage(
            "Starting to set " +
                percentage +
                "% of all " +
                material.name() +
                " blocks as loot containers. This may take a moment..."
        );

        // Run the world-wide operation asynchronously
        new BukkitRunnable() {
            private final List<Location> locationsToRegister =
                new ArrayList<>();
            private final String lootTable = "default";

            @Override
            public void run() {
                // 1. ASYNCHRONOUS PHASE: Iterate and collect locations
                for (World world : Bukkit.getWorlds()) {
                    processWorldAndCollect(
                        world,
                        material,
                        percentage,
                        locationsToRegister
                    );
                }

                int finalCount = locationsToRegister.size();

                // 2. SYNCHRONOUS PHASE: Register all and save data (MUST run on main thread)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Use the bulk registration method
                        lootContainerManager.registerMultipleLootContainers(
                            locationsToRegister,
                            lootTable
                        );

                        // Save the data only once!
                        lootContainerManager.saveLootData();

                        player.sendMessage(
                            "§aSuccessfully set §6" +
                                finalCount +
                                "§a " +
                                material.name() +
                                " blocks as loot containers. (Approx. " +
                                percentage +
                                "%)"
                        );
                    }
                }
                    .runTask(plugin);
            }
        }
            .runTaskAsynchronously(plugin);

        return true;
    }

    private void processWorldAndCollect(
        World world,
        Material material,
        int percentage,
        List<Location> locationsToRegister
    ) {
        // Boundary definitions for world iteration (Adjust these!)
        int MAX_X = 2000;
        int MAX_Z = 2000;
        int MIN_X = -2000;
        int MIN_Z = -2000;
        int MIN_Y = world.getMinHeight();
        int MAX_Y = world.getMaxHeight();

        for (int x = MIN_X; x <= MAX_X; x++) {
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                if (world.isChunkLoaded(x >> 4, z >> 4)) {
                    for (int y = MIN_Y; y < MAX_Y; y++) {
                        Block block = world.getBlockAt(x, y, z);

                        if (
                            block.getType() == material &&
                            RANDOM.nextInt(100) < percentage
                        ) {
                            locationsToRegister.add(
                                block.getLocation().clone()
                            );
                        }
                    }
                }
            }
        }
    }

    private boolean isLootContainerType(Material material) {
        return (
            material == Material.CHEST ||
            material == Material.BARREL ||
            material == Material.FURNACE
        );
    }
}
