package lunalauf.rms.utilities.network.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class PortProvider {

    private static final Collection<Integer> preferredPorts;

    static {
        preferredPorts = new LinkedList<>();
        for (int i = 50916; i < 50931; i++)
            preferredPorts.add(i);
    }

    public static Collection<Integer> getPreferredPorts() {
        return preferredPorts;
    }
}
