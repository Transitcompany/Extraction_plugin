package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.data.PlayerDataManager;
import com.extraction.economy.EconomyManager;
import com.extraction.leveling.LevelingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProfileCommand implements CommandExecutor, TabCompleter, Listener {
    private final ExtractionPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final EconomyManager economyManager;
    private final LevelingManager levelingManager;

    public ProfileCommand(ExtractionPlugin plugin, PlayerDataManager playerDataManager,
                         EconomyManager economyManager, LevelingManager levelingManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.economyManager = economyManager;
        this.levelingManager = levelingManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        OfflinePlayer target;
        
        if (args.length == 0) {
            target = player;
        } else {
            if (!sender.hasPermission("extraction.admin")) {
                player.sendMessage(ChatColor.RED + "You can only view your own profile!");
                return true;
            }
            target = Bukkit.getOfflinePlayer(args[0]);
        }

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player '" + target.getName() + "' has never played on this server.");
            return true;
        }

        PlayerDataManager.PlayerData data = playerDataManager.getPlayerData(target);
        openProfileGUI(player, target, data);
        
        return true;
    }

    private void openProfileGUI(Player viewer, OfflinePlayer target, PlayerDataManager.PlayerData data) {
        Inventory gui = Bukkit.createInventory(null, 54, 
            ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Profile: " + ChatColor.GOLD + target.getName());

        // Fill empty slots with gray stained glass
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", "");
        for (int i = 0; i < 54; i++) {
            if (!isSpecialSlot(i)) {
                gui.setItem(i, filler);
            }
        }

        // Player info item (center top) - using custom item instead of head to avoid texture issues
        ItemStack playerInfo = createItem(Material.ENCHANTED_GOLDEN_APPLE,
            ChatColor.GOLD + "" + ChatColor.BOLD + target.getName(),
            "",
            ChatColor.GRAY + "UUID: " + ChatColor.DARK_GRAY + data.getUuid().toString().substring(0, 8) + "...",
            ChatColor.GRAY + "Rank: " + getRankName(data.getLevel()),
            "",
            ChatColor.YELLOW + "" + ChatColor.BOLD + "Quick Stats:",
            ChatColor.AQUA + "  Level " + data.getLevel() + " " + ChatColor.GRAY + "• " + ChatColor.GREEN + "$" + String.format("%.0f", economyManager.getBalance(data.getUuid())),
            ChatColor.GRAY + "  " + data.getExtractionsCompleted() + " extractions" + ChatColor.GRAY + " • " + data.getItemsSold() + " items sold");
        gui.setItem(13, playerInfo);

        // Level section (left side)
        ItemStack levelItem = createItem(Material.EXPERIENCE_BOTTLE,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Level & XP",
            "",
            ChatColor.YELLOW + "Level: " + ChatColor.WHITE + data.getLevel(),
            ChatColor.AQUA + "Current XP: " + ChatColor.WHITE + String.format("%.1f", data.getXp()),
            ChatColor.AQUA + "XP Needed: " + ChatColor.WHITE + String.format("%.1f", data.getXpNeededForNextLevel()),
            ChatColor.LIGHT_PURPLE + "Total XP: " + ChatColor.WHITE + String.format("%.1f", data.getTotalXp()),
            "",
            createProgressBar(data.getXpProgress()),
            "",
            ChatColor.GRAY + "Progress: " + ChatColor.GREEN + String.format("%.1f", data.getXpProgress() * 100) + "%");
        gui.setItem(19, levelItem);

        // Economy section (middle left)
        ItemStack economyItem = createItem(Material.GOLD_BLOCK,
            ChatColor.GOLD + "" + ChatColor.BOLD + "Economy",
            "",
            ChatColor.YELLOW + "Balance: " + ChatColor.GREEN + "$" + String.format("%.2f", economyManager.getBalance(data.getUuid())),
            ChatColor.GREEN + "Total Earned: " + ChatColor.WHITE + "$" + String.format("%.2f", data.getTotalMoneyEarned()),
            ChatColor.RED + "Total Spent: " + ChatColor.WHITE + "$" + String.format("%.2f", data.getTotalMoneySpent()),
            "",
            ChatColor.GRAY + "Net Profit: " + (data.getTotalMoneyEarned() > data.getTotalMoneySpent() ? 
                ChatColor.GREEN + "$" + String.format("%.2f", data.getTotalMoneyEarned() - data.getTotalMoneySpent()) :
                ChatColor.RED + "-$" + String.format("%.2f", data.getTotalMoneySpent() - data.getTotalMoneyEarned())));
        gui.setItem(22, economyItem);

        // Activities section (middle right)
        ItemStack activitiesItem = createItem(Material.BOOK,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Activities",
            "",
            ChatColor.YELLOW + "Extractions: " + ChatColor.WHITE + data.getExtractionsCompleted(),
            ChatColor.YELLOW + "Items Sold: " + ChatColor.WHITE + data.getItemsSold(),
            ChatColor.YELLOW + "Auctions Won: " + ChatColor.WHITE + data.getAuctionsWon(),
            ChatColor.YELLOW + "Auctions Created: " + ChatColor.WHITE + data.getAuctionsCreated(),
            "",
            ChatColor.GRAY + "Total Transactions: " + ChatColor.WHITE + (data.getAuctionsWon() + data.getAuctionsCreated()));
        gui.setItem(25, activitiesItem);

        // Bonuses section (right side)
        ItemStack bonusesItem = createItem(Material.ENCHANTED_GOLDEN_APPLE,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Level Bonuses",
            "",
            ChatColor.GREEN + "Sell Multiplier: " + ChatColor.WHITE + "+" + String.format("%.0f", 
                (levelingManager.getSellMultiplier(data.getLevel()) - 1.0) * 100) + "%",
            ChatColor.GREEN + "Extraction Bonus: " + ChatColor.WHITE + "+" + String.format("%.0f", 
                (levelingManager.getExtractionBonus(data.getLevel()) - 1.0) * 100) + "%",
            "",
            ChatColor.GRAY + "Higher levels = better rewards!");
        if (data.getLevel() >= 50) {
            ItemMeta meta = bonusesItem.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                bonusesItem.setItemMeta(meta);
            }
        }
        gui.setItem(31, bonusesItem);

        // Timeline section (bottom left)
        ItemStack timelineItem = createItem(Material.CLOCK,
            ChatColor.BLUE + "" + ChatColor.BOLD + "Timeline",
            "",
            ChatColor.YELLOW + "First Joined: " + ChatColor.WHITE + formatDate(data.getFirstJoin()),
            target.isOnline() ? 
                ChatColor.GREEN + "Status: " + ChatColor.WHITE + "Online Now" :
                ChatColor.GRAY + "Last Seen: " + ChatColor.WHITE + getRelativeTime(data.getLastSeen()),
            "",
            ChatColor.GRAY + "Account Age: " + ChatColor.WHITE + getAccountAge(data.getFirstJoin()));
        gui.setItem(37, timelineItem);

        // Statistics summary (bottom middle)
        ItemStack statsItem = createItem(Material.DIAMOND,
            ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Statistics Summary",
            "",
            ChatColor.YELLOW + "Avg per Extraction: " + ChatColor.WHITE + "$" + 
                (data.getExtractionsCompleted() > 0 ? String.format("%.2f", data.getTotalMoneyEarned() / data.getExtractionsCompleted()) : "0.00"),
            ChatColor.YELLOW + "Avg per Sale: " + ChatColor.WHITE + "$" + 
                (data.getItemsSold() > 0 ? String.format("%.2f", data.getTotalMoneyEarned() / data.getItemsSold()) : "0.00"),
            ChatColor.YELLOW + "Highest Sale: " + ChatColor.GREEN + "$" + String.format("%,.2f", (double) data.getHighestSingleSale()),
            ChatColor.YELLOW + "Success Rate: " + ChatColor.WHITE + 
                (data.getAuctionsCreated() > 0 ? String.format("%.1f", (double) data.getAuctionsWon() / data.getAuctionsCreated() * 100) + "%" : "N/A"),
            "",
            ChatColor.YELLOW + "Monsters Killed: " + ChatColor.RED + data.getMonstersKilled(),
            ChatColor.YELLOW + "Kill Streak: " + ChatColor.WHITE + data.getKillStreak() + " | Best: " + ChatColor.GREEN + data.getHighestKillStreak());
        gui.setItem(40, statsItem);

        // Rank/Title (bottom right)
        ItemStack rankItem = createRankItem(data.getLevel());
        gui.setItem(43, rankItem);

        viewer.openInventory(gui);
    }

    private List<String> createPlayerLore(OfflinePlayer target, PlayerDataManager.PlayerData data) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "UUID: " + ChatColor.DARK_GRAY + data.getUuid().toString().substring(0, 8) + "...");
        lore.add(ChatColor.GRAY + "Rank: " + getRankName(data.getLevel()));
        lore.add("");
        lore.add(ChatColor.YELLOW + "" + ChatColor.BOLD + "Quick Stats:");
        lore.add(ChatColor.AQUA + "  Level " + data.getLevel() + " " + ChatColor.GRAY + "• " + ChatColor.GREEN + "$" + String.format("%.0f", economyManager.getBalance(data.getUuid())));
        lore.add(ChatColor.GRAY + "  " + data.getExtractionsCompleted() + " extractions" + ChatColor.GRAY + " • " + data.getItemsSold() + " items sold");
        return lore;
    }

    private String createProgressBar(double progress) {
        int totalBars = 20;
        int filledBars = (int) (progress * totalBars);
        StringBuilder bar = new StringBuilder(ChatColor.GRAY + "[");
        
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                bar.append(ChatColor.GREEN + "█");
            } else {
                bar.append(ChatColor.DARK_GRAY + "░");
            }
        }
        
        bar.append(ChatColor.GRAY + "]");
        return bar.toString();
    }

    private String getRankName(int level) {
        if (level >= 100) return ChatColor.GOLD + "" + ChatColor.BOLD + "Master Extractor";
        if (level >= 75) return ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Elite";
        if (level >= 50) return ChatColor.AQUA + "" + ChatColor.BOLD + "Expert";
        if (level >= 25) return ChatColor.GREEN + "" + ChatColor.BOLD + "Veteran";
        if (level >= 10) return ChatColor.YELLOW + "" + ChatColor.BOLD + "Skilled";
        if (level >= 5) return ChatColor.GRAY + "" + ChatColor.BOLD + "Experienced";
        return ChatColor.WHITE + "Beginner";
    }

    private ItemStack createRankItem(int level) {
        Material material;
        String displayName;
        String[] lore;
        
        if (level >= 100) {
            material = Material.NETHER_STAR;
            displayName = ChatColor.GOLD + "" + ChatColor.BOLD + "Master Extractor";
            lore = new String[]{
                "", ChatColor.YELLOW + "You have reached pinnacle!",
                ChatColor.GRAY + "Maximum bonuses unlocked",
                "", ChatColor.GREEN + "Special recognition and rewards"
            };
        } else if (level >= 75) {
            material = Material.DIAMOND_BLOCK;
            displayName = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Elite Extractor";
            lore = new String[]{
                "", ChatColor.GRAY + "Among the top players",
                ChatColor.YELLOW + "Exclusive bonuses available"
            };
        } else if (level >= 50) {
            material = Material.EMERALD_BLOCK;
            displayName = ChatColor.AQUA + "" + ChatColor.BOLD + "Expert Extractor";
            lore = new String[]{
                "", ChatColor.GRAY + "Highly skilled extractor",
                ChatColor.YELLOW + "Enhanced rewards"
            };
        } else if (level >= 25) {
            material = Material.IRON_BLOCK;
            displayName = ChatColor.GREEN + "" + ChatColor.BOLD + "Veteran Extractor";
            lore = new String[]{
                "", ChatColor.GRAY + "Experienced player",
                ChatColor.YELLOW + "Improved bonuses"
            };
        } else if (level >= 10) {
            material = Material.GOLD_INGOT;
            displayName = ChatColor.YELLOW + "" + ChatColor.BOLD + "Skilled Extractor";
            lore = new String[]{
                "", ChatColor.GRAY + "Developing skills",
                ChatColor.YELLOW + "Basic bonuses active"
            };
        } else {
            material = Material.IRON_INGOT;
            displayName = ChatColor.GRAY + "" + ChatColor.BOLD + "Rookie Extractor";
            lore = new String[]{
                "", ChatColor.GRAY + "Just getting started",
                ChatColor.YELLOW + "Keep extracting to level up!"
            };
        }
        
        ItemStack item = createItem(material, displayName, lore);
        if (level >= 50) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
        }
        
        return item;
    }

    private boolean isSpecialSlot(int slot) {
        return slot == 13 || slot == 19 || slot == 22 || slot == 25 || 
               slot == 31 || slot == 37 || slot == 40 || slot == 43;
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(new Date(timestamp));
    }

    private String getRelativeTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long hours = diff / (1000 * 60 * 60);
        long days = hours / 24;
        
        if (days > 0) {
            return days + " days ago";
        } else if (hours > 0) {
            return hours + " hours ago";
        } else {
            return "Recently";
        }
    }

    private String getAccountAge(long firstJoin) {
        long diff = System.currentTimeMillis() - firstJoin;
        long days = diff / (1000 * 60 * 60 * 24);
        
        if (days > 365) {
            return (days / 365) + " years";
        } else if (days > 30) {
            return (days / 30) + " months";
        } else {
            return days + " days";
        }
    }

    private ItemStack createItem(Material material, String displayName, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Profile: ")) {
            return;
        }
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR || 
            clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }
        
        // Add interaction sounds
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("extraction.admin")) {
            String partial = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}