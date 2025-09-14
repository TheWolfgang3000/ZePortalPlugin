package de.db.zeportal;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

/**
 * Hauptklasse für das ZePortalPlugin.
 * Hier wird das Plugin initialisiert und deaktiviert.
 */
public class ZePortalPlugin extends JavaPlugin {

    // Der Logger wird verwendet, um Nachrichten in der Server-Konsole auszugeben.
    private final Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {
        // Diese Methode wird aufgerufen, wenn das Plugin auf dem Server startet.
        log.info("[ZePortalPlugin] Plugin wird aktiviert!");

        // Hier registrieren wir später unsere Events und Befehle.
    }

    @Override
    public void onDisable() {
        // Diese Methode wird aufgerufen, wenn der Server herunterfährt oder das Plugin entladen wird.
        log.info("[ZePortalPlugin] Plugin wird deaktiviert!");
    }
}