package com.extraction.managers;

import com.extraction.economy.EconomyManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeManager {
    private JavaPlugin plugin;
    private EconomyManager economyManager;
    private Map<UUID, TradeOffer> pendingTrades = new HashMap<>();
    private Map<UUID, Player> currentTradeTargets = new HashMap<>();
    private Map<UUID, Long> currentAmounts = new HashMap<>();

    public TradeManager(JavaPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    public void sendTradeOffer(Player sender, Player receiver, long amount) {
        if (amount <= 0) {
            sender.sendMessage("Amount must be positive.");
            return;
        }
        double bal = economyManager.getBalanceAsDouble(sender.getUniqueId());
        if (bal < amount) {
            sender.sendMessage("You don't have enough money.");
            return;
        }
        TradeOffer offer = new TradeOffer(sender.getUniqueId(), receiver.getUniqueId(), amount);
        pendingTrades.put(receiver.getUniqueId(), offer);
        sender.sendMessage("Trade offer sent to " + receiver.getName() + " for $" + amount);
        TextComponent message = new TextComponent(sender.getName() + " wants to trade $" + amount + ". ");
        TextComponent accept = new TextComponent("[Accept]");
        accept.setColor(ChatColor.GREEN);
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accepttrade"));
        TextComponent decline = new TextComponent(" [Decline]");
        decline.setColor(ChatColor.RED);
        decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/declinetrade"));
        message.addExtra(accept);
        message.addExtra(decline);
        receiver.spigot().sendMessage(message);
        sender.playSound(sender.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
        receiver.playSound(receiver.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    public void acceptTrade(Player player) {
        TradeOffer offer = pendingTrades.get(player.getUniqueId());
        if (offer == null) {
            player.sendMessage("No pending trade offer.");
            return;
        }
        Player sender = Bukkit.getPlayer(offer.getSender());
        if (sender == null) {
            player.sendMessage("Sender is offline.");
            pendingTrades.remove(player.getUniqueId());
            return;
        }
        double bal = economyManager.getBalanceAsDouble(offer.getSender());
        if (bal < offer.getAmount()) {
            player.sendMessage("Sender no longer has enough money.");
            sender.sendMessage("Trade failed: insufficient funds.");
            pendingTrades.remove(player.getUniqueId());
            return;
        }
        economyManager.takeBalance(offer.getSender(), offer.getAmount());
        economyManager.addBalance(player.getUniqueId(), offer.getAmount());
        player.sendMessage("Trade accepted! Received $" + offer.getAmount());
        sender.sendMessage("Trade accepted! Sent $" + offer.getAmount());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        if (sender != null) sender.playSound(sender.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        pendingTrades.remove(player.getUniqueId());
    }

    public void declineTrade(Player player) {
        TradeOffer offer = pendingTrades.remove(player.getUniqueId());
        if (offer == null) {
            player.sendMessage("No pending trade offer.");
            return;
        }
        Player sender = Bukkit.getPlayer(offer.getSender());
        if (sender != null) {
            sender.sendMessage(player.getName() + " declined your trade offer.");
        }
        player.sendMessage("Trade offer declined.");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        if (sender != null) sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }

    public void openSenderGUI(Player player) {
        currentAmounts.put(player.getUniqueId(), 0L);
        Inventory inv = Bukkit.createInventory(null, 27, "Trade Amount");
        // Display amount
        updateAmountDisplay(inv, player.getUniqueId());
        // +1
        ItemStack plus1 = new ItemStack(Material.EMERALD);
        ItemMeta plus1Meta = plus1.getItemMeta();
        plus1Meta.setDisplayName("§a+1 Dollar");
        plus1Meta.setLore(java.util.Arrays.asList("§7Click to add $1 to the trade amount"));
        plus1.setItemMeta(plus1Meta);
        inv.setItem(10, plus1);
        // +10
        ItemStack plus10 = new ItemStack(Material.GOLD_INGOT);
        ItemMeta plus10Meta = plus10.getItemMeta();
        plus10Meta.setDisplayName("§e+10 Dollars");
        plus10Meta.setLore(java.util.Arrays.asList("§7Click to add $10 to the trade amount"));
        plus10.setItemMeta(plus10Meta);
        inv.setItem(11, plus10);
        // +100
        ItemStack plus100 = new ItemStack(Material.DIAMOND);
        ItemMeta plus100Meta = plus100.getItemMeta();
        plus100Meta.setDisplayName("§b+100 Dollars");
        plus100Meta.setLore(java.util.Arrays.asList("§7Click to add $100 to the trade amount"));
        plus100.setItemMeta(plus100Meta);
        inv.setItem(12, plus100);
        // +1000
        ItemStack plus1000 = new ItemStack(Material.NETHERITE_INGOT);
        ItemMeta plus1000Meta = plus1000.getItemMeta();
        plus1000Meta.setDisplayName("§5+1000 Dollars");
        plus1000Meta.setLore(java.util.Arrays.asList("§7Click to add $1000 to the trade amount"));
        plus1000.setItemMeta(plus1000Meta);
        inv.setItem(13, plus1000);
        // Clear
        ItemStack clear = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta clearMeta = clear.getItemMeta();
        clearMeta.setDisplayName("§cClear Amount");
        clearMeta.setLore(java.util.Arrays.asList("§7Reset the trade amount to $0"));
        clear.setItemMeta(clearMeta);
        inv.setItem(14, clear);
        // Confirm
        ItemStack confirm = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName("§aSend Trade Offer");
        confirmMeta.setLore(java.util.Arrays.asList("§7Send the trade offer to the selected player"));
        confirm.setItemMeta(confirmMeta);
        inv.setItem(16, confirm);
        // Cancel
        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§cCancel Trade");
        cancelMeta.setLore(java.util.Arrays.asList("§7Close the GUI without sending an offer"));
        cancel.setItemMeta(cancelMeta);
        inv.setItem(17, cancel);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    private void updateAmountDisplay(Inventory inv, UUID player) {
        long amount = currentAmounts.getOrDefault(player, 0L);
        ItemStack display = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta displayMeta = display.getItemMeta();
        displayMeta.setDisplayName("§6Current Trade Amount");
        displayMeta.setLore(java.util.Arrays.asList("§e$" + amount, "§7Click buttons below to adjust"));
        display.setItemMeta(displayMeta);
        inv.setItem(4, display);
    }



    public TradeOffer getPendingTrade(UUID player) {
        return pendingTrades.get(player);
    }

    public void handleSenderClick(Player player, int slot, Inventory inv) {
        long amount = currentAmounts.getOrDefault(player.getUniqueId(), 0L);
        if (slot == 10) { // +1
            amount += 1;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        } else if (slot == 11) { // +10
            amount += 10;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
        } else if (slot == 12) { // +100
            amount += 100;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.4f);
        } else if (slot == 13) { // +1000
            amount += 1000;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.6f);
        } else if (slot == 14) { // clear
            amount = 0;
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
        } else if (slot == 16) { // confirm
            Player target = currentTradeTargets.get(player.getUniqueId());
            if (target != null && amount > 0) {
                sendTradeOffer(player, target, amount);
                player.closeInventory();
                currentTradeTargets.remove(player.getUniqueId());
                currentAmounts.remove(player.getUniqueId());
            } else {
                player.sendMessage("Invalid trade.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
            return;
        } else if (slot == 17) { // cancel
            player.closeInventory();
            currentTradeTargets.remove(player.getUniqueId());
            currentAmounts.remove(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.5f);
            return;
        } else {
            return; // not a button
        }
        currentAmounts.put(player.getUniqueId(), amount);
        updateAmountDisplay(inv, player.getUniqueId());
    }



    public void setCurrentTradeTarget(Player player, Player target) {
        currentTradeTargets.put(player.getUniqueId(), target);
    }

    public static class TradeOffer {
        private UUID sender;
        private UUID receiver;
        private long amount;

        public TradeOffer(UUID sender, UUID receiver, long amount) {
            this.sender = sender;
            this.receiver = receiver;
            this.amount = amount;
        }

        public UUID getSender() {
            return sender;
        }

        public UUID getReceiver() {
            return receiver;
        }

        public long getAmount() {
            return amount;
        }
    }
}