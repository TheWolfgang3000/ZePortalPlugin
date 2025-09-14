package de.db.manager;

import de.db.ZePortalPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.config.Configuration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the creation and storage of portal templates.
 * This class handles the templates.yml file.
 */
public class TemplateManager {

    private final ZePortalPlugin plugin;
    private Configuration templateConfig = null;
    private File templateFile = null;
    private static final Logger log = Logger.getLogger("Minecraft");

    /**
     * Constructor for the TemplateManager.
     * @param plugin The main plugin instance.
     */
    public TemplateManager(ZePortalPlugin plugin) {
        this.plugin = plugin;
        loadTemplates();
    }

    /**
     * Creates a new portal template from an admin's selection.
     * @param name The name for the new template.
     * @param pos1 The first corner of the admin's selection.
     * @param pos2 The second corner of the admin's selection.
     * @return An error message string if creation fails, otherwise null on success.
     */
    public String createTemplate(String name, Location pos1, Location pos2) {
        World world = pos1.getWorld();
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        Location origin = new Location(world, minX, minY, minZ);
        List<String> finalBlocks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR) {
                        finalBlocks.add(getBlockData(block, origin));
                    }
                }
            }
        }
        if (finalBlocks.isEmpty()) {
            return "§cError: The selection contains no solid blocks.";
        }

        int searchMinX = minX - 1, searchMaxX = maxX + 1;
        int searchMinY = minY - 1, searchMaxY = maxY + 1;
        int searchMinZ = minZ - 1, searchMaxZ = maxZ + 1;
        Location signLocation = null;
        Location buttonLocation = null;
        for (int x = searchMinX; x <= searchMaxX; x++) {
            for (int y = searchMinY; y <= searchMaxY; y++) {
                for (int z = searchMinZ; z <= maxZ; z++) {
                    if (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) {
                        continue;
                    }
                    Block currentBlock = world.getBlockAt(x, y, z);
                    Material type = currentBlock.getType();
                    if (type == Material.SIGN_POST || type == Material.WALL_SIGN) {
                        if (signLocation != null) return "§cError: More than one sign found near the frame!";
                        signLocation = currentBlock.getLocation();
                    }
                    if (type == Material.STONE_BUTTON) {
                        if (buttonLocation != null) return "§cError: More than one button found near the frame!";
                        buttonLocation = currentBlock.getLocation();
                    }
                }
            }
        }
        if (signLocation == null) return "§cError: No sign found near the frame!";
        if (buttonLocation == null) return "§cError: No button found near the frame!";

        finalBlocks.add(getBlockData(signLocation.getBlock(), origin));
        finalBlocks.add(getBlockData(buttonLocation.getBlock(), origin));
        String path = "templates." + name;
        templateConfig.setProperty(path + ".blocks", finalBlocks);
        templateConfig.save();
        return null;
    }

    /**
     * Converts a block's data into a relative string format for storage.
     * @param block The block to process.
     * @param origin The anchor point (0,0,0) of the template.
     * @return A string representation of the block's relative position and type.
     */
    private String getBlockData(Block block, Location origin) {
        int relX = block.getX() - origin.getBlockX();
        int relY = block.getY() - origin.getBlockY();
        int relZ = block.getZ() - origin.getBlockZ();
        return relX + ";" + relY + ";" + relZ + ";" + block.getType().name() + ";" + block.getData();
    }

    /**
     * Loads the templates.yml file from disk. Creates it if it doesn't exist.
     */
    public void loadTemplates() {
        plugin.getDataFolder().mkdirs();
        if (templateFile == null) {
            templateFile = new File(plugin.getDataFolder(), "templates.yml");
        }
        if (!templateFile.exists()) {
            try {
                templateFile.createNewFile();
            } catch (IOException e) {
                log.log(Level.SEVERE, "[ZePortalPlugin] Could not create templates.yml!", e);
            }
        }
        templateConfig = new Configuration(templateFile);
        templateConfig.load();
    }

    /**
     * Retrieves the list of block data strings for a specific template.
     * @param name The name of the template to retrieve.
     * @return A List of block data strings, or null if the template is not found.
     */
    public List<String> getTemplateBlocks(String name) {
        String path = "templates." + name + ".blocks";
        if (templateConfig.getList(path) != null) {
            List<?> rawList = templateConfig.getList(path);
            List<String> blockStrings = new ArrayList<>();
            for(Object obj : rawList) {
                if(obj instanceof String) {
                    blockStrings.add((String) obj);
                }
            }
            return blockStrings;
        }
        return null;
    }
}