package com.extraction;

import com.extraction.auction.AuctionManager;
import com.extraction.commands.AcceptTradeCommand;
import com.extraction.commands.AuctionCommand;
import com.extraction.commands.DeclineTradeCommand;
import com.extraction.commands.GiveMoneyCommand;
import com.extraction.commands.ResetStockCommand;
import com.extraction.commands.SetMoneyCommand;
import com.extraction.commands.TradeCommand;
import com.extraction.crypto.CryptoManager;
import com.extraction.resources.ResourcePackManager;
import com.extraction.commands.BalanceCommand;
import com.extraction.commands.ExtractGiveCommand;
import com.extraction.commands.ExtractOutBannerCommand;
import com.extraction.commands.ExtractPointCommand;
import com.extraction.commands.GiveMoneyCommand;
import com.extraction.commands.LootChestSetCommand;
import com.extraction.commands.ProfileCommand;
import com.extraction.commands.ResetLootCommand;
import com.extraction.commands.SellCommand;
import com.extraction.commands.SetExtractToPointCommand;
import com.extraction.commands.SetWorldCommand;
import com.extraction.commands.ShopCommand;
import com.extraction.commands.StashCommand;
import com.extraction.commands.WipeCommand;
import com.extraction.data.PlayerDataManager;
import com.extraction.economy.EconomyManager;
import com.extraction.extract.ExtractManager;
import com.extraction.leveling.LevelingManager;
import com.extraction.listeners.BannerListener;
import com.extraction.listeners.ContainerListener;
import com.extraction.listeners.CryptoWalletListener;
import com.extraction.listeners.CustomItemListener;
import com.extraction.listeners.DeathListener;
import com.extraction.listeners.FishingListener;
import com.extraction.listeners.JoinLeaveListener;
import com.extraction.listeners.ProximityChatListener;
import com.extraction.listeners.TradeListener;
import com.extraction.managers.TradeManager;
import com.extraction.loot.LootContainerManager;
import com.extraction.loot.LootTableManager;
import com.extraction.shop.ShopManager;
import com.extraction.stash.StashManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ExtractionPlugin extends JavaPlugin {

    private LootContainerManager lootContainerManager;
    private LootTableManager lootTableManager;
    private StashManager stashManager;
    private ShopManager shopManager;
    private ExtractManager extractManager;
    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private PlayerDataManager playerDataManager;
    private LevelingManager levelingManager;
    private CryptoManager cryptoManager;
    private ResourcePackManager resourcePackManager;
    private TradeManager tradeManager;
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
        this.shopManager = new ShopManager(this, economyManager);
        this.extractManager = new ExtractManager(this, levelingManager);
        this.auctionManager = new AuctionManager(this, economyManager);
        this.cryptoManager = new CryptoManager(this, economyManager);
        this.resourcePackManager = new ResourcePackManager(this);
        this.tradeManager = new TradeManager(this, economyManager);

        registerCommands();
        registerListeners();
        getLogger().info("Extraction plugin enabled!");
    }

    private void registerCommands() {
        getCommand("lootchestset").setExecutor(
            new LootChestSetCommand(this, lootContainerManager)
        );
        getCommand("resetloot").setExecutor(
            new ResetLootCommand(this, lootContainerManager)
        );
        getCommand("shop").setExecutor(
            new ShopCommand(this, shopManager, economyManager)
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
        getCommand("setextracttopoint").setExecutor(
            new SetExtractToPointCommand(this, extractManager)
        );

        ExtractGiveCommand extractGiveCommand = new ExtractGiveCommand(
            this,
            shopManager
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
        getCommand("resetstock").setExecutor(
            new ResetStockCommand(this, shopManager)
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
            .registerEvents(new JoinLeaveListener(extractManager), this);
        // temperature subsystem removed
    }

    public LootContainerManager getLootContainerManager() {
        return lootContainerManager;
    }

    public StashManager getStashManager() {
        return stashManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
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
}
