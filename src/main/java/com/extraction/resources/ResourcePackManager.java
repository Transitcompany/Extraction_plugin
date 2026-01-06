package com.extraction.resources;

import com.extraction.ExtractionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.io.File;
import java.security.MessageDigest;
import java.util.logging.Level;

public class ResourcePackManager implements Listener, CommandExecutor {
    
    private final ExtractionPlugin plugin;
    private final File resourcePackFile;
    private String resourcePackHash;
    private boolean forceResourcePack = true;
    
    public ResourcePackManager(ExtractionPlugin plugin) {
        this.plugin = plugin;
        this.resourcePackFile = new File(plugin.getDataFolder(), "extraction-resourcepack.zip");
        this.resourcePackHash = "00000000000000000000000000000000000000000";
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Register command
        plugin.getCommand("disableresourcepack").setExecutor(this);
    }
    
    private void setupResourcePack() {
        try {
            // Just create placeholder file for now
            if (!resourcePackFile.exists()) {
                resourcePackFile.createNewFile();
            }
            
            // Generate hash
            generateResourcePackHash();
            
            plugin.getLogger().info("Resource pack setup completed!");
            plugin.getLogger().info("Resource pack file: " + resourcePackFile.getAbsolutePath());
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to setup resource pack!", e);
        }
    }
    
    private void generateResourcePackHash() {
        try {
            if (resourcePackFile.exists()) {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                try (java.io.FileInputStream is = new java.io.FileInputStream(resourcePackFile)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = is.read(buffer)) > 0) {
                        digest.update(buffer, 0, read);
                    }
                }
                
                byte[] hashBytes = digest.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) {
                    sb.append(String.format("%02x", b));
                }
                
                String fullHash = sb.toString();
                resourcePackHash = fullHash.length() > 40 ? fullHash.substring(0, 40) : fullHash;
                
                plugin.getLogger().info("Generated resource pack hash: " + resourcePackHash);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not generate resource pack hash: " + e.getMessage());
            resourcePackHash = "00000000000000000000000000000000000000000000";
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (forceResourcePack) {
            Player player = event.getPlayer();
            // Messages removed as requested
        }
    }
    
    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        
        switch (status) {
            case SUCCESSFULLY_LOADED:
                player.sendMessage(ChatColor.GREEN + "Extraction resource pack loaded successfully!");
                break;
            case DECLINED:
                player.sendMessage(ChatColor.RED + "Resource pack declined. Some items may not display correctly.");
                break;
            case FAILED_DOWNLOAD:
                player.sendMessage(ChatColor.RED + "Failed to download resource pack. Please reconnect.");
                break;
            case ACCEPTED:
                break;
            case DOWNLOADED:
                player.sendMessage(ChatColor.YELLOW + "Resource pack downloaded successfully!");
                break;
            case INVALID_URL:
                player.sendMessage(ChatColor.RED + "Invalid resource pack URL.");
                break;
            case FAILED_RELOAD:
                player.sendMessage(ChatColor.RED + "Failed to reload resource pack.");
                break;
            case DISCARDED:
                player.sendMessage(ChatColor.YELLOW + "Resource pack discarded.");
                break;
        }
    }
    
    public void setForceResourcePack(boolean force) {
        this.forceResourcePack = force;
    }
    
    public boolean isForceResourcePack() {
        return forceResourcePack;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("extraction.resourcepack.manage")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "/disableresourcepack - Toggle resource pack enforcement");
            player.sendMessage(ChatColor.GRAY + "Current status: " + 
                (forceResourcePack ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            return true;
        }
        
        if (args[0].equalsIgnoreCase("toggle")) {
            forceResourcePack = !forceResourcePack;
            String status = forceResourcePack ? "enabled" : "disabled";
            
            player.sendMessage(ChatColor.GREEN + "Resource pack enforcement " + status + ".");
            
            plugin.getLogger().info("Resource pack enforcement " + status + " by " + player.getName());
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /disableresourcepack toggle");
        }
        
        return true;
    }
}