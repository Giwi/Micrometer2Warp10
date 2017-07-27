package org.giwi.warp10.micrometer.plugin;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.spectator.SpectatorMeterRegistry;

/**
 * Created by b3605 on 27/07/17.
 *
 * @author Xavier MARIN (b3605)
 */
public class Warp10MeterRegistry extends SpectatorMeterRegistry {
    public Warp10MeterRegistry(Clock clock, Warp10config config) {
        super(new Warp10Registry(new com.netflix.spectator.api.Clock() {
            @Override
            public long wallTime() {
                return clock.wallTime();
            }

            @Override
            public long monotonicTime() {
                return clock.monotonicTime();
            }
        }, config), clock);

        ((Warp10Registry) this.getSpectatorRegistry()).start();
    }

    public Warp10MeterRegistry(Warp10config config) {
        this(Clock.SYSTEM, config);
        this.commonTags("warp10", "true");
    }
}
