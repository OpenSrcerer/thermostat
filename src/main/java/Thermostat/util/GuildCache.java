package thermostat.util;

import thermostat.util.entities.Synapse;

import java.util.Map;
import java.util.WeakHashMap;

public class GuildCache {
    public class CacheValue {
        private final Synapse synapse;
        private String prefix;

        public CacheValue(Synapse synapse, String prefix) {
            this.synapse = synapse;
            this.prefix = prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        public Synapse getSynapse() {
            return synapse;
        }
    }

    private static final Map<String, CacheValue> cache = new WeakHashMap<>();

    private static void initializeCache() {

    }
}
