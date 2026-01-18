package com.extraction;

import com.extraction.auction.AuctionManager;

import com.extraction.commands.AuctionCommand;
import com.extraction.commands.BalanceCommand;
import com.extraction.commands.ClaimChestCommand;
import com.extraction.commands.ClaimDoorCommand;

import com.extraction.commands.ExtractGiveCommand;
import com.extraction.commands.ExtractOutBannerCommand;
import com.extraction.commands.ExtractPointCommand;
import com.extraction.commands.GiveMoneyCommand;
import com.extraction.commands.GiveRankCommand;
import com.extraction.commands.PayCommand;
import com.extraction.commands.LootChestSetCommand;
import com.extraction.commands.ProfileCommand;
import com.extraction.commands.ReportCommand;
import com.extraction.commands.ResetLootCommand;
import com.extraction.commands.RespawnHighLocCommand;
import com.extraction.commands.SellCommand;
import com.extraction.commands.ServerMapCommand;
import com.extraction.commands.SetExtractToPointCommand;
import com.extraction.commands.SetHighLocCommand;
import com.extraction.commands.SetMoneyCommand;
import com.extraction.commands.SetReportWebhookCommand;
import com.extraction.commands.SetServerMapCommand;
import com.extraction.commands.SetWorldCommand;
import com.extraction.commands.StashCommand;
import com.extraction.commands.TeamCommand;

import com.extraction.commands.ValueCommand;
import com.extraction.commands.WipeCommand;
import com.extraction.crypto.CryptoManager;
import com.extraction.resources.ResourcePackManager;
import com.extraction.data.Rank;
import com.extraction.data.PlayerDataManager.PlayerData;
import com.extraction.loot.LootContainerManager;
import com.extraction.loot.LootTableManager;
import com.extraction.stash.StashManager;
import com.extraction.extract.ExtractManager;
import com.extraction.economy.EconomyManager;
import com.extraction.auction.AuctionManager;
import com.extraction.data.PlayerDataManager;
import com.extraction.leveling.LevelingManager;

import com.extraction.managers.ChestManager;
import com.extraction.managers.DoorManager;
import com.extraction.managers.TeamManager;
import com.extraction.managers.FirstTimeJoinManager;
import com.extraction.managers.ChatModerationManager;
import com.extraction.managers.HighLocManager;
import com.extraction.managers.ReportManager;
import com.extraction.managers.ServerMapManager;
import com.extraction.placeholders.BalancePlaceholder;
import com.extraction.placeholders.TeamPlaceholder;
import com.extraction.listeners.BannerListener;
import com.extraction.listeners.ChestListener;
import com.extraction.listeners.ContainerListener;
import com.extraction.listeners.CryptoWalletListener;
import com.extraction.listeners.CustomItemListener;
import com.extraction.listeners.DeathListener;
import com.extraction.listeners.DoorListener;
import com.extraction.listeners.FishingListener;
import com.extraction.listeners.HighLocListener;
import com.extraction.listeners.JoinLeaveListener;
import com.extraction.listeners.ProximityChatListener;

import com.extraction.listeners.ChatModerationListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Map;

public class ExtractionPlugin extends JavaPlugin {

    private LootContainerManager lootContainerManager;
    private LootTableManager lootTableManager;
    private StashManager stashManager;

    private ExtractManager extractManager;
    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private PlayerDataManager playerDataManager;
    private LevelingManager levelingManager;
    private CryptoManager cryptoManager;
    private ResourcePackManager resourcePackManager;
    
    private ChestManager chestManager;
    private DoorManager doorManager;
    private TeamManager teamManager;
    private FirstTimeJoinManager firstTimeJoinManager;
    private ChatModerationManager chatModerationManager;
    private ReportManager reportManager;
    private ServerMapManager serverMapManager;
    private HighLocManager highLocManager;
    private String lobbyWorld = "world";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.playerDataManager = new PlayerDataManager(this);
        this.levelingManager = new LevelingManager(this, playerDataManager);
        this.lootTableManager = new LootTableManager(this);
        this.lootContainerManager = new LootContainerManager(
            this,
            lootTableManager
        );
        this.economyManager = new EconomyManager(this);
        this.stashManager = new StashManager(this);

        this.extractManager = new ExtractManager(this, levelingManager);
        this.auctionManager = new AuctionManager(this, economyManager);
        this.cryptoManager = new CryptoManager(this, economyManager);
         this.resourcePackManager = new ResourcePackManager(this);
           
           this.chestManager = new ChestManager(this);
           this.doorManager = new DoorManager(this);
           this.teamManager = new TeamManager();
           this.firstTimeJoinManager = new FirstTimeJoinManager(this);
           this.chatModerationManager = new ChatModerationManager(this);
           this.reportManager = new ReportManager(this);
            this.serverMapManager = new ServerMapManager(this);
            this.highLocManager = new HighLocManager(this);

        // Add custom campfire recipe for rotten flesh to leather
        CampfireRecipe rottenFleshRecipe = new CampfireRecipe(
            new NamespacedKey(this, "rotten_flesh_to_leather"),
            new ItemStack(Material.LEATHER),
            Material.ROTTEN_FLESH,
            0.35f, // Experience
            600 // Cook time in ticks (30 seconds)
        );
        Bukkit.addRecipe(rottenFleshRecipe);

        // Add custom campfire recipe for bone to bone meal
        CampfireRecipe boneRecipe = new CampfireRecipe(
            new NamespacedKey(this, "bone_to_bone_meal"),
            new ItemStack(Material.BONE_MEAL, 3), // 3 bone meal per bone
            Material.BONE,
            0.1f, // Experience
            300 // Cook time in ticks (15 seconds)
        );
        Bukkit.addRecipe(boneRecipe);

        registerCommands();
        registerListeners();

        // Start high loc spawner task
        startHighLocSpawner();

        // Register PlaceholderAPI expansion if present
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BalancePlaceholder(this).register();
            new TeamPlaceholder(this).register();
            getLogger().info("Registered %Vbalance% and %Extract_team% placeholders with PlaceholderAPI.");
        }

        getLogger().info("Extraction plugin enabled!");
    }

    private void registerCommands() {
        getCommand("lootchestset").setExecutor(
            new LootChestSetCommand(this, lootContainerManager)
        );
        getCommand("resetloot").setExecutor(
            new ResetLootCommand(this, lootContainerManager)
        );

        getCommand("stash").setExecutor(new StashCommand(this, stashManager));
        getCommand("extractpoint").setExecutor(
            new ExtractPointCommand(this, extractManager)
        );
        getCommand("extractoutbanner").setExecutor(
            new ExtractOutBannerCommand(this, extractManager)
        );
        getCommand("setworld").setExecutor(
            new SetWorldCommand(this, extractManager)
        );
        getCommand("balance").setExecutor(new BalanceCommand(economyManager));
        getCommand("pay").setExecutor(new PayCommand(economyManager));
        getCommand("setmoney").setExecutor(
            new SetMoneyCommand(economyManager)
        );
        getCommand("givemoney").setExecutor(
            new GiveMoneyCommand(economyManager)
        );
        getCommand("sell").setExecutor(new SellCommand(this, economyManager, levelingManager));
        getCommand("value").setExecutor(new ValueCommand(this, economyManager, levelingManager));
        getCommand("setextracttopoint").setExecutor(
            new SetExtractToPointCommand(this, extractManager)
        );

        ExtractGiveCommand extractGiveCommand = new ExtractGiveCommand(
            this
        );
        getCommand("extractgive").setExecutor(extractGiveCommand);
        getCommand("extractgive").setTabCompleter(extractGiveCommand);

        getCommand("auction").setExecutor(
            new AuctionCommand(this, auctionManager, economyManager)
        );
        
        getCommand("profile").setExecutor(
            new ProfileCommand(this, playerDataManager, economyManager, levelingManager)
        );
        getCommand("wipe").setExecutor(
            new WipeCommand(this, playerDataManager, economyManager, stashManager)
        );


        getCommand("claimdoor").setExecutor(
            new ClaimDoorCommand(this, doorManager)
        );
        getCommand("claimchest").setExecutor(
            new ClaimChestCommand(this, chestManager)
        );
        getCommand("report").setExecutor(
            new ReportCommand(this, reportManager)
        );
        getCommand("setreporthook").setExecutor(
            new SetReportWebhookCommand(this, reportManager)
        );
        getCommand("servermap").setExecutor(
            new ServerMapCommand(this, serverMapManager)
        );
        getCommand("setservermap").setExecutor(
            new SetServerMapCommand(this, serverMapManager)
        );
        getCommand("sethighloc").setExecutor(
            new SetHighLocCommand(this, highLocManager)
        );
        getCommand("respawnhighloc").setExecutor(
            new RespawnHighLocCommand(this, highLocManager)
        );
        getCommand("giverank").setExecutor(
            new GiveRankCommand(this)
        );
        getCommand("team").setExecutor(
            new TeamCommand(this)
        );


    }

    private void registerListeners() {
        getServer()
            .getPluginManager()
            .registerEvents(
                new ContainerListener(this, lootContainerManager),
                this
            );
        getServer()
            .getPluginManager()
            .registerEvents(new DeathListener(this, extractManager), this);
        getServer()
            .getPluginManager()
            .registerEvents(new BannerListener(this, extractManager), this);
        getServer()
            .getPluginManager()
            .registerEvents(new FishingListener(this), this);
        getServer()
            .getPluginManager()
            .registerEvents(new JoinLeaveListener(this, extractManager), this);
        getServer()
            .getPluginManager()
            .registerEvents(new ChestListener(this, chestManager), this);
        getServer()
            .getPluginManager()
            .registerEvents(new DoorListener(this, doorManager), this);
        getServer()
            .getPluginManager()
            .registerEvents(new HighLocListener(this, highLocManager), this);
        getServer()
            .getPluginManager()
            .registerEvents(new ChatModerationListener(chatModerationManager), this);
        // temperature subsystem removed
    }

    public void assignPlayerToTeam(Player player) {
        PlayerDataManager.PlayerData data = playerDataManager.getPlayerData(player);
        String prefix = data.getRank().getPrefix();
        String command = "tab player " + player.getName() + " tabprefix " + prefix.replace("§", "&");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        player.setPlayerListName(prefix + " " + player.getName());
    }

    public LootContainerManager getLootContainerManager() {
        return lootContainerManager;
    }

    public StashManager getStashManager() {
        return stashManager;
    }



    public ExtractManager getExtractManager() {
        return extractManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public LootTableManager getLootTableManager() {
        return lootTableManager;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public LevelingManager getLevelingManager() {
        return levelingManager;
    }

    public CryptoManager getCryptoManager() {
        return cryptoManager;
    }

    public ResourcePackManager getResourcePackManager() {
        return resourcePackManager;
    }

    

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public ChestManager getChestManager() {
        return chestManager;
    }

    public DoorManager getDoorManager() {
        return doorManager;
    }

    public FirstTimeJoinManager getFirstTimeJoinManager() {
        return firstTimeJoinManager;
    }

    public ChatModerationManager getChatModerationManager() {
        return chatModerationManager;
    }

    public ReportManager getReportManager() {
        return reportManager;
    }

    public ServerMapManager getServerMapManager() {
        return serverMapManager;
    }

    public HighLocManager getHighLocManager() {
        return highLocManager;
    }

    private void startHighLocSpawner() {
        // Run every 20 minutes (24000 ticks)
        getServer().getScheduler().runTaskTimer(this, () -> {
            respawnHighLocs();
        }, 0L, 24000L); // 20 minutes
    }

    public void respawnHighLocs() {
        for (Map.Entry<Location, String> entry : highLocManager.getHighLocs().entrySet()) {
            Location loc = entry.getKey();
            String type = entry.getValue();
            // Check nearby mobs of type
            int count = 0;
            boolean hasWither = false;
            for (org.bukkit.entity.Entity entity : loc.getWorld().getNearbyEntities(loc, 20, 10, 20)) {
                if (isHighLocMob(entity, type)) {
                    count++;
                    if (entity instanceof org.bukkit.entity.WitherSkeleton || entity instanceof org.bukkit.entity.Husk) {
                        hasWither = true;
                    }
                }
            }
            int toSpawn = 5 - count;
            if (toSpawn > 0) {
                boolean spawnedWither = hasWither;
                // Spawn with delay over 3 ticks per mob
                for (int i = 0; i < toSpawn; i++) {
                    final int index = i;
                    final boolean forceWither = !spawnedWither && index == 0 && Math.random() < 0.1; // 10% chance for wither on first
                    if (forceWither) spawnedWither = true;
                    getServer().getScheduler().runTaskLater(this, () -> {
                        spawnHighLocMob(loc, type, forceWither);
                    }, index * 4L); // 4 ticks delay between spawns
                }
            }
        }
    }

    private boolean isHighLocMob(org.bukkit.entity.Entity entity, String type) {
        if (type.equals("skeletons")) {
            return entity instanceof org.bukkit.entity.Skeleton || entity instanceof org.bukkit.entity.SkeletonHorse || entity instanceof org.bukkit.entity.WitherSkeleton;
        } else if (type.equals("zombies")) {
            return entity instanceof org.bukkit.entity.Zombie || entity instanceof org.bukkit.entity.ZombieHorse || entity instanceof org.bukkit.entity.Husk;
        }
        return false;
    }

    private void spawnHighLocMob(Location loc, String type, boolean forceWither) {
        // Find a random location around
        Location spawnLoc = loc.clone().add((Math.random() - 0.5) * 10, 1, (Math.random() - 0.5) * 10);
        if (!spawnLoc.getBlock().getType().isSolid()) {
            spawnLoc.setY(loc.getY() + 1);
        }
        // Spawn with rise animation (particles)
        loc.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, spawnLoc, 50, 0.5, 0.5, 0.5, 0.1);
        loc.getWorld().playSound(spawnLoc, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);

        double rand = Math.random();
        if (type.equals("skeletons")) {
            if (forceWither) {
                // Wither Skeleton
                org.bukkit.entity.WitherSkeleton wither = (org.bukkit.entity.WitherSkeleton) loc.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.WITHER_SKELETON);
                wither.setCanPickupItems(false);
                equipWitherSkeleton(wither);
            } else if (rand < 0.5) {
                // Rider
                org.bukkit.entity.SkeletonHorse horse = (org.bukkit.entity.SkeletonHorse) loc.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.SKELETON_HORSE);
                horse.setTamed(false);
                horse.getInventory().setSaddle(new org.bukkit.inventory.ItemStack(org.bukkit.Material.SADDLE));
                org.bukkit.entity.Skeleton rider = (org.bukkit.entity.Skeleton) loc.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.SKELETON);
                rider.setCanPickupItems(false);
                horse.addPassenger(rider);
                equipSkeleton(rider);
            } else if (rand < 0.7) {
                // Walker
                org.bukkit.entity.Skeleton skel = (org.bukkit.entity.Skeleton) loc.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.SKELETON);
                skel.setCanPickupItems(false);
                equipSkeleton(skel);
            } else if (rand < 0.9) {
                // Parched (gold armor)
                org.bukkit.entity.Skeleton skel = (org.bukkit.entity.Skeleton) loc.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.SKELETON);
                skel.setCanPickupItems(false);
                equipSkeletonGold(skel);
            } else {
                // Parched-like (gold armor)
                org.bukkit.entity.Zombie zomb = (org.bukkit.entity.Zombie) loc.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.ZOMBIE);
                zomb.setCanPickupItems(false);
                equipZombieGold(zomb);
            }
        } else if (type.equals("zombies")) {
            if (forceWither) {
                // Husk
                org.bukkit.entity.Husk husk = (org.bukkit.entity.Husk) loc.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.HUSK);
                husk.setCanPickupItems(false);
                equipZombie(husk);
            } else if (rand < 0.5) {
                // Rider
                org.bukkit.entity.ZombieHorse horse = (org.bukkit.entity.ZombieHorse) loc.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.ZOMBIE_HORSE);
                horse.setTamed(false);
                horse.getInventory().setSaddle(new org.bukkit.inventory.ItemStack(org.bukkit.Material.SADDLE));
                org.bukkit.entity.Zombie rider = (org.bukkit.entity.Zombie) loc.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.ZOMBIE);
                rider.setCanPickupItems(false);
                horse.addPassenger(rider);
                equipZombie(rider);
            } else if (rand < 0.75) {
                // Walker
                org.bukkit.entity.Zombie zomb = (org.bukkit.entity.Zombie) loc.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.ZOMBIE);
                zomb.setCanPickupItems(false);
                equipZombie(zomb);
            } else {
                // Parched (gold armor)
                org.bukkit.entity.Skeleton skel = (org.bukkit.entity.Skeleton) loc.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.SKELETON);
                skel.setCanPickupItems(false);
                equipSkeletonGold(skel);
            }
        }
    }

    private void equipSkeleton(org.bukkit.entity.Skeleton skel) {
        skel.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_HELMET));
        skel.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_CHESTPLATE));
        skel.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_LEGGINGS));
        skel.getEquipment().setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_BOOTS));
        double rand = Math.random();
        if (rand < 0.2) {
            skel.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.WOODEN_HOE));
        } else if (rand < 0.4) {
            skel.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.TRIDENT));
        } else if (rand < 0.6) {
            skel.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE_SWORD));
        } else {
            skel.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.BOW));
        }
        skel.getEquipment().setItemInOffHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.SHIELD));
    }

    private void equipSkeletonGold(org.bukkit.entity.Skeleton skel) {
        skel.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_HELMET));
        skel.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_CHESTPLATE));
        skel.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_LEGGINGS));
        skel.getEquipment().setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_BOOTS));
        skel.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_SWORD));
        skel.getEquipment().setItemInOffHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.SHIELD));
    }

    private void equipWitherSkeleton(org.bukkit.entity.WitherSkeleton wither) {
        wither.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_HELMET));
        wither.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_CHESTPLATE));
        wither.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_LEGGINGS));
        wither.getEquipment().setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_BOOTS));
        wither.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_SWORD));
    }

    private void equipZombie(org.bukkit.entity.Zombie zomb) {
        zomb.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_HELMET));
        zomb.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_CHESTPLATE));
        zomb.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_LEGGINGS));
        zomb.getEquipment().setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_BOOTS));
        double rand = Math.random();
        if (rand < 0.2) {
            zomb.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.WOODEN_HOE));
        } else if (rand < 0.4) {
            zomb.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.TRIDENT));
        } else if (rand < 0.6) {
            zomb.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE_SWORD));
        } else {
            zomb.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.BOW));
        }
        zomb.getEquipment().setItemInOffHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.SHIELD));
    }

    private void equipZombieGold(org.bukkit.entity.Zombie zomb) {
        zomb.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_HELMET));
        zomb.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_CHESTPLATE));
        zomb.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_LEGGINGS));
        zomb.getEquipment().setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_BOOTS));
        zomb.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLDEN_SWORD));
        zomb.getEquipment().setItemInOffHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.SHIELD));
    }
}
