package net.sparkworks.edgent;

import com.google.gson.JsonObject;
import net.sparkworks.EdgentConfiguration;
import org.apache.edgent.connectors.rabbitmq.RabbitmqConfigKeyConstants;
import org.apache.edgent.connectors.rabbitmq.RabbitmqConsumer;
import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

import java.util.HashMap;
import java.util.Map;

public class StreamListener {

    /**
     * Run a topology with a RabbitMQ connector printing readings to standard out.
     *
     * @param args command arguments
     * @throws Exception on failure
     */
    public static void main(String[] args) throws Exception {

        System.out.println("StreamListener: Simple output from RabbitMQ");

        DirectProvider tp = new DevelopmentProvider();

        Topology topology = tp.newTopology("StreamListener");

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
            return sensorDataMapFunction(ja);
        });

        tach.print();

        System.out.println("#### Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());

        tp.submit(topology);
    }

    /**
     * Converts a value received from the RabbitMQ to a JSON object
     *
     * @param value the comma separated value received.
     * @return the JSON object.
     */
    public static JsonObject sensorDataMapFunction(final String value) {
        JsonObject j = new JsonObject();

        final String[] items = value.split(",");

        String urn = items[0];
        j.addProperty("urn", urn);

        // extract value
        final String txtValue = items[1];
        try {
            final double doubleValue = Double.parseDouble(txtValue);

            j.addProperty("value", doubleValue);

        } catch (Exception ex) {
            // either district does not exist or it is not an integer
            // simply ignore
            j.addProperty("value", 0.0);
        }

        // extract timestamp
        final String txtTS = items[2];
        try {
            final long longValue = Long.parseLong(txtTS);

            j.addProperty("timestamp", longValue);

        } catch (Exception ex) {
            // either district does not exist or it is not an integer
            // simply ignore
            j.addProperty("timestamp", 0.0);
        }

        return j;
    }
}
