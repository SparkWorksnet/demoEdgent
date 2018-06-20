package net.sparkworks.edgent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.sparkworks.EdgentConfiguration;
import org.apache.edgent.analytics.math3.json.JsonAnalytics;
import org.apache.edgent.connectors.rabbitmq.RabbitmqConfigKeyConstants;
import org.apache.edgent.connectors.rabbitmq.RabbitmqConsumer;
import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.TWindow;
import org.apache.edgent.topology.Topology;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.edgent.analytics.math3.stat.Statistic.*;

public class StreamProcessor {

    /**
     * Run a topology with two bursty sensors printing them to standard out.
     *
     * @param args command arguments
     * @throws Exception on failure
     */
    public static void main(String[] args) throws Exception {
        System.out.println("StreamProcessor: Process incomming messages from RabbitMQ");

        DirectProvider tp = new DevelopmentProvider();

        Topology topology = tp.newTopology("StreamProcessor");

        // RabbitMQ Connector
        Map<String, Object> config = new HashMap<>();
        config.put(RabbitmqConfigKeyConstants.RABBITMQ_CONFIG_KEY_HOST, EdgentConfiguration.brokerHost);
        config.put(RabbitmqConfigKeyConstants.RABBITMQ_CONFIG_KEY_PORT, EdgentConfiguration.brokerPort);
        config.put(RabbitmqConfigKeyConstants.RABBITMQ_CONFIG_KEY_AUTH_NAME, EdgentConfiguration.username);
        config.put(RabbitmqConfigKeyConstants.RABBITMQ_CONFIG_KEY_AUTH_PASSWORD, EdgentConfiguration.password);
        String queue = "ichatz-annotated-readings";

        RabbitmqConsumer consumer = new RabbitmqConsumer(topology, () -> config);
        TStream<String> receivedStream = consumer.subscribe((byte[] bytes) -> new String(bytes), queue);

        TStream<JsonObject> tach = receivedStream.map(ja -> {
            return StreamListener.sensorDataMapFunction(ja);
        });

        TStream<JsonObject> sensors = process(tach);

        sensors.print();

        System.out.println("#### Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());

        tp.submit(topology);
    }

    /**
     * Create a stream containing the aggregated values.
     *
     * @param tach the stream to process.
     * @return Stream containing aggregated stream.
     */
    public static TStream<JsonObject> process(TStream<JsonObject> tach) {

        // Create a window on the stream of the last 50 readings partitioned
        // by sensor name. In this case two independent windows are created (for a and b)
        TWindow<JsonObject, JsonElement> sensorWindow = tach.last(10, TimeUnit.SECONDS, j -> j.get("urn"));

        // Aggregate the windows calculating the min, max, mean and standard deviation
        // across each window independently.
        TStream<JsonObject> sensors = JsonAnalytics.aggregate(sensorWindow, "urn", "value", MIN, MAX, MEAN, STDDEV);

        // Filter so that only when the sensor is beyond 2.0 (absolute) is a reading sent.

        sensors = sensors.filter(j -> j.get("urn").getAsString().startsWith("0013a20040f6497b/0x331/sound"));

        return sensors;

    }

}
