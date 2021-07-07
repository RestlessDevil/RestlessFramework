package framework.cache;

import java.util.LinkedList;
import java.util.HashMap;

public final class Cache {

    private final HashMap<Object, Cacheable> map;
    private final LinkedList<Cacheable> accessOrder;
    private int capacity;

    public Cache(int capacity) {
        map = new HashMap<>();
        accessOrder = new LinkedList<>();
        this.capacity = capacity;
    }

    private void purgeExcess() {
        while (accessOrder.size() > capacity) {
            Cacheable toBeRemoved = accessOrder.remove(0);
            map.remove(toBeRemoved.getId());
        }
    }

    public synchronized void put(Cacheable object) {
        map.put(object.getId(), object);
        accessOrder.add(object);
        purgeExcess();
    }

    public synchronized Cacheable get(Object id) {
        if (map.containsKey(id)) {
            Cacheable object = map.get(id);
            // Updating Access Order
            accessOrder.remove(object);
            accessOrder.add(object);
            return object;
        } else {
            return null;
        }
    }

    public synchronized void remove(Object id) {
        if (map.containsKey(id)) {
            Cacheable object = map.remove(id);
            accessOrder.remove(object);
        }
    }

    public synchronized void clear() {
        map.clear();
        accessOrder.clear();
    }

    public synchronized void resize(int capacity) {
        this.capacity = capacity;
        purgeExcess();
    }
}
