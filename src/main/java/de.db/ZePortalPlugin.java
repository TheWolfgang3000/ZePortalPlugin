package de.db;

import de.db.commands.PortalCommand;
import de.db.listeners.PortalCreationListener;
import de.db.listeners.PortalInteractionListener; // NEU
import de.db.manager.PortalManager;
import de.db.manager.TemplateManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public class ZePortalPlugin extends JavaPlugin {

    private final Logger log = Logger.getLogger("Minecraft");
    private PortalManager portalManager;
    private TemplateManager templateManager;

    @Override
    public void onEnable() {
        log.info("[ZePortalPlugin] Plugin wird aktiviert!");

        // Manager initialisieren
        this.portalManager = new PortalManager(this);
        this.templateManager = new TemplateManager(this);

        // Befehl registrieren
        getCommand("portal").setExecutor(new PortalCommand(this));

        // KORREKTUR: Wir registrieren ZWEI getrennte Listener
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

    public TemplateManager getTemplateManager() {
        return templateManager;
    }
}