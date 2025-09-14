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
import org.bukkit.event.block.BlockPhysicsEvent; // NEU
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PortalInteractionListener implements Listener {

    private final ZePortalPlugin plugin;

    public PortalInteractionListener(ZePortalPlugin plugin) {
        this.plugin = plugin;
    }

    // Die onPlayerInteract- und onPlayerMove-Methoden bleiben unverändert
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) { /* ... */ }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) { /* ... */ }


    // --- HIER IST DIE NEUE, MAGISCHE METHODE ---
    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        // Wir prüfen, ob der Block Wasser ist
        if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
            // Wir fragen den Manager, ob dieser Wasserblock Teil eines AKTIVEN Portals ist
            if (plugin.getPortalManager().getActivePortalByBlock(block) != null) {
                // Wenn ja, brechen wir das Physik-Update ab. Das Wasser wird nicht fließen.
                event.setCancelled(true);
            }
        }
    }
}