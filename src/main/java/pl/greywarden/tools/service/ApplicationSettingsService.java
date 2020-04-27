package pl.greywarden.tools.service;

import org.springframework.stereotype.Service;

import java.util.prefs.Preferences;

@Service
public class ApplicationSettingsService {
    private static final String APPLICATION_PACKAGE = "pl/greywarden/tools/inventory_manager";
    private static final Preferences prefs = Preferences.userRoot().node(APPLICATION_PACKAGE);

    public void setProperty(String key, String value) {
        prefs.put(key, value);
    }

    public void setProperty(String key, Boolean value) {
        prefs.putBoolean(key, value);
    }

    public void setProperty(String key, double value) {
        prefs.putDouble(key, value);
    }

    public String getString(String key, String defaultValue) {
        return prefs.get(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public double getDouble(String key, double defaultValue) {
        return prefs.getDouble(key, defaultValue);
    }
}
