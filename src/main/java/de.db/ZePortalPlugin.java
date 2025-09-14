package de.db;

import de.db.commands.PortalCommand;
import de.db.listeners.PortalCreationListener;
import de.db.listeners.PortalInteractionListener;
import de.db.manager.PortalManager;
import de.db.manager.TemplateManager; // NEU
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public class ZePortalPlugin extends JavaPlugin {

    private final Logger log = Logger.getLogger("Minecraft");
    private PortalManager portalManager;
    private TemplateManager templateManager; // NEU

    @Override
    public void onEnable() {
        log.info("[ZePortalPlugin] Plugin wird aktiviert!");

        this.portalManager = new PortalManager(this);
        this.templateManager = new TemplateManager(this); // NEU

        getCommand("portal").setExecutor(new PortalCommand(this));
        getServer().getPluginManager().registerEvents(new PortalCreationListener(), this);

        getServer().getPluginManager().registerEvents(new PortalInteractionListener(this), this);
    }

    @Override
    public void onDisable() {
        log.info("[ZePortalPlugin] Plugin wird deaktiviert!");
    }

    public PortalManager getPortalManager() {
        return portalManager;
    }

    public TemplateManager getTemplateManager() { // NEU
        return templateManager;
    }
}