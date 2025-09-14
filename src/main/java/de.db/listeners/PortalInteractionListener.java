package de.db.listeners;

import de.db.ZePortalPlugin;
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
            // Jetzt fragen wir den PortalManager: Gehört dieser Knopf zu einem Portal?
            String portalName = plugin.getPortalManager().getPortalNameByBlock(clickedBlock);

            if (portalName != null) {
                // Ja, der Knopf gehört zu einem Portal!
                player.sendMessage("§b[ZePortalPlugin] §7Du hast den Knopf von Portal '" + portalName + "' gedrückt!");

                // TODO: Hier starten wir die Wasser-Animation!
            }
        }
    }
}