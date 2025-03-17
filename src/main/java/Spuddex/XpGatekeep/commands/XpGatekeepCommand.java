
package Spuddex.XpGatekeep.commands;

import Spuddex.XpGatekeep.XpGatekeepPlugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for XpGatekeep plugin
 */
public class XpGatekeepCommand implements CommandExecutor, TabCompleter {

    private final XpGatekeepPlugin plugin;
    private final List<String> subcommands = Arrays.asList("set", "remove", "info", "reload");

    public XpGatekeepCommand(final XpGatekeepPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        // Check permission
        if (!sender.hasPermission("xpgatekeep.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                this.plugin.getConfigManager().getPrefix() + 
                this.plugin.getConfigManager().getNoPermissionMessage()));
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        // Handle subcommands
        switch (args[0].toLowerCase()) {
            case "set":
                return handleSetCommand(sender, args);
            case "remove":
                return handleRemoveCommand(sender, args);
            case "info":
                return handleInfoCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender, args);
            default:
                sendHelpMessage(sender);
                return true;
        }
    }
    
    /**
     * Handles the set command to set an XP level requirement for a region
     */
    private boolean handleSetCommand(final CommandSender sender, final String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /xpgatekeep set <region> <level>");
            return true;
        }
        
        final String regionName = args[1];
        int level;
        
        try {
            level = Integer.parseInt(args[2]);
            if (level < 0) {
                sender.sendMessage(ChatColor.RED + "XP level must be a positive number");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "XP level must be a number");
            return true;
        }
        
        // Get the region
        final ProtectedRegion region = getRegion(sender, regionName);
        if (region == null) {
            return true;
        }
        
        // Set the flag
        region.setFlag(this.plugin.getXpLevelFlag(), level);
        
        // Send success message
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            this.plugin.getConfigManager().getPrefix() +
            this.plugin.getConfigManager().getSetSuccessMessage()
                .replace("%region%", regionName)
                .replace("%level%", String.valueOf(level))));
        
        return true;
    }
    
    /**
     * Handles the remove command to remove an XP level requirement from a region
     */
    private boolean handleRemoveCommand(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /xpgatekeep remove <region>");
            return true;
        }
        
        final String regionName = args[1];
        
        // Get the region
        final ProtectedRegion region = getRegion(sender, regionName);
        if (region == null) {
            return true;
        }
        
        // Remove the flag
        region.setFlag(this.plugin.getXpLevelFlag(), null);
        
        // Send success message
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            this.plugin.getConfigManager().getPrefix() +
            this.plugin.getConfigManager().getRemoveSuccessMessage()
                .replace("%region%", regionName)));
        
        return true;
    }
    
    /**
     * Handles the info command to get information about a region's XP level requirement
     */
    private boolean handleInfoCommand(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /xpgatekeep info <region>");
            return true;
        }
        
        final String regionName = args[1];
        
        // Get the region
        final ProtectedRegion region = getRegion(sender, regionName);
        if (region == null) {
            return true;
        }
        
        // Get the flag value
        final Integer level = region.getFlag(this.plugin.getXpLevelFlag());
        
        // Send info message
        if (level != null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                this.plugin.getConfigManager().getPrefix() +
                this.plugin.getConfigManager().getInfoRegionMessage()
                    .replace("%region%", regionName)
                    .replace("%level%", String.valueOf(level))));
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                this.plugin.getConfigManager().getPrefix() +
                this.plugin.getConfigManager().getInfoNoRequirementMessage()
                    .replace("%region%", regionName)));
        }
        
        return true;
    }
    
    /**
     * Handles the reload command to reload the plugin configuration
     */
    private boolean handleReloadCommand(final CommandSender sender, final String[] args) {
        this.plugin.reloadConfig();
        this.plugin.getConfigManager().loadConfig();
        
        sender.sendMessage(ChatColor.GREEN + "XpGatekeep configuration reloaded");
        
        return true;
    }
    
    /**
     * Sends the help message to a command sender
     */
    private void sendHelpMessage(final CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== XpGatekeep Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/xpgatekeep set <region> <level>" + ChatColor.WHITE + " - Set XP level requirement for a region");
        sender.sendMessage(ChatColor.YELLOW + "/xpgatekeep remove <region>" + ChatColor.WHITE + " - Remove XP level requirement from a region");
        sender.sendMessage(ChatColor.YELLOW + "/xpgatekeep info <region>" + ChatColor.WHITE + " - View XP level requirement for a region");
        sender.sendMessage(ChatColor.YELLOW + "/xpgatekeep reload" + ChatColor.WHITE + " - Reload the plugin configuration");
    }
    
    /**
     * Gets a WorldGuard region by name
     * 
     * @param sender The command sender
     * @param regionName The name of the region
     * @return The region, or null if not found
     */
    private ProtectedRegion getRegion(final CommandSender sender, final String regionName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players");
            return null;
        }
        
        final Player player = (Player) sender;
        
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
        
        if (regions == null) {
            sender.sendMessage(ChatColor.RED + "WorldGuard is not enabled in this world");
            return null;
        }
        
        final ProtectedRegion region = regions.getRegion(regionName);
        
        if (region == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                this.plugin.getConfigManager().getPrefix() +
                this.plugin.getConfigManager().getRegionNotFoundMessage()
                    .replace("%region%", regionName)));
            return null;
        }
        
        return region;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        final List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Suggest subcommands
            return this.subcommands.stream()
                .filter(subcommand -> subcommand.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("set") || 
                args[0].equalsIgnoreCase("remove") || 
                args[0].equalsIgnoreCase("info"))) {
            // Suggest region names
            if (sender instanceof Player) {
                final Player player = (Player) sender;
                final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                final RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
                
                if (regions != null) {
                    return regions.getRegions().keySet().stream()
                        .filter(region -> region.startsWith(args[1]))
                        .collect(Collectors.toList());
                }
            }
        }
        
        return completions;
    }
}
