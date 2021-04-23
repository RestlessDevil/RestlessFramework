package framework;

import framework.diagnostics.Monitorable;
import framework.database.ConnectionPool;
import framework.diagnostics.Status.State;
import framework.settings.RestlessSettings;
import framework.settings.DatabaseSettings;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean(name = "dashboard", eager = true)
@ApplicationScoped
public final class Dashboard implements Serializable {

    private final List<Monitorable> monitorables;

    public Dashboard() {
        monitorables = new ArrayList<>();
    }

    private void formChain() {  // Ensures the proper order of initialization
        monitorables.add(DatabaseSettings.getInstance());
        monitorables.add(ConnectionPool.getInstance());
        monitorables.add(RestlessSettings.getInstance());
    }

    @PostConstruct
    public void initialize() {
        formChain();
        for (Monitorable monitorable : monitorables) {
            try {
                monitorable.initialize();
            } catch (Exception ex) {
                Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, ex.getMessage());
                break;
            }
            if (monitorable.getStatus().getState() == State.malfunction && monitorable.isVital()) {
                break;
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        ArrayList<Monitorable> reversed = new ArrayList<>();
        reversed.addAll(monitorables);
        Collections.reverse(reversed);
        for (Monitorable m : reversed) {
            m.shutdown();
        }
    }

    public List<Monitorable> getMonitorables() {
        return monitorables;
    }

    public boolean isOperational() {
        boolean operational = true;
        for (Monitorable monitorable : monitorables) {
            if (monitorable.getStatus().isMalfunction() && monitorable.isVital()) {
                operational = false;
            }
        }
        return operational;
    }

}