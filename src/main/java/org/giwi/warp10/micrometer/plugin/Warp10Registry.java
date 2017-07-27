package org.giwi.warp10.micrometer.plugin;

import com.netflix.spectator.api.Clock;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Tag;
import io.micrometer.core.instrument.spectator.step.AbstractStepRegistry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.joining;

/**
 * Created by b3605 on 27/07/17.
 *
 * @author Xavier MARIN (b3605)
 */
public class Warp10Registry extends AbstractStepRegistry {

    private final URL metricsEndpoint;
    private final String token;

    public Warp10Registry(Clock clock, Warp10config config) {
        super(clock, config);
        try {
            this.token = config.token();
            this.metricsEndpoint = URI.create(config.url() + "/api/v0/update").toURL();
        } catch (MalformedURLException e) {
            // not possible
            throw new RuntimeException(e);
        }
    }

    protected void pushMetrics() {
        for (List<Measurement> batch : getBatches()) {
            batch.stream().filter(m -> StreamSupport.stream(m.id().tags().spliterator(), false)
                    .map(Tag::key)
                    .collect(Collectors.toList()).contains("warp10")
            ).collect(Collectors.toList()).forEach(m -> {
                Iterable<Tag> tags = m.id().tags();
                StringBuilder body = new StringBuilder()
                        .append(System.currentTimeMillis() * 1000)
                        .append('/')
                        .append('/')
                        .append(' ')
                        .append(m.id().name())
                        .append('{')
                        .append(StreamSupport.stream(tags.spliterator(), false)
                                .filter(t->!t.key().equals("warp10"))
                                .map(t -> t.key() + "=" + t.value())
                                .collect(joining(",")))
                        .append('}')
                        .append(' ')
                        .append(m.value());
               // send(body);
                System.out.println("Metric -> " + body);
            });
        }
    }

    private void send(StringBuilder body) {

        try {
            HttpURLConnection con = (HttpURLConnection) metricsEndpoint.openConnection();
            con.setConnectTimeout(connectTimeout);
            con.setReadTimeout(readTimeout);
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Warp10-Token", this.token);
            con.setRequestProperty("Content-Type", "text/plain");
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write(body.toString().getBytes());
                os.flush();
            }

            int status = con.getResponseCode();

            if (status >= 200 && status < 300) {
                logger.info("successfully sent metrics to Warp10");
            } else if (status >= 400) {
                try (InputStream in = (status >= 400) ? con.getErrorStream() : con.getInputStream()) {
                    logger.error(String.format("failed to send metrics: %s", new BufferedReader(new InputStreamReader(in))
                            .lines().collect(joining("\n"))));
                }
            } else {
                logger.error("failed to send metrics: http " + status);
            }

            con.disconnect();
        } catch (Exception e) {
            logger.warn("failed to send metrics", e);
        }
    }
}
