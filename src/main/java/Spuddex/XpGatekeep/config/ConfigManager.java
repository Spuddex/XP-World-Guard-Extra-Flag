
package Spuddex.XpGatekeep.config;

import Spuddex.XpGatekeep.XpGatekeepPlugin;

import lombok.Getter;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manages configuration for the plugin
 */
public class ConfigManager {

    private final XpGatekeepPlugin plugin;
    
    @Getter
    private String prefix;
    
    @Getter
    private String noPermissionMessage;
    
    @Getter
    private String regionBlockedMessage;
    
    @Getter
    private String setSuccessMessage;
    
    @Getter
    private String removeSuccessMessage;
    
    @Getter
    private String infoRegionMessage;
    
    @Getter
    private String infoNoRequirementMessage;
    
    @Getter
    private String regionNotFoundMessage;
    
    @Getter
    private int checkIntervalTicks;

    public ConfigManager(final XpGatekeepPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads configuration from config.yml
     */
    public void loadConfig() {
        this.plugin.saveDefaultConfig();
        
        final FileConfiguration config = this.plugin.getConfig();
        
        // Load messages
        this.prefix = config.getString("messages.prefix", "&8[&aXpGatekeep&8] &r");
        this.noPermissionMessage = config.getString("messages.no-permission", "&cYou don't have permission to use this command.");
        this.regionBlockedMessage = config.getString("messages.region-blocked", "&cYou need at least &e%level% &cXP levels to enter this region.");
        
        // Load command messages
        this.setSuccessMessage = config.getString("messages.command.set-success", "&aSet XP requirement for region &e%region% &ato &e%level%&a.");
        this.removeSuccessMessage = config.getString("messages.command.remove-success", "&aRemoved XP requirement from region &e%region%&a.");
        this.infoRegionMessage = config.getString("messages.command.info-region", "&aRegion &e%region% &arequires &e%level% &aXP levels.");
        this.infoNoRequirementMessage = config.getString("messages.command.info-no-requirement", "&aRegion &e%region% &ahas no XP requirement.");
        this.regionNotFoundMessage = config.getString("messages.command.region-not-found", "&cRegion &e%region% &cnot found.");
        
        // Load settings
        this.checkIntervalTicks = config.getInt("settings.check-interval-ticks", 5);
    }
}
