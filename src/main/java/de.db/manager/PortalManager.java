package de.db.manager;

import de.db.ZePortalPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortalManager {

    private final ZePortalPlugin plugin;
    private Configuration portalConfig = null;
    private File portalFile = null;
    private static final Logger log = Logger.getLogger("Minecraft");

    // NEU: Das "Gedächtnis" des Managers. Speichert den Ort eines Blocks und den Namen des zugehörigen Portals.
    private Map<Location, String> blockToPortalMap = new HashMap<>();

    public PortalManager(ZePortalPlugin plugin) {
        this.plugin = plugin;
        loadPortals();
    }

    public void activatePortal(String portalName, String networkName, String templateName, Location worldOrigin, List<String> templateBlocks) {
        // ... diese Methode bleibt unverändert ...
        String path = "portale." + portalName;

        portalConfig.setProperty(path + ".netzwerk", networkName);
        portalConfig.setProperty(path + ".template", templateName);
        portalConfig.setProperty(path + ".welt", worldOrigin.getWorld().getName());

        List<String> absoluteBlockCoords = new ArrayList<>();
        for (String blockData : templateBlocks) {
            String[] parts = blockData.split(";");
            int relX = Integer.parseInt(parts[0]);
            int relY = Integer.parseInt(parts[1]);
            int relZ = Integer.parseInt(parts[2]);

            int absX = worldOrigin.getBlockX() + relX;
            int absY = worldOrigin.getBlockY() + relY;
            int absZ = worldOrigin.getBlockZ() + relZ;
            absoluteBlockCoords.add(absX + ";" + absY + ";" + absZ);
        }
        portalConfig.setProperty(path + ".bloecke", absoluteBlockCoords);

        portalConfig.save();
        // Lade die Portale neu, um das neue Portal direkt ins Gedächtnis aufzunehmen
        loadPortals();
    }

    public void loadPortals() {
        if (portalFile == null) {
            portalFile = new File(plugin.getDataFolder(), "portals.yml");
        }
        if (!portalFile.exists()) {
            try {
                portalFile.createNewFile();
            } catch (IOException e) {
                log.log(Level.SEVERE, "[ZePortalPlugin] Konnte portals.yml nicht erstellen!", e);
            }
        }
        portalConfig = new Configuration(portalFile);
        portalConfig.load();

        // NEU: Lade alle Portale aus der Datei ins Gedächtnis (in die Map)
        blockToPortalMap.clear(); // Zuerst das alte Gedächtnis leeren
        if(portalConfig.getKeys("portale") == null) return;

        for(String portalName : portalConfig.getKeys("portale")) {
            List<?> blockData = portalConfig.getList("portale." + portalName + ".bloecke");
            World world = plugin.getServer().getWorld(portalConfig.getString("portale." + portalName + ".welt"));
            if(world == null) continue;

            for(Object obj : blockData) {
                if(obj instanceof String) {
                    String[] coords = ((String) obj).split(";");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);
                    blockToPortalMap.put(new Location(world, x, y, z), portalName);
                }
            }
        }
        log.info("[ZePortalPlugin] " + portalConfig.getKeys("portale").size() + " Portale geladen.");
    }

    // NEU: Die Methode, die unser Listener aufruft
    public String getPortalNameByBlock(Block block) {
        return blockToPortalMap.get(block.getLocation());
    }
}