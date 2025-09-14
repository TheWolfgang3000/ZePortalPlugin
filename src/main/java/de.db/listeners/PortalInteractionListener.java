package de.db.listeners;

import de.db.ZePortalPlugin;
import de.db.portal.Portal;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handles all interactions with active portals, such as button presses
 * and players moving into them. Also prevents portal water from flowing.
 */
public class PortalInteractionListener implements Listener {

    private final ZePortalPlugin plugin;

    /**
     * Constructor for the PortalInteractionListener.
     * @param plugin The main plugin instance.
     */
    public PortalInteractionListener(ZePortalPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for right-clicks on portal buttons.
     * @param event The PlayerInteractEvent.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock.getType() == Material.STONE_BUTTON) {
            Portal portal = plugin.getPortalManager().getPortalByBlock(clickedBlock);
            if (portal != null) {
                plugin.getPortalManager().startPortalAnimation(portal.getName());
            }
        }
    }

    /**
     * Listens for player movement to handle teleportation.
     * @param event The PlayerMoveEvent.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }
        Player player = event.getPlayer();
        Block blockPlayerIsIn = event.getTo().getBlock();

        Portal portal = plugin.getPortalManager().getActivePortalByBlock(blockPlayerIsIn);

        if (portal != null) {
            Location destination = plugin.getPortalManager().findTeleportDestination(portal, player);

            if (destination != null) {
                player.teleport(destination);
                player.sendMessage("§dWhoosh!");
                plugin.getPortalManager().deactivatePortal(portal.getName());
            } else {
                player.sendMessage("§cNo destination portal found in the same network!");
                plugin.getPortalManager().deactivatePortal(portal.getName());
            }
        }
    }

    /**
     * Prevents water in active portals from flowing.
     * @param event The BlockFromToEvent.
     */
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
            if (plugin.getPortalManager().getActivePortalByBlock(block) != null) {
                event.setCancelled(true);
            }
        }
    }
}