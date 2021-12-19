package framework.settings;

import framework.diagnostics.MonitoredComponent;
import framework.diagnostics.Status;
import framework.diagnostics.Status.State;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class Settings extends MonitoredComponent {

    private static final Logger LOG = Logger.getLogger(Settings.class.getName());

    final String prefix;
    final String path;

    public Settings(String prefix, String path, String label, boolean vital) {
        super(label, vital);
        this.prefix = prefix;
        this.path = path;
    }

    protected abstract void load() throws IOException, SettingsException; // Loading and validation of parameters

    protected abstract void shutdownProcedure() throws SettingsException;    // Cleaning up

    @Override
    public synchronized void initialize() {
        try {
            load();
            status = new Status(State.OPERATIONAL);
        } catch (IOException | SettingsException ex) {
            status = new Status(State.MALFUNCTION, ex);
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void shutdown() {
        try {
            shutdownProcedure();
        } catch (SettingsException ex) {
            status = new Status(State.MALFUNCTION, ex);
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}

