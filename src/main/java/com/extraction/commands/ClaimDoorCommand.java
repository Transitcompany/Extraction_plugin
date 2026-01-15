package com.extraction.commands;

import com.extraction.ExtractionPlugin;
import com.extraction.managers.DoorManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

public class ClaimDoorCommand implements CommandExecutor {

    private final ExtractionPlugin plugin;
    private final DoorManager doorManager;

    public ClaimDoorCommand(ExtractionPlugin plugin, DoorManager doorManager) {
        this.plugin = plugin;
        this.doorManager = doorManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Find the door the player is looking at
        Block targetBlock = getTargetDoorBlock(player);
        if (targetBlock == null || !isDoorMaterial(targetBlock.getType())) {
            player.sendMessage(ChatColor.RED + "You must be looking at a door to claim it.");
            return true;
        }

        if (doorManager.isDoorClaimed(targetBlock.getLocation())) {
            player.sendMessage(ChatColor.RED + "This door is already claimed.");
            return true;
        }

        // Check for 2 iron in inventory
        if (!hasEnoughIron(player, 2)) {
            player.sendMessage(ChatColor.RED + "You need 2 iron ingots to claim this door.");
            return true;
        }

        // Take 2 iron
        removeIron(player, 2);

        // Claim the door
        if (doorManager.claimDoor(player.getUniqueId(), targetBlock.getLocation())) {
            player.sendMessage(ChatColor.GREEN + "Door claimed successfully!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to claim door.");
        }

        return true;
    }

    private Block getTargetDoorBlock(Player player) {
        BlockIterator iterator = new BlockIterator(player, 5); // 5 block range
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (isDoorMaterial(block.getType())) {
                return block;
            }
        }
        return null;
    }

    private boolean isDoorMaterial(Material material) {
        return material.name().endsWith("_DOOR");
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