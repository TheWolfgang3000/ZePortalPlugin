// KORREKTUR: Paket-Name an deine Ordnerstruktur angepasst
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

public class PortalCreationListener implements Listener {
    public static final Map<Player, Location> position1 = new HashMap<>();
    public static final Map<Player, Location> position2 = new HashMap<>();

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
            player.sendMessage("§a[ZePortalPlugin] §ePosition 1 gesetzt!");
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            position2.put(player, clickedLocation);
            player.sendMessage("§a[ZePortalPlugin] §ePosition 2 gesetzt!");
        }
        if (position1.containsKey(player) && position2.containsKey(player)) {
            player.sendMessage("§a[ZePortalPlugin] §7Innenraum des Portals markiert. Nutze nun §c/portal new <Name>");
        }
    }
}