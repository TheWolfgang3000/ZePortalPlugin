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

public class TemplateManager {

    private final ZePortalPlugin plugin;
    private Configuration templateConfig = null;
    private File templateFile = null;
    private static final Logger log = Logger.getLogger("Minecraft");

    public TemplateManager(ZePortalPlugin plugin) {
        this.plugin = plugin;
        loadTemplates();
    }

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
            return "§cFehler: Die Auswahl enthält keine soliden Blöcke.";
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
                        if (signLocation != null) return "§cFehler: Es wurde mehr als ein Schild in der Nähe gefunden!";
                        signLocation = currentBlock.getLocation();
                    }
                    if (type == Material.STONE_BUTTON) {
                        if (buttonLocation != null) return "§cFehler: Es wurde mehr als ein Knopf in der Nähe gefunden!";
                        buttonLocation = currentBlock.getLocation();
                    }
                }
            }
        }

        if (signLocation == null) return "§cFehler: Es wurde kein Schild in der Nähe des Rahmens gefunden!";
        if (buttonLocation == null) return "§cFehler: Es wurde kein Knopf in der Nähe des Rahmens gefunden!";

        finalBlocks.add(getBlockData(signLocation.getBlock(), origin));
        finalBlocks.add(getBlockData(buttonLocation.getBlock(), origin));

        String path = "templates." + name;
        templateConfig.setProperty(path + ".blocks", finalBlocks);
        templateConfig.save();

        return null;
    }

    private String getBlockData(Block block, Location origin) {
        int relX = block.getX() - origin.getBlockX();
        int relY = block.getY() - origin.getBlockY();
        int relZ = block.getZ() - origin.getBlockZ();
        return relX + ";" + relY + ";" + relZ + ";" + block.getType().name() + ";" + block.getData();
    }

    public void loadTemplates() {
        // NEU: Stelle sicher, dass der Plugin-Ordner existiert
        plugin.getDataFolder().mkdirs();

        if (templateFile == null) {
            templateFile = new File(plugin.getDataFolder(), "templates.yml");
        }
        if (!templateFile.exists()) {
            try {
                templateFile.createNewFile();
            } catch (IOException e) {
                log.log(Level.SEVERE, "[ZePortalPlugin] Konnte templates.yml nicht erstellen!", e);
            }
        }
        templateConfig = new Configuration(templateFile);
        templateConfig.load();
    }

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