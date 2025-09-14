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

/**
 * Handles all commands starting with "/portal".
 */
public class PortalCommand implements CommandExecutor {

    private final ZePortalPlugin plugin;

    /**
     * Constructor for the PortalCommand class.
     * @param plugin The main plugin instance.
     */
    public PortalCommand(ZePortalPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the given command, returning its success.
     * @param sender Source of the command.
     * @param cmd Command which was executed.
     * @param label Alias of the command which was used.
     * @param args Passed command arguments.
     * @return true if a valid command, otherwise false.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§a[ZePortalPlugin] §7Please specify a subcommand. Use /portal help for more info.");
            return true;
        }

        // --- ADMIN COMMAND: /portal new <TemplateName> ---
        if (args[0].equalsIgnoreCase("new")) {
            if (!player.isOp()) {
                player.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            if (args.length != 2) {
                player.sendMessage("§cUsage: /portal new <TemplateName>");
                return true;
            }
            if (!PortalCreationListener.position1.containsKey(player) || !PortalCreationListener.position2.containsKey(player)) {
                player.sendMessage("§cYou must first select two points with the golden hoe!");
                return true;
            }
            Location pos1 = PortalCreationListener.position1.get(player);
            Location pos2 = PortalCreationListener.position2.get(player);
            int dimX = Math.abs(pos1.getBlockX() - pos2.getBlockX()) + 1;
            int dimY = Math.abs(pos1.getBlockY() - pos2.getBlockY()) + 1;
            int dimZ = Math.abs(pos1.getBlockZ() - pos2.getBlockZ()) + 1;
            if (dimX > 1 && dimY > 1 && dimZ > 1) {
                player.sendMessage("§cError: The selection for a portal frame must be flat.");
                return true;
            }
            String templateName = args[1];
            String errorMessage = plugin.getTemplateManager().createTemplate(templateName, pos1, pos2);
            if (errorMessage != null) {
                player.sendMessage(errorMessage);
            } else {
                player.sendMessage("§aTemplate '" + templateName + "' successfully saved!");
            }
            PortalCreationListener.position1.remove(player);
            PortalCreationListener.position2.remove(player);
            return true;
        }

        // --- PLAYER COMMAND: /portal create <TemplateName> <PortalName> <NetworkName> ---
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length != 4) {
                player.sendMessage("§cUsage: /portal create <TemplateName> <PortalName> <NetworkName>");
                return true;
            }
            if (!PortalCreationListener.position1.containsKey(player) || !PortalCreationListener.position2.containsKey(player)) {
                player.sendMessage("§cYou must first select the frame you built with the golden hoe!");
                return true;
            }

            String templateName = args[1];
            String portalName = args[2];
            String networkName = args[3];

            List<String> templateBlocks = plugin.getTemplateManager().getTemplateBlocks(templateName);
            if (templateBlocks == null) {
                player.sendMessage("§cError: A template with the name '" + templateName + "' does not exist.");
                return true;
            }

            Location playerPos1 = PortalCreationListener.position1.get(player);
            Location playerPos2 = PortalCreationListener.position2.get(player);
            int minX = Math.min(playerPos1.getBlockX(), playerPos2.getBlockX());
            int minY = Math.min(playerPos1.getBlockY(), playerPos2.getBlockY());
            int minZ = Math.min(playerPos1.getBlockZ(), playerPos2.getBlockZ());
            Location worldOrigin = new Location(player.getWorld(), minX, minY, minZ);

            List<String> builtBlocks = new ArrayList<>();
            for (int x = 0; x <= Math.abs(playerPos1.getBlockX() - playerPos2.getBlockX()); x++) {
                for (int y = 0; y <= Math.abs(playerPos1.getBlockY() - playerPos2.getBlockY()); y++) {
                    for (int z = 0; z <= Math.abs(playerPos1.getBlockZ() - playerPos2.getBlockZ()); z++) {
                        Block block = worldOrigin.clone().add(x, y, z).getBlock();
                        if (block.getType() != Material.AIR) {
                            builtBlocks.add(x + ";" + y + ";" + z + ";" + block.getType().name());
                        }
                    }
                }
            }

            List<String> templateFrameBlocks = new ArrayList<>();
            for (String blockData : templateBlocks) {
                Material material = Material.matchMaterial(blockData.split(";")[3]);
                if (material != Material.WALL_SIGN && material != Material.SIGN_POST && material != Material.STONE_BUTTON) {
                    String[] parts = blockData.split(";");
                    templateFrameBlocks.add(parts[0] + ";" + parts[1] + ";" + parts[2] + ";" + parts[3]);
                }
            }

            if (builtBlocks.size() != templateFrameBlocks.size() || !builtBlocks.containsAll(templateFrameBlocks)) {
                player.sendMessage("§cError: The frame you selected does not match the '" + templateName + "' template.");
                PortalCreationListener.position1.remove(player);
                PortalCreationListener.position2.remove(player);
                return true;
            }

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
            plugin.getPortalManager().activatePortal(portalName, networkName, templateName, worldOrigin, templateBlocks);
            player.sendMessage("§aPortal '" + portalName + "' in network '" + networkName + "' created successfully!");
            PortalCreationListener.position1.remove(player);
            PortalCreationListener.position2.remove(player);
            return true;
        }

        player.sendMessage("§cUnknown command.");
        return true;
    }
}