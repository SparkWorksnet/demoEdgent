package net.sparkworks.edgent;

import com.google.gson.JsonObject;
import org.apache.edgent.connectors.rabbitmq.RabbitmqConfigKeyConstants;
import org.apache.edgent.connectors.rabbitmq.RabbitmqConsumer;
import org.apache.edgent.connectors.rabbitmq.RabbitmqProducer;
import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

import java.util.HashMap;
import java.util.Map;

import static net.sparkworks.edgent.MappingUtils.sensorDataMapFunction;

public class StreamListener {
    
    /**
     * Run a topology with a RabbitMQ connector printing readings to standard out.
     *
     * @param args command arguments
     * @throws Exception on failure
     */
    public static void main(String[] args) throws Exception {
        
        System.out.println("StreamListener: Simple output from RabbitMQ");
        
        final DirectProvider tp = new DevelopmentProvider();
        
        final Topology topology = tp.newTopology("StreamListener");
        
        // RabbitMQ Connector
        final Map<String, Object> config = new HashMap<>();
        config.put(RabbitmqConfigKeyConstants.RABBITMQ_CONFIG_KEY_HOST, EdgentConfiguration.brokerHost);
        config.put(RabbitmqConfigKeyConstants.RABBITMQ_CONFIG_KEY_PORT, EdgentConfiguration.brokerPort);
        config.put(RabbitmqConfigKeyConstants.RABBITMQ_CONFIG_KEY_AUTH_NAME, EdgentConfiguration.username);
        config.put(RabbitmqConfigKeyConstants.RABBITMQ_CONFIG_KEY_AUTH_PASSWORD, EdgentConfiguration.password);
        
        final RabbitmqConsumer consumer = new RabbitmqConsumer(topology, () -> config);
        final TStream<String> receivedStream = consumer.subscribe((byte[] bytes) -> new String(bytes), EdgentConfiguration.queue);
        
        final TStream<JsonObject> tach = receivedStream.map(ja -> sensorDataMapFunction(ja));
        
        tach.print();
        
        if (EdgentConfiguration.doOutput) {
            final RabbitmqProducer producer = new RabbitmqProducer(topology, () -> config);
            producer.publish(tach, EdgentConfiguration.outExchange, (JsonObject s) -> s.toString().getBytes());
        }
        
        System.out.println("#### Console URL for the job: " + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        
        tp.submit(topology);
    }
    
    
}
