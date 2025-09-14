package de.db.manager;

import de.db.ZePortalPlugin;
import de.db.portal.Portal; // NEU
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortalManager {

    private final ZePortalPlugin plugin;
    private Configuration portalConfig = null;
    private File portalFile = null;
    private static final Logger log = Logger.getLogger("Minecraft");

    // Das Gedächtnis wird aufgerüstet
    private Map<Location, Portal> blockToPortalMap = new HashMap<>();
    private Map<String, Portal> nameToPortalMap = new HashMap<>();

    public PortalManager(ZePortalPlugin plugin) {
        this.plugin = plugin;
        loadPortals();
    }

    // Die activatePortal-Methode bleibt größtenteils gleich
    public void activatePortal(String portalName, String networkName, String templateName, Location worldOrigin, List<String> templateBlocks) {
        // ...
        // Am Ende laden wir die Portale neu, um das neue Portal direkt ins Gedächtnis aufzunehmen
        loadPortals();
    }

    public void loadPortals() {
        // ... (der Anfang der Methode zum Laden der Datei bleibt gleich) ...

        // Lade alle Portale aus der Datei ins Gedächtnis
        blockToPortalMap.clear();
        nameToPortalMap.clear();
        if(portalConfig.getKeys("portale") == null) return;

        for(String portalName : portalConfig.getKeys("portale")) {
            String network = portalConfig.getString("portale." + portalName + ".netzwerk");
            World world = plugin.getServer().getWorld(portalConfig.getString("portale." + portalName + ".welt"));
            if(world == null) continue;

            List<?> blockData = portalConfig.getList("portale." + portalName + ".bloecke");
            Set<Location> frameLocations = new HashSet<>();
            for(Object obj : blockData) {
                if(obj instanceof String) {
                    String[] coords = ((String) obj).split(";");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);
                    frameLocations.add(new Location(world, x, y, z));
                }
            }

            // Den Innenraum aus dem Rahmen berechnen
            List<Location> interiorLocations = calculateInterior(frameLocations);

            Portal portal = new Portal(portalName, network, frameLocations, interiorLocations);
            nameToPortalMap.put(portalName, portal);
            for(Location loc : frameLocations) {
                blockToPortalMap.put(loc, portal);
            }
        }
        log.info("[ZePortalPlugin] " + nameToPortalMap.size() + " Portale geladen.");
    }

    // NEUE METHODE: Die Animation!
    public void startPortalAnimation(String portalName) {
        Portal portal = nameToPortalMap.get(portalName);
        if (portal == null || portal.getInteriorLocations().isEmpty()) return;

        // Wir sortieren die Innenraum-Blöcke von der Mitte nach außen für den Effekt
        List<Location> interior = new ArrayList<>(portal.getInteriorLocations());
        final Location center = interior.get(interior.size() / 2);
        Collections.sort(interior, new Comparator<Location>() {
            @Override
            public int compare(Location loc1, Location loc2) {
                return Double.compare(loc1.distanceSquared(center), loc2.distanceSquared(center));
            }
        });

        long delay = 1L; // Start-Verzögerung in Ticks
        for (final Location loc : interior) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    loc.getBlock().setType(Material.STATIONARY_WATER);
                }
            }, delay);
            delay += 1L; // Jeder weitere Block erscheint einen Tick später
        }
    }

    // NEUE HILFSMETHODE: Berechnet den Innenraum basierend auf den Rahmenblöcken
    private List<Location> calculateInterior(Set<Location> frameLocations) {
        if(frameLocations.isEmpty()) return new ArrayList<>();

        World world = frameLocations.iterator().next().getWorld();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (Location loc : frameLocations) {
            if(loc.getBlockX() < minX) minX = loc.getBlockX();
            if(loc.getBlockX() > maxX) maxX = loc.getBlockX();
            if(loc.getBlockY() < minY) minY = loc.getBlockY();
            if(loc.getBlockY() > maxY) maxY = loc.getBlockY();
            if(loc.getBlockZ() < minZ) minZ = loc.getBlockZ();
            if(loc.getBlockZ() > maxZ) maxZ = loc.getBlockZ();
        }

        List<Location> interior = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location currentLoc = new Location(world, x, y, z);
                    if (!frameLocations.contains(currentLoc)) {
                        interior.add(currentLoc);
                    }
                }
            }
        }
        return interior;
    }

    // Die get-Methode gibt jetzt das ganze Portal-Objekt zurück
    public Portal getPortalByBlock(Block block) {
        return blockToPortalMap.get(block.getLocation());
    }
}