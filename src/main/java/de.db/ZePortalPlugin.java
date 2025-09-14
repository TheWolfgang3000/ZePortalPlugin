package de.db;

import de.db.commands.PortalCommand;
import de.db.listeners.PortalCreationListener;
import de.db.listeners.PortalInteractionListener;
import de.db.manager.PortalManager;
import de.db.manager.TemplateManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

/**
 * Main class for the ZePortalPlugin.
 * Handles the enabling and disabling of the plugin and holds instances of the managers.
 */
public class ZePortalPlugin extends JavaPlugin {

    private final Logger log = Logger.getLogger("Minecraft");
    private PortalManager portalManager;
    private TemplateManager templateManager;

    /**
     * Called when the plugin is enabled by the server.
     */
    @Override
    public void onEnable() {
        log.info("[ZePortalPlugin] Plugin is being enabled!");

        // Initialize managers
        this.portalManager = new PortalManager(this);
        this.templateManager = new TemplateManager(this);

        // Register command executor
        getCommand("portal").setExecutor(new PortalCommand(this));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PortalCreationListener(), this);
        getServer().getPluginManager().registerEvents(new PortalInteractionListener(this), this);
    }

    /**
     * Called when the plugin is disabled by the server.
     */
    @Override
    public void onDisable() {
        log.info("[ZePortalPlugin] Plugin is being disabled!");
    }

    /**
     * Gets the active PortalManager instance.
     * @return The PortalManager instance.
     */
    public PortalManager getPortalManager() {
        return portalManager;
    }

    /**
     * Gets the active TemplateManager instance.
     * @return The TemplateManager instance.
     */
    public TemplateManager getTemplateManager() {
        return templateManager;
    }
}