
package Spuddex.XpGatekeep.handlers;

import Spuddex.XpGatekeep.XpGatekeepPlugin;
import Spuddex.XpGatekeep.config.ConfigManager;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles checking if players can enter regions based on XP level requirements
 */
public class RegionAccessHandler implements Listener {

    private final XpGatekeepPlugin plugin;
    private final Map<UUID, Location> lastValidLocations = new HashMap<>();
    private final Map<UUID, Long> lastMessageTime = new HashMap<>();
    private static final long MESSAGE_COOLDOWN = 2000; // milliseconds

    public RegionAccessHandler(final XpGatekeepPlugin plugin) {
        this.plugin = plugin;
        
        // Start a task to continuously check players in regions
        new BukkitRunnable() {
            @Override
            public void run() {
                checkPlayersInRegions();
            }
        }.runTaskTimer(plugin, 20L, plugin.getConfigManager().getCheckIntervalTicks());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        // Only check if the player changed blocks to minimize impact
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        final Player player = event.getPlayer();
        
        // Check if player has bypass permission
        if (player.hasPermission("xpgatekeep.bypass")) {
            return;
        }
        
        // Check if player meets XP requirements for the region they're entering
        if (!checkPlayerXpForRegion(player, event.getTo())) {
            // If not, cancel the move and send them back to their last valid location
            event.setCancelled(true);
            
            // Teleport them back to their last valid location if available
            final Location lastValid = this.lastValidLocations.get(player.getUniqueId());
            if (lastValid != null) {
                player.teleport(lastValid);
            }
        } else {
            // Update their last valid location
            this.lastValidLocations.put(player.getUniqueId(), event.getFrom());
        }
    }
    
    /**
     * Checks all online players to ensure they meet XP requirements for the regions they're in
     */
    private void checkPlayersInRegions() {
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            // Skip players with bypass permission
            if (player.hasPermission("xpgatekeep.bypass")) {
                continue;
            }
            
            // Check if player meets XP requirements for their current location
            if (!checkPlayerXpForRegion(player, player.getLocation())) {
                // If they don't have enough XP, teleport them to their last valid location
                final Location lastValid = this.lastValidLocations.get(player.getUniqueId());
                if (lastValid != null) {
                    player.teleport(lastValid);
                } else {
                    // If no last valid location, teleport them to world spawn
                    player.teleport(player.getWorld().getSpawnLocation());
                }
            } else {
                // Update their last valid location
                this.lastValidLocations.put(player.getUniqueId(), player.getLocation());
            }
        }
    }
    
    /**
     * Checks if a player meets the XP requirements for a location
     * 
     * @param player The player to check
     * @param location The location to check
     * @return true if the player can enter, false if not
     */
    public boolean checkPlayerXpForRegion(final Player player, final Location location) {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionQuery query = container.createQuery();
        final ApplicableRegionSet regions = query.getApplicableRegions(
            BukkitAdapter.adapt(location)
        );
        
        int highestRequirement = 0;
        
        // Check all regions at this location for XP requirements
        for (final ProtectedRegion region : regions) {
            final Integer requiredLevel = region.getFlag(this.plugin.getXpLevelFlag());
            
            if (requiredLevel != null && requiredLevel > highestRequirement) {
                highestRequirement = requiredLevel;
            }
        }
        
        // If there's an XP requirement, check if the player meets it
        if (highestRequirement > 0) {
            final int playerLevel = player.getLevel();
            
            if (playerLevel < highestRequirement) {
                // Player doesn't have enough XP, notify them (with cooldown)
                final long currentTime = System.currentTimeMillis();
                final Long lastTime = this.lastMessageTime.get(player.getUniqueId());
                
                if (lastTime == null || currentTime - lastTime > MESSAGE_COOLDOWN) {
                    final String message = ChatColor.translateAlternateColorCodes('&', 
                        this.plugin.getConfigManager().getPrefix() + 
                        this.plugin.getConfigManager().getRegionBlockedMessage()
                            .replace("%level%", String.valueOf(highestRequirement))
                    );
                    
                    player.sendMessage(message);
                    this.lastMessageTime.put(player.getUniqueId(), currentTime);
                }
                
                return false;
            }
        }
        
        return true;
    }
}
