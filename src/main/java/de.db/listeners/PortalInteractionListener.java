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

public class PortalInteractionListener implements Listener {

    private final ZePortalPlugin plugin;

    public PortalInteractionListener(ZePortalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        Portal portal = plugin.getPortalManager().getPortalByBlock(clickedBlock);

        if (portal == null) return;

        // Wenn der Knopf gedrückt wird
        if (clickedBlock.getType() == Material.STONE_BUTTON) {
            // Wir prüfen, ob ein Ziel ausgewählt ist, bevor wir aktivieren
            if (!portal.getCurrentDestination().isEmpty()) {
                plugin.getPortalManager().startPortalAnimation(portal.getName());
            } else {
                event.getPlayer().sendMessage("§cBitte wähle zuerst ein Ziel auf dem Schild aus!");
            }
        }

        // Wenn das Schild geklickt wird
        if (clickedBlock.getType() == Material.WALL_SIGN || clickedBlock.getType() == Material.SIGN_POST) {
            plugin.getPortalManager().cycleDestination(portal.getName());
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
                player.sendMessage("§cKein gültiges Zielportal gefunden!");
                plugin.getPortalManager().deactivatePortal(portal.getName());
            }
        }
    }

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