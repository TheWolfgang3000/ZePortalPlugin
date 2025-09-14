package de.db.portal;

import org.bukkit.Location;
import java.util.List;
import java.util.Set;

/**
 * A data class representing a single, active portal instance in the world.
 */
public class Portal {
    private final String name;
    private final String network;
    private final Set<Location> allBlockLocations;
    private final List<Location> interiorLocations;
    private String currentDestination = "";

    /**
     * Constructor for the Portal object.
     * @param name The unique name of this portal.
     * @param network The network this portal belongs to.
     * @param allBlockLocations A set of all block locations that make up this portal.
     * @param interiorLocations A list of block locations that form the portal's interior.
     */
    public Portal(String name, String network, Set<Location> allBlockLocations, List<Location> interiorLocations) {
        this.name = name;
        this.network = network;
        this.allBlockLocations = allBlockLocations;
        this.interiorLocations = interiorLocations;
    }

    /**
     * @return The name of the portal.
     */
    public String getName() { return name; }

    /**
     * @return The network of the portal.
     */
    public String getNetwork() { return network; }

    /**
     * @return A set of all block locations belonging to this portal.
     */
    public Set<Location> getAllBlockLocations() { return allBlockLocations; }

    /**
     * @return A list of the interior block locations of this portal.
     */
    public List<Location> getInteriorLocations() { return interiorLocations; }

    /**
     * @return The name of the currently selected destination portal.
     */
    public String getCurrentDestination() { return currentDestination; }

    /**
     * Sets the current destination for this portal.
     * @param dest The name of the destination portal.
     */
    public void setCurrentDestination(String dest) { this.currentDestination = dest; }
}