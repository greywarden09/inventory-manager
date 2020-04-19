package pl.greywarden.tools.service;

import org.springframework.stereotype.Service;

import java.util.prefs.Preferences;

@Service
public class ApplicationSettingsService {
    private static final String APPLICATION_PACKAGE = "pl.greywarden.tools.inventory-manager";
    private static final Preferences prefs = Preferences.userRoot().node(APPLICATION_PACKAGE);

    public String getString(String key, String defaultValue) {
        return prefs.get(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        prefs.put(key, value);
    }

}
