
package Spuddex.XpGatekeep;

import Spuddex.XpGatekeep.commands.XpGatekeepCommand;
import Spuddex.XpGatekeep.config.ConfigManager;
import Spuddex.XpGatekeep.flags.XpLevelFlag;
import Spuddex.XpGatekeep.handlers.RegionAccessHandler;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import lombok.Getter;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the XpGatekeep plugin
 */
public class XpGatekeepPlugin extends JavaPlugin {

    @Getter
    private static XpGatekeepPlugin instance;
    
    @Getter
    private ConfigManager configManager;
    
    @Getter
    private XpLevelFlag xpLevelFlag;

    @Override
    public void onLoad() {
        instance = this;
        
        // Register our custom flag with WorldGuard
        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        try {
            this.xpLevelFlag = new XpLevelFlag("xp-level-required");
            flagRegistry.register(this.xpLevelFlag);
            this.getLogger().info("Registered custom WorldGuard flag: xp-level-required");
        } catch (Exception e) {
            this.getLogger().severe("Error registering WorldGuard flag: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        // Load configuration
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();
        
        // Register event handlers
        this.getServer().getPluginManager().registerEvents(new RegionAccessHandler(this), this);
        
        // Register commands
        this.getCommand("xpgatekeep").setExecutor(new XpGatekeepCommand(this));
        
        this.getLogger().info("XpGatekeep has been enabled!");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("XpGatekeep has been disabled!");
    }
}
