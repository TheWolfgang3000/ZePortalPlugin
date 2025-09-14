package de.db.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the block selection process using the golden hoe.
 */
public class PortalCreationListener implements Listener {

    /**
     * Stores the first selection point for each player.
     */
    public static final Map<Player, Location> position1 = new HashMap<>();
    /**
     * Stores the second selection point for each player.
     */
    public static final Map<Player, Location> position2 = new HashMap<>();

    /**
     * Listens for player interaction events to capture wand selections.
     * @param event The PlayerInteractEvent.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getItemInHand().getType() != Material.GOLD_HOE) {
            return;
        }
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        event.setCancelled(true);
        Location clickedLocation = event.getClickedBlock().getLocation();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            position1.put(player, clickedLocation);
            player.sendMessage("§a[ZePortalPlugin] §ePosition 1 set!");
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            position2.put(player, clickedLocation);
            player.sendMessage("§a[ZePortalPlugin] §ePosition 2 set!");
        }
    }
}