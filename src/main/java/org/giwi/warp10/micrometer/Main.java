package org.giwi.warp10.micrometer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.giwi.warp10.micrometer.plugin.Warp10MeterRegistry;
import org.giwi.warp10.micrometer.plugin.Warp10config;

import java.time.Duration;
import java.util.ResourceBundle;

/**
 * Created by b3605 on 27/07/17.
 *
 * @author Xavier MARIN (b3605)
 */
public class Main {
    public static void main(String... args) {

        ResourceBundle labels = ResourceBundle.getBundle("warp10");
        Warp10config config = new Warp10config() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(1);
            }

            @Override
            public String get(String k) {
                return labels.containsKey(k)?labels.getString(k):null; // accept the rest of the defaults
            }
        };
        MeterRegistry registry = new Warp10MeterRegistry(config);
        registry.commonTags("stack", "prod", "region", "us-east-1");

        Counter counter = registry.counter("apiCall", "method", "POST", "uri", "register");
        try {
            while (true) {
                counter.increment();
                Thread.sleep((long) (Math.random() * 100L));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
