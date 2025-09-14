package de.db.listeners;

import de.db.ZePortalPlugin;
import de.db.portal.Portal; // Wichtig: Die neue Portal-Klasse importieren
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PortalInteractionListener implements Listener {

    private final ZePortalPlugin plugin;

    public PortalInteractionListener(ZePortalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Wir interessieren uns nur für Rechtsklicks auf einen Block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();

        // Prüfen, ob der geklickte Block ein Steinknopf ist
        if (clickedBlock.getType() == Material.STONE_BUTTON) {
            // Wir holen uns jetzt das ganze Portal-Objekt
            Portal portal = plugin.getPortalManager().getPortalByBlock(clickedBlock);

            if (portal != null) {
                // HIER IST DIE ÄNDERUNG:
                // Die alte Chat-Nachricht wird durch den Aufruf der Animation ersetzt.
                plugin.getPortalManager().startPortalAnimation(portal.getName());
            }
        }
    }
}