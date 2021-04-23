package framework.diagnostics;

public interface Monitorable {

    public void initialize();

    public void shutdown();

    public void reload();

    public Status getStatus();

    public String getLabel();

    public boolean isVital();
}
