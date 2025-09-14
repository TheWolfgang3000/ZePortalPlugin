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
import org.bukkit.event.block.BlockFromToEvent; // NEU
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PortalInteractionListener implements Listener {

    private final ZePortalPlugin plugin;

    public PortalInteractionListener(ZePortalPlugin plugin) {
        this.plugin = plugin;
    }

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
                player.sendMessage("§cKein Zielportal im selben Netzwerk gefunden!");
                plugin.getPortalManager().deactivatePortal(portal.getName());
            }
        }
    }

    // FINALE METHODE ZUM VERHINDERN DES WASSERFLUSSES
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Block block = event.getBlock();
        // Wir prüfen, ob der Block, der fließen will, Wasser ist
        if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
            // Wir fragen den Manager, ob dieser Wasserblock Teil eines AKTIVEN Portals ist
            if (plugin.getPortalManager().getActivePortalByBlock(block) != null) {
                // Wenn ja, brechen wir das Fließen ab.
                event.setCancelled(true);
            }
        }
    }
}