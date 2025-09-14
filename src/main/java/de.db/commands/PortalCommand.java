package de.db.commands;

import de.db.ZePortalPlugin;
import de.db.listeners.PortalCreationListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PortalCommand implements CommandExecutor {

    private final ZePortalPlugin plugin;

    public PortalCommand(ZePortalPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§a[ZePortalPlugin] §7Verfügbare Befehle: /portal new, /portal create");
            return true;
        }

        if (args[0].equalsIgnoreCase("new")) {
            // Admin-Logik - bleibt unverändert
            if (!player.isOp()) {
                player.sendMessage("§cDu hast keine Rechte, diesen Befehl zu verwenden.");
                return true;
            }
            if (args.length != 2) {
                player.sendMessage("§cBenutzung: /portal new <TemplateName>");
                return true;
            }
            if (!PortalCreationListener.position1.containsKey(player) || !PortalCreationListener.position2.containsKey(player)) {
                player.sendMessage("§cDu musst zuerst mit der Goldhacke eine Auswahl treffen!");
                return true;
            }
            Location pos1 = PortalCreationListener.position1.get(player);
            Location pos2 = PortalCreationListener.position2.get(player);
            int dimX = Math.abs(pos1.getBlockX() - pos2.getBlockX()) + 1;
            int dimY = Math.abs(pos1.getBlockY() - pos2.getBlockY()) + 1;
            int dimZ = Math.abs(pos1.getBlockZ() - pos2.getBlockZ()) + 1;
            if (dimX > 1 && dimY > 1 && dimZ > 1) {
                player.sendMessage("§cFehler: Die Auswahl für einen Portalrahmen muss flach sein.");
                return true;
            }
            String templateName = args[1];
            String errorMessage = plugin.getTemplateManager().createTemplate(templateName, pos1, pos2);
            if (errorMessage != null) {
                player.sendMessage(errorMessage);
            } else {
                player.sendMessage("§aTemplate '" + templateName + "' erfolgreich gespeichert!");
            }
            PortalCreationListener.position1.remove(player);
            PortalCreationListener.position2.remove(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length != 3) {
                player.sendMessage("§cBenutzung: /portal create <TemplateName> <PortalName>");
                return true;
            }
            // NEU: Auch der Spieler muss eine Auswahl treffen
            if (!PortalCreationListener.position1.containsKey(player) || !PortalCreationListener.position2.containsKey(player)) {
                player.sendMessage("§cDu musst zuerst mit der Goldhacke den von dir gebauten Rahmen markieren!");
                return true;
            }

            String templateName = args[1];
            String portalName = args[2];

            List<String> templateBlocks = plugin.getTemplateManager().getTemplateBlocks(templateName);
            if (templateBlocks == null) {
                player.sendMessage("§cFehler: Ein Template mit dem Namen '" + templateName + "' existiert nicht.");
                return true;
            }

            // NEU: Wir nutzen die Spielerauswahl als Ankerpunkt
            Location playerPos1 = PortalCreationListener.position1.get(player);
            Location playerPos2 = PortalCreationListener.position2.get(player);
            int minX = Math.min(playerPos1.getBlockX(), playerPos2.getBlockX());
            int minY = Math.min(playerPos1.getBlockY(), playerPos2.getBlockY());
            int minZ = Math.min(playerPos1.getBlockZ(), playerPos2.getBlockZ());
            Location worldOrigin = new Location(player.getWorld(), minX, minY, minZ);

            // Wir erstellen eine Liste der Blöcke, die der Spieler tatsächlich gebaut hat
            List<String> builtBlocks = new ArrayList<>();
            for (int x = 0; x <= Math.abs(playerPos1.getBlockX() - playerPos2.getBlockX()); x++) {
                for (int y = 0; y <= Math.abs(playerPos1.getBlockY() - playerPos2.getBlockY()); y++) {
                    for (int z = 0; z <= Math.abs(playerPos1.getBlockZ() - playerPos2.getBlockZ()); z++) {
                        Block block = worldOrigin.clone().add(x, y, z).getBlock();
                        if(block.getType() != Material.AIR) {
                            builtBlocks.add(x+";"+y+";"+z+";"+block.getType().name());
                        }
                    }
                }
            }

            // Wir extrahieren die reinen Rahmenblöcke aus der Vorlage
            List<String> templateFrameBlocks = new ArrayList<>();
            for (String blockData : templateBlocks) {
                Material material = Material.matchMaterial(blockData.split(";")[3]);
                if (material != Material.WALL_SIGN && material != Material.SIGN_POST && material != Material.STONE_BUTTON) {
                    // Wir speichern ohne Block-Daten, um den Vergleich zu vereinfachen
                    String[] parts = blockData.split(";");
                    templateFrameBlocks.add(parts[0]+";"+parts[1]+";"+parts[2]+";"+parts[3]);
                }
            }

            // Vergleiche die beiden Listen
            if (builtBlocks.size() != templateFrameBlocks.size() || !builtBlocks.containsAll(templateFrameBlocks)) {
                player.sendMessage("§cFehler: Der von dir markierte Rahmen passt nicht zum Template '" + templateName + "'.");
                PortalCreationListener.position1.remove(player);
                PortalCreationListener.position2.remove(player);
                return true;
            }

            // Struktur passt! Platziere Schild und Knopf
            for (String blockData : templateBlocks) {
                String[] parts = blockData.split(";");
                Material material = Material.matchMaterial(parts[3]);
                if (material == Material.WALL_SIGN || material == Material.SIGN_POST || material == Material.STONE_BUTTON) {
                    int relX = Integer.parseInt(parts[0]);
                    int relY = Integer.parseInt(parts[1]);
                    int relZ = Integer.parseInt(parts[2]);
                    byte data = Byte.parseByte(parts[4]);
                    Block blockToPlace = worldOrigin.clone().add(relX, relY, relZ).getBlock();
                    blockToPlace.setType(material);
                    blockToPlace.setData(data);
                }
            }

            plugin.getPortalManager().activatePortal(portalName, portalName, templateName, worldOrigin, templateBlocks);

            player.sendMessage("§aPortal '" + portalName + "' erfolgreich erstellt und aktiviert!");
            PortalCreationListener.position1.remove(player);
            PortalCreationListener.position2.remove(player);
            return true;
        }

        player.sendMessage("§cUnbekannter Befehl.");
        return true;
    }
}