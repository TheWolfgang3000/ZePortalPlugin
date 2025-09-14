package de.db.portal;

import org.bukkit.Location;
import java.util.List;
import java.util.Set;

public class Portal {
    private final String name;
    private final String network;
    private final Set<Location> frameBlockLocations;
    private final List<Location> interiorLocations;

    public Portal(String name, String network, Set<Location> frameBlockLocations, List<Location> interiorLocations) {
        this.name = name;
        this.network = network;
        this.frameBlockLocations = frameBlockLocations;
        this.interiorLocations = interiorLocations;
    }

    public String getName() { return name; }
    public String getNetwork() { return network; }
    public Set<Location> getFrameBlockLocations() { return frameBlockLocations; }
    public List<Location> getInteriorLocations() { return interiorLocations; }
}