package com.extraction;

import com.extraction.auction.AuctionManager;
import com.extraction.commands.AcceptTradeCommand;
import com.extraction.commands.AuctionCommand;
import com.extraction.commands.BalanceCommand;
import com.extraction.commands.ClaimChestCommand;
import com.extraction.commands.ClaimDoorCommand;
import com.extraction.commands.DeclineTradeCommand;
import com.extraction.commands.ExtractGiveCommand;
import com.extraction.commands.ExtractOutBannerCommand;
import com.extraction.commands.ExtractPointCommand;
import com.extraction.commands.GiveMoneyCommand;
import com.extraction.commands.GiveRankCommand;
import com.extraction.commands.LootChestSetCommand;
import com.extraction.commands.ProfileCommand;
import com.extraction.commands.ReportCommand;
import com.extraction.commands.ResetLootCommand;
import com.extraction.commands.SellCommand;
import com.extraction.commands.ServerMapCommand;
import com.extraction.commands.SetExtractToPointCommand;
import com.extraction.commands.SetMoneyCommand;
import com.extraction.commands.SetReportWebhookCommand;
import com.extraction.commands.SetServerMapCommand;
import com.extraction.commands.SetWorldCommand;
import com.extraction.commands.StashCommand;
import com.extraction.commands.TeamCommand;
import com.extraction.commands.TradeCommand;
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
import com.extraction.managers.TradeManager;
import com.extraction.managers.ChestManager;
import com.extraction.managers.DoorManager;
import com.extraction.managers.TeamManager;
import com.extraction.managers.FirstTimeJoinManager;
import com.extraction.managers.ChatModerationManager;
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
import com.extraction.listeners.JoinLeaveListener;
import com.extraction.listeners.ProximityChatListener;
import com.extraction.listeners.TradeListener;
import com.extraction.listeners.ChatModerationListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

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
    private TradeManager tradeManager;
    private ChestManager chestManager;
    private DoorManager doorManager;
    private TeamManager teamManager;
    private FirstTimeJoinManager firstTimeJoinManager;
    private ChatModerationManager chatModerationManager;
    private ReportManager reportManager;
    private ServerMapManager serverMapManager;
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
           this.tradeManager = new TradeManager(this, economyManager);
           this.chestManager = new ChestManager(this);
           this.doorManager = new DoorManager(this);
           this.teamManager = new TeamManager();
           this.firstTimeJoinManager = new FirstTimeJoinManager(this);
           this.chatModerationManager = new ChatModerationManager(this);
           this.reportManager = new ReportManager(this);
           this.serverMapManager = new ServerMapManager(this);

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

        getCommand("trade").setExecutor(
            new TradeCommand(this, tradeManager)
        );
        getCommand("accepttrade").setExecutor(
            new AcceptTradeCommand(this, tradeManager)
        );
        getCommand("declinetrade").setExecutor(
            new DeclineTradeCommand(this, tradeManager)
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

    public TradeManager getTradeManager() {
        return tradeManager;
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
}
