package de.db.manager;

import de.db.ZePortalPlugin;
import de.db.portal.Portal;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
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

    private Map<Location, Portal> blockToPortalMap = new HashMap<>();
    private Map<String, Portal> nameToPortalMap = new HashMap<>();
    private Set<String> activePortals = new HashSet<>();

    public PortalManager(ZePortalPlugin plugin) {
        this.plugin = plugin;
        loadPortals();
    }

    public void activatePortal(String portalName, String networkName, String templateName, Location worldOrigin, List<String> templateBlocks) {
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
        loadPortals();
    }

    public void loadPortals() {
        plugin.getDataFolder().mkdirs();
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
        blockToPortalMap.clear();
        nameToPortalMap.clear();
        if (portalConfig.getKeys("portale") == null) return;
        for (String portalName : portalConfig.getKeys("portale")) {
            String network = portalConfig.getString("portale." + portalName + ".netzwerk");
            World world = plugin.getServer().getWorld(portalConfig.getString("portale." + portalName + ".welt"));
            if (world == null) continue;
            List<?> blockData = portalConfig.getList("portale." + portalName + ".bloecke");
            Set<Location> allBlockLocations = new HashSet<>();
            for (Object obj : blockData) {
                if (obj instanceof String) {
                    String[] coords = ((String) obj).split(";");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);
                    allBlockLocations.add(new Location(world, x, y, z));
                }
            }
            List<Location> interiorLocations = calculateInterior(allBlockLocations);
            Portal portal = new Portal(portalName, network, allBlockLocations, interiorLocations);
            nameToPortalMap.put(portalName, portal);
            for (Location loc : allBlockLocations) {
                blockToPortalMap.put(loc, portal);
            }
        }
        log.info("[ZePortalPlugin] " + nameToPortalMap.size() + " Portale geladen.");
    }

    public void startPortalAnimation(String portalName) {
        final Portal portal = nameToPortalMap.get(portalName);
        if (portal == null || portal.getInteriorLocations().isEmpty() || activePortals.contains(portalName)) {
            return;
        }
        activePortals.add(portalName);
        for (Location loc : portal.getInteriorLocations()) {
            loc.getBlock().setType(Material.STATIONARY_WATER);
        }
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                deactivatePortal(portalName);
            }
        }, 400L);
    }

    public void deactivatePortal(String portalName) {
        Portal portal = nameToPortalMap.get(portalName);
        if (portal == null || !activePortals.contains(portalName)) return;
        for (Location loc : portal.getInteriorLocations()) {
            Block block = loc.getBlock();
            if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
                block.setType(Material.AIR);
            }
        }
        activePortals.remove(portalName);
        log.info("[ZePortalPlugin] Portal '" + portalName + "' geschlossen.");
    }

    // NEU: Wechselt das Ziel eines Portals und aktualisiert das Schild
    public void cycleDestination(String portalName) {
        Portal portal = nameToPortalMap.get(portalName);
        if (portal == null) return;

        List<Portal> destinations = new ArrayList<>();
        for (Portal p : nameToPortalMap.values()) {
            if (p.getNetwork().equals(portal.getNetwork()) && !p.getName().equals(portal.getName())) {
                destinations.add(p);
            }
        }

        if (destinations.isEmpty()) {
            portal.setCurrentDestination("");
            updateSign(portal);
            return;
        }

        int currentIndex = -1;
        for (int i = 0; i < destinations.size(); i++) {
            if (destinations.get(i).getName().equals(portal.getCurrentDestination())) {
                currentIndex = i;
                break;
            }
        }

        int nextIndex = (currentIndex + 1) % destinations.size();
        portal.setCurrentDestination(destinations.get(nextIndex).getName());

        updateSign(portal);
    }

    // NEU: Aktualisiert den Text auf dem Schild eines Portals in der Welt
    public void updateSign(Portal portal) {
        Sign signBlock = null;
        for (Location loc : portal.getAllBlockLocations()) {
            if (loc.getBlock().getType() == Material.WALL_SIGN || loc.getBlock().getType() == Material.SIGN_POST) {
                if (loc.getBlock().getState() instanceof Sign) {
                    signBlock = (Sign) loc.getBlock().getState();
                    break;
                }
            }
        }

        if (signBlock == null) return;

        signBlock.setLine(0, "§9--" + portal.getName() + "--");
        if (portal.getCurrentDestination().isEmpty()) {
            signBlock.setLine(1, "§cKein Ziel");
            signBlock.setLine(2, "gefunden");
        } else {
            signBlock.setLine(1, "§7Ziel:");
            signBlock.setLine(2, "§f" + portal.getCurrentDestination());
        }
        signBlock.setLine(3, "§8(" + portal.getNetwork() + ")");
        signBlock.update();
    }

    public Portal getActivePortalByBlock(Block block) {
        if (block.getType() != Material.STATIONARY_WATER && block.getType() != Material.WATER) {
            return null;
        }
        for (String portalName : activePortals) {
            Portal portal = nameToPortalMap.get(portalName);
            if (portal != null && portal.getInteriorLocations().contains(block.getLocation())) {
                return portal;
            }
        }
        return null;
    }

    public Location findTeleportDestination(Portal startPortal, Player player) {
        // HINWEIS: Diese Methode müssen wir anpassen, damit sie das ausgewählte Ziel nutzt
        Portal destinationPortal = nameToPortalMap.get(startPortal.getCurrentDestination());
        if (destinationPortal != null) {
            Location center = destinationPortal.getInteriorLocations().get(destinationPortal.getInteriorLocations().size() / 2);
            Location safeSpot = center.clone();
            safeSpot.setYaw(player.getLocation().getYaw());
            safeSpot.setPitch(player.getLocation().getPitch());
            return safeSpot.add(0.5, 0, 1.5);
        }
        return null;
    }

    private List<Location> calculateInterior(Set<Location> allPortalBlocks) {
        if (allPortalBlocks.isEmpty()) return new ArrayList<>();
        World world = allPortalBlocks.iterator().next().getWorld();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (Location loc : allPortalBlocks) {
            Material type = loc.getBlock().getType();
            if (type == Material.SIGN_POST || type == Material.WALL_SIGN || type == Material.STONE_BUTTON) continue;
            if (loc.getBlockX() < minX) minX = loc.getBlockX();
            if (loc.getBlockX() > maxX) maxX = loc.getBlockX();
            if (loc.getBlockY() < minY) minY = loc.getBlockY();
            if (loc.getBlockY() > maxY) maxY = loc.getBlockY();
            if (loc.getBlockZ() < minZ) minZ = loc.getBlockZ();
            if (loc.getBlockZ() > maxZ) maxZ = loc.getBlockZ();
        }
        List<Location> interior = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location currentLoc = new Location(world, x, y, z);
                    if (!allPortalBlocks.contains(currentLoc)) {
                        interior.add(currentLoc);
                    }
                }
            }
        }
        return interior;
    }

    public Portal getPortalByBlock(Block block) {
        return blockToPortalMap.get(block.getLocation());
    }
}