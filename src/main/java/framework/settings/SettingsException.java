package framework.settings;

public class SettingsException extends Exception {  // Any non-IOException that can be thrown by load() method of Settings class

    SettingsException(String message) {
        super(message);
    }
}
