package de.db.manager;

import de.db.ZePortalPlugin;
import de.db.portal.Portal;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages all active portals on the server.
 * This class handles loading portals from portals.yml, activating new portals,
 * running animations, deactivation, and performing various lookups.
 */
public class PortalManager {

    private final ZePortalPlugin plugin;
    private Configuration portalConfig = null;
    private File portalFile = null;
    private static final Logger log = Logger.getLogger("Minecraft");

    private Map<Location, Portal> blockToPortalMap = new HashMap<>();
    private Map<String, Portal> nameToPortalMap = new HashMap<>();
    private Set<String> activePortals = new HashSet<>();

    /**
     * Constructor for the PortalManager.
     * @param plugin The main plugin instance.
     */
    public PortalManager(ZePortalPlugin plugin) {
        this.plugin = plugin;
        loadPortals();
    }

    /**
     * Saves a newly created portal's data to the portals.yml file.
     * @param portalName The unique name for the new portal.
     * @param networkName The network this portal belongs to.
     * @param templateName The name of the template used to create the portal.
     * @param worldOrigin The anchor location of the portal structure in the world.
     * @param templateBlocks The list of relative block data from the template.
     */
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

    /**
     * Loads all portal data from portals.yml into memory for fast access.
     */
    public void loadPortals() {
        plugin.getDataFolder().mkdirs();
        if (portalFile == null) {
            portalFile = new File(plugin.getDataFolder(), "portals.yml");
        }
        if (!portalFile.exists()) {
            try {
                portalFile.createNewFile();
            } catch (IOException e) {
                log.log(Level.SEVERE, "[ZePortalPlugin] Could not create portals.yml!", e);
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
        log.info("[ZePortalPlugin] " + nameToPortalMap.size() + " portals loaded.");
    }

    /**
     * Starts the portal activation sequence.
     * @param portalName The name of the portal to activate.
     */
    public void startPortalAnimation(String portalName) {
        final Portal portal = nameToPortalMap.get(portalName);
        if (portal == null || portal.getInteriorLocations().isEmpty() || activePortals.contains(portalName)) {
            return;
        }
        activePortals.add(portalName);
        for (Location loc : portal.getInteriorLocations()) {
            loc.getBlock().setType(Material.STATIONARY_WATER);
        }
        scheduleDeactivation(portalName);
    }

    /**
     * Deactivates a portal, removing its water.
     * @param portalName The name of the portal to deactivate.
     */
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
        log.info("[ZePortalPlugin] Portal '" + portalName + "' closed.");
    }

    /**
     * Checks if a given block is part of an active portal's interior.
     * @param block The block to check.
     * @return The Portal object if the block is part of an active portal, otherwise null.
     */
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

    /**
     * Finds a safe teleport destination location for a given portal.
     * @param startPortal The portal the player is teleporting from.
     * @param player The player who is teleporting.
     * @return A safe teleport Location, or null if no destination is found.
     */
    public Location findTeleportDestination(Portal startPortal, Player player) {
        for (Portal destinationPortal : nameToPortalMap.values()) {
            if (destinationPortal.getNetwork().equals(startPortal.getNetwork()) && !destinationPortal.getName().equals(startPortal.getName())) {
                Location center = destinationPortal.getInteriorLocations().get(destinationPortal.getInteriorLocations().size() / 2);
                Location safeSpot = center.clone();
                safeSpot.setYaw(player.getLocation().getYaw());
                safeSpot.setPitch(player.getLocation().getPitch());
                return safeSpot.add(0.5, 0, 1.5);
            }
        }
        return null;
    }

    /**
     * Retrieves a Portal object by one of its component blocks (frame, sign, button).
     * @param block The block to check.
     * @return The Portal object, or null if the block is not part of any portal.
     */
    public Portal getPortalByBlock(Block block) {
        return blockToPortalMap.get(block.getLocation());
    }

    /**
     * Schedules a delayed task to deactivate a portal.
     * @param portalName The name of the portal to schedule for deactivation.
     */
    private void scheduleDeactivation(final String portalName) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                deactivatePortal(portalName);
            }
        }, 400L); // 20 seconds
    }

    /**
     * Calculates the interior locations of a portal based on its frame.
     * @param allPortalBlocks A set of all blocks belonging to the portal structure.
     * @return A list of locations representing the portal's interior.
     */
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
}