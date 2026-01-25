package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.ChestManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

public class ClaimChestCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;
    private final ChestManager chestManager;

    public ClaimChestCommand(ExtractionPlugin plugin, ChestManager chestManager) {
        this.plugin = plugin;
        this.chestManager = chestManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Find the chest the player is looking at
        Block targetBlock = getTargetChestBlock(player);
        if (targetBlock == null || !chestManager.isChestMaterial(targetBlock.getType())) {
            player.sendMessage(ChatColor.RED + "You must be looking at a chest to claim it.");
            return true;
        }

        if (chestManager.isChestClaimed(targetBlock.getLocation())) {
            player.sendMessage(ChatColor.RED + "This chest is already claimed.");
            return true;
        }

        boolean isDouble = chestManager.isDoubleChest(targetBlock.getLocation());
        int ironCost = isDouble ? 30 : 15;

        // Check for enough iron in inventory
        if (!hasEnoughIron(player, ironCost)) {
            player.sendMessage(ChatColor.RED + "You need " + ironCost + " iron ingots to claim this " + (isDouble ? "double chest" : "chest") + ".");
            return true;
        }

        // Take iron
        removeIron(player, ironCost);

        // Claim the chest
        if (chestManager.claimChest(player.getUniqueId(), targetBlock.getLocation())) {
            player.sendMessage(ChatColor.GREEN + "Chest claimed successfully!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to claim chest.");
        }

        return true;
    }

    private Block getTargetChestBlock(Player player) {
        BlockIterator iterator = new BlockIterator(player, 5); // 5 block range
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (chestManager.isChestMaterial(block.getType())) {
                return block;
            }
        }
        return null;
    }

    private boolean hasEnoughIron(Player player, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.IRON_INGOT) {
                count += item.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    private void removeIron(Player player, int amount) {
        for (int i = 0; i < player.getInventory().getSize() && amount > 0; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == Material.IRON_INGOT) {
                int itemAmount = item.getAmount();
                if (itemAmount <= amount) {
                    player.getInventory().setItem(i, null);
                    amount -= itemAmount;
                } else {
                    item.setAmount(itemAmount - amount);
                    amount = 0;
                }
            }
        }
    }
}