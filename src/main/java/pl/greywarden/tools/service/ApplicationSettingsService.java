package pl.greywarden.tools.service;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.prefs.Preferences;

@Service
public class ApplicationSettingsService {
    private static final String APPLICATION_PACKAGE = "pl/greywarden/tools/inventory_manager";
    private static final Preferences prefs = Preferences.userRoot().node(APPLICATION_PACKAGE);

    public void setDefaultDatabasePath(String path) {
        prefs.put("default-database-path", path);
    }

    public void setInitialDirectory(String directoryPath) {
        prefs.put("initial-directory", directoryPath);
    }

    public void setMaximized(boolean maximized) {
        prefs.putBoolean("main-window.maximized", maximized);
    }

    public void setPrefWidth(double width) {
        prefs.putDouble("main-window.pref-width", width);
    }

    public void setPrefHeight(double height) {
        prefs.putDouble("main-window.pref-height", height);
    }

    public Locale getLocale() {
        return Locale.forLanguageTag(prefs.get("application-locale", Locale.getDefault().toLanguageTag()));
    }

    public Boolean isMaximized() {
        return prefs.getBoolean("main-window.maximized", true);
    }

    public String getDefaultDatabasePath() {
        return prefs.get("default-database-path", System.getProperty("user.home"));
    }

    public String getInitialDirectory() {
        return prefs.get("initial-directory", "");
    }

    public double getPrefWidth() {
        return prefs.getDouble("main-window.pref-width", 800);
    }

    public double getPrefHeight() {
        return prefs.getDouble("main-window.pref-height", 600);
    }
}
