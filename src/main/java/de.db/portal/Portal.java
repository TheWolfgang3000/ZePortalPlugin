package de.db.portal;

import org.bukkit.Location;
import java.util.List;
import java.util.Set;

public class Portal {
    private final String name;
    private final String network;
    private final Set<Location> allBlockLocations;
    private final List<Location> interiorLocations;
    private String currentDestination = ""; // NEU

    public Portal(String name, String network, Set<Location> allBlockLocations, List<Location> interiorLocations) {
        this.name = name;
        this.network = network;
        this.allBlockLocations = allBlockLocations;
        this.interiorLocations = interiorLocations;
    }

    public String getName() { return name; }
    public String getNetwork() { return network; }
    public Set<Location> getAllBlockLocations() { return allBlockLocations; } // Umbenannt für Klarheit
    public List<Location> getInteriorLocations() { return interiorLocations; }

    // NEUE METHODEN
    public String getCurrentDestination() { return currentDestination; }
    public void setCurrentDestination(String dest) { this.currentDestination = dest; }
}