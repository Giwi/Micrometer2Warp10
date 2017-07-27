package org.giwi.warp10.micrometer.plugin;

import io.micrometer.core.instrument.spectator.step.StepRegistryConfig;

/**
 * Created by b3605 on 27/07/17.
 *
 * @author Xavier MARIN (b3605)
 */
public interface Warp10config extends StepRegistryConfig {
    default String prefix() {
        return "warp10";
    }

    default String token() {
        String v = get(prefix() + ".token");
        if(v == null)
            throw new IllegalStateException(prefix() + ".token must be set to report metrics to warp10");
        return v;
    }
    default String url() {
        String v = get(prefix() + ".url");
        if(v == null)
            throw new IllegalStateException(prefix() + ".url must be set to report metrics to warp10");
        return v;
    }
}
