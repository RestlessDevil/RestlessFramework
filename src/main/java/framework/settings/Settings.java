package framework.settings;

import framework.diagnostics.Monitorable;
import framework.diagnostics.Status;
import framework.diagnostics.Status.State;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class Settings implements Monitorable {

    private static final Logger LOG = Logger.getLogger(Settings.class.getName());

    final String prefix;
    final String path;
    final String label;
    final boolean vital;

    private Status status;

    public Settings(String prefix, String path, String label, boolean vital) {
        this.prefix = prefix;
        this.path = path;
        this.label = label;
        this.vital = vital;

        status = new Status(State.uninitialized, null);
    }

    protected abstract void load() throws IOException; // Loading and validation of parameters

    @Override
    public synchronized void initialize() {
        try {
            load();
            status = new Status(State.operational, null);
        } catch (Exception ex) {
            status = new Status(State.malfunction, ex);
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void shutdown() {
        status = new Status(State.uninitialized, null);
    }

    @Override
    public synchronized void reload() {
        initialize();
    }

    @Override
    public final Status getStatus() {
        return status;
    }

    @Override
    public final String getLabel() {
        return label;
    }

    @Override
    public final boolean isVital() {
        return vital;
    }
}
