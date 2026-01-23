package com.extraction.listeners;

import com.extraction.ExtractionPlugin;
import com.extraction.extract.ExtractManager;
import com.extraction.data.PlayerDataManager;
import com.extraction.data.PlayerDataManager.PlayerData;
import com.extraction.managers.LoginLogManager;
import com.extraction.managers.TermsOfServiceManager;
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
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class JoinLeaveListener implements Listener {

    private static final NamespacedKey ownerKey = new NamespacedKey("extraction", "owner");
    private static final NamespacedKey creationDateKey = new NamespacedKey("extraction", "creation_date");

    private final ExtractionPlugin plugin;
    private final ExtractManager extractManager;
    private final LoginLogManager loginLogManager;
    private final TermsOfServiceManager tosManager;
    private final Set<UUID> awaitingTosAcceptance = new HashSet<>();
    private final Map<UUID, Long> acceptanceTimers = new HashMap<>();
    private final Random random = new Random();

    public JoinLeaveListener(ExtractionPlugin plugin, ExtractManager extractManager, TermsOfServiceManager tosManager) {
        this.plugin = plugin;
        this.extractManager = extractManager;
        this.loginLogManager = plugin.getLoginLogManager();
        this.tosManager = tosManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        String prefix = data.getRank().getPrefix();
        event.setJoinMessage(ChatColor.BLACK + "[" + ChatColor.AQUA + "+" + ChatColor.BLACK + "] " + prefix + " " + ChatColor.AQUA + player.getName() + " joined");

        // Log login with IP
        String ip = player.getAddress().getAddress().getHostAddress();
        loginLogManager.addLoginEntry(player.getName(), ip);

        if (tosManager.needsToAcceptTerms(player)) {
            awaitingTosAcceptance.add(player.getUniqueId());
            
            // Teleport to lobby world first
            String lobbyWorld = extractManager.getLobbyWorld();
            if (lobbyWorld != null) {
                World world = Bukkit.getWorld(lobbyWorld);
                if (world != null) {
                    Location spawn = world.getSpawnLocation();
                    player.teleport(spawn);
                    player.setFoodLevel(20); // Full hunger in lobby
                }
            }
            
            tosManager.displayTermsOfService(player);
            startAcceptanceTimer(player);
        } else {
            // Handle normal join logic
            boolean isFirstTime = plugin.getFirstTimeJoinManager().isFirstTimeJoin(player.getUniqueId());
            if (isFirstTime) {
                doFirstTimeStuff(player);
            } else {
                // Teleport back to lobby if rejoining in lobby world
                String lobbyWorld = extractManager.getLobbyWorld();
                if (lobbyWorld != null && player.getWorld().getName().equals(lobbyWorld)) {
                    World world = Bukkit.getWorld(lobbyWorld);
                    if (world != null) {
                        Location spawn = world.getSpawnLocation();
                        player.teleport(spawn);
                        player.setFoodLevel(20); // Full hunger in lobby
                    }
                }
            }
        }

        plugin.assignPlayerToTeam(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Cancel timer if player was waiting for ToS acceptance
        if (awaitingTosAcceptance.contains(playerId)) {
            awaitingTosAcceptance.remove(playerId);
            acceptanceTimers.remove(playerId);
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        String prefix = data.getRank().getPrefix();
        event.setQuitMessage(ChatColor.BLACK + "[" + ChatColor.AQUA + "-" + ChatColor.BLACK + "] " + prefix + " " + ChatColor.AQUA + player.getName() + " left");
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        String lobbyWorld = extractManager.getLobbyWorld();
        if (lobbyWorld != null && player.getWorld().getName().equals(lobbyWorld)) {
            // In lobby, keep hunger at max
            event.setFoodLevel(20);
        }
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

    private void sendConsentMessage(Player player) {
        Component message = Component.text("Welcome to VaultMC! We collect data like your IP address for logging purposes.\n")
            .color(NamedTextColor.AQUA)
            .append(Component.text("[YES] ").color(NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/consent yes")))
            .append(Component.text("[NO]").color(NamedTextColor.RED)
                .clickEvent(ClickEvent.runCommand("/consent no")));
        player.sendMessage(message);
    }

    private void doFirstTimeStuff(Player player) {
        // Welcome message
        String welcomeMessage = ChatColor.AQUA + "🌟 Welcome to VaultMC, " + ChatColor.GOLD + player.getName() + ChatColor.AQUA + "! 🌟\n" +
                                 ChatColor.AQUA + "Have fun!";
        player.sendMessage(welcomeMessage);

        // Broadcast in chat
        Bukkit.broadcastMessage(ChatColor.AQUA + "🎉 " + ChatColor.GOLD + player.getName() + ChatColor.AQUA + " has joined VaultMC for the first time! Welcome! 🎉");

        // Give starting kit
        giveStartingKit(player);

        // Teleport to lobby world
        String lobbyWorld = extractManager.getLobbyWorld();
        if (lobbyWorld != null) {
            World world = Bukkit.getWorld(lobbyWorld);
            if (world != null) {
                Location spawn = world.getSpawnLocation();
                player.teleport(spawn);
                player.setFoodLevel(20); // Full hunger in lobby
            }
        }

        // Mark as joined
        plugin.getFirstTimeJoinManager().markJoined(player.getUniqueId(), player.getName());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (awaitingTosAcceptance.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must accept the terms to play!");
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();
            if (message.equals("/accept")) {
                event.setCancelled(true); // Prevent command from executing further
                if (!awaitingTosAcceptance.contains(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You have already accepted the Terms of Service!");
                    return;
                }
                awaitingTosAcceptance.remove(player.getUniqueId());
                tosManager.acceptTerms(player);

        // Cancel the timer if it exists
        cancelAcceptanceTimer(player);

        // Clear slow falling effect
        player.removePotionEffect(PotionEffectType.SLOW_FALLING);

        // Confirmation message
        player.sendMessage(ChatColor.GREEN + "✓ Terms of Service accepted! Welcome to the server.");
        player.playSound(player.getLocation(), "entity.experience_orb.pickup", 1.0f, 1.0f);

        // Check if first time and do first-time stuff
        boolean isFirstTime = plugin.getFirstTimeJoinManager().isFirstTimeJoin(player.getUniqueId());
        if (isFirstTime) {
            doFirstTimeStuff(player);
        }
            }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (awaitingTosAcceptance.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must accept the Terms of Service to chat!");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (awaitingTosAcceptance.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You must accept the Terms of Service to interact!");
        }
    }



    private void startAcceptanceTimer(Player player) {
        UUID playerId = player.getUniqueId();
        acceptanceTimers.put(playerId, System.currentTimeMillis() + 100000); // 100 seconds = 100000ms

        new BukkitRunnable() {
            int secondsLeft = 100;

            @Override
            public void run() {
                Player p = Bukkit.getPlayer(playerId);
                if (p == null || !awaitingTosAcceptance.contains(playerId)) {
                    this.cancel();
                    acceptanceTimers.remove(playerId);
                    return;
                }

                // Update actionbar timer
                Component timerMessage = Component.text("§cAccept Terms of Service in: §e" + secondsLeft + " §cseconds")
                    .color(NamedTextColor.RED);
                p.sendActionBar(timerMessage);

                secondsLeft--;

                if (secondsLeft <= 0) {
                    // Time's up - kick the player
                    p.kick(Component.text("§cYou did not accept the Terms of Service in time. Please try again."));
                    awaitingTosAcceptance.remove(playerId);
                    acceptanceTimers.remove(playerId);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
    }



    private void cancelAcceptanceTimer(Player player) {
        acceptanceTimers.remove(player.getUniqueId());
    }
}