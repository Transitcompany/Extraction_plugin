package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.stash.StashManager;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StashCommand implements CommandExecutor, Listener {

    private final ExtractionPlugin plugin;
    private final StashManager stashManager;

    // --- GUI Constants ---
    // The new 54-slot GUI Title. This must be unique.
    public static final String STASH_GUI_TITLE =
        ChatColor.DARK_GRAY +
        "" +
        ChatColor.BOLD +
        "Personal Stash Depot (5x9)";

    private static final int INVENTORY_SIZE = 54;
    private static final int STASH_SLOTS = 45; // Slots 0-44 are for storage

    // Aesthetic & Utility
    private static final Material NAV_FILLER_MATERIAL =
        Material.BLACK_STAINED_GLASS_PANE;

    // Button Slots
    private static final int INFO_SLOT = 46;

    private static final int STATS_SLOT = 49;
    private static final int SELL_SLOT = 51;
    private static final int EXIT_SLOT = 52;

    public StashCommand(ExtractionPlugin plugin, StashManager stashManager) {
        this.plugin = plugin;
        this.stashManager = stashManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // --- Utility Methods ---
    private ItemStack createGuiItem(
        final Material material,
        final String name,
        final String... lore
    ) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            final List<String> finalLore = new ArrayList<>();
            for (final String line : lore) {
                finalLore.add(line);
            }
            meta.setLore(finalLore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createFiller(Material material) {
        return createGuiItem(material, " ", "");
    }



    // --- GUI Creation Logic ---

    private Inventory createStashGUI(Player player) {
        // 1. Create a NEW Inventory for display purposes
        Inventory displayInv = Bukkit.createInventory(
            null,
            INVENTORY_SIZE,
            STASH_GUI_TITLE
        );

        // 2. Get the actual stash contents and transfer to the new display inventory
        Inventory rawStashInv = stashManager.getStash(player);
        ItemStack[] stashContents = rawStashInv.getContents();
        for (int i = 0; i < STASH_SLOTS && i < stashContents.length; i++) {
            // Copy the item; cloning prevents modification of the original item object if it has meta
            if (stashContents[i] != null) {
                displayInv.setItem(i, stashContents[i].clone());
            }
        }

        // 3. Fill the bottom utility row (slots 45-53)
        ItemStack navFiller = createFiller(NAV_FILLER_MATERIAL);
        for (int i = STASH_SLOTS; i < INVENTORY_SIZE; i++) {
            displayInv.setItem(i, navFiller);
        }

        // 4. Add Utility and Info Icons/Buttons

        // Stash Information Icon (Slot 46)
        ItemStack infoIcon = createGuiItem(
            Material.BOOK,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Stash Information",
            ChatColor.GRAY + "This is your permanent, secure storage space.",
            ChatColor.GRAY +
                "Only items in the top " +
                STASH_SLOTS +
                " slots are saved."
        );
        displayInv.setItem(INFO_SLOT, infoIcon);



        // Stash Stats/Usage (Slot 49 - Center)
        int usedSlots = 0;
        for (int i = 0; i < STASH_SLOTS; i++) {
            if (
                displayInv.getItem(i) != null &&
                displayInv.getItem(i).getType() != Material.AIR
            ) {
                usedSlots++;
            }
        }

        ItemStack statsIcon = createGuiItem(
            Material.ENDER_CHEST,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Stash Usage",
            ChatColor.WHITE +
                "Status: " +
                ChatColor.YELLOW +
                usedSlots +
                "/" +
                STASH_SLOTS +
                " Slots Used",
            "",
            ChatColor.GRAY + "Items are saved automatically on close."
        );
        displayInv.setItem(STATS_SLOT, statsIcon);

        // Open Sell Menu Button (Slot 51)
        ItemStack sellButton = createGuiItem(
            Material.EMERALD,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Open Sell Menu",
            ChatColor.GRAY + "Sell materials and gear for money.",
            ChatColor.YELLOW + "Click to execute /sell"
        );
        displayInv.setItem(SELL_SLOT, sellButton);



        // Close Stash Button (Slot 52)
        ItemStack exitIcon = createGuiItem(
            Material.BARRIER,
            ChatColor.RED + "" + ChatColor.BOLD + "Close & Save Stash",
            ChatColor.GRAY + "Closing the inventory confirms all changes.",
            ChatColor.GRAY + "Saves items in the top " + STASH_SLOTS + " slots."
        );
        displayInv.setItem(EXIT_SLOT, exitIcon);

        return displayInv;
    }

    // --- Command Executor ---
    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player) sender;

        // Open the visually enhanced GUI
        Inventory gui = createStashGUI(player);
        player.openInventory(gui);

        return true;
    }

    // --- Event Handlers ---

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(STASH_GUI_TITLE)) return;

        // Block all clicks in the custom GUI area (slots 45-53)
        int rawSlot = event.getRawSlot();
        if (rawSlot >= STASH_SLOTS && rawSlot < INVENTORY_SIZE) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();

            // Handle specific button clicks
            if (rawSlot == SELL_SLOT) {
                player.closeInventory();
                Bukkit.dispatchCommand(player, "sell");
                player.sendMessage(
                    ChatColor.YELLOW + "Opening the Sell Menu..."
                );

            } else if (rawSlot == EXIT_SLOT) {
                player.closeInventory();
            }
        }
        // Allows interaction (placing/taking) with the actual stash slots (0-44)
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        // Check if this is the custom stash GUI
        if (event.getView().getTitle().equals(STASH_GUI_TITLE)) {
            Player player = (Player) event.getPlayer();
            Inventory displayInv = event.getInventory();

            // 1. Get the actual stash inventory (which StashManager is tracking)
            Inventory rawStashInv = stashManager.getStash(player);

            // 2. Clear the raw stash inventory
            // We only care about saving the top 45 slots of the display inventory.

            // NOTE: If the rawStashInv size is smaller than 54, this prevents an ArrayIndexOutOfBounds.
            int saveSize = Math.min(STASH_SLOTS, rawStashInv.getSize());

            // 3. Transfer the contents from the display GUI's stash slots (0-44) to the raw stash
            // We clear the raw stash first to remove old/unwanted items if the player took them out.
            rawStashInv.clear();
            for (int i = 0; i < saveSize; i++) {
                // Copy the item from the display to the raw stash
                rawStashInv.setItem(i, displayInv.getItem(i));
            }

            // 4. Save the raw stash inventory.
            stashManager.saveStash(player);

            player.sendMessage(
                ChatColor.GREEN + "Stash contents updated and saved!"
            );
        }
    }
}
