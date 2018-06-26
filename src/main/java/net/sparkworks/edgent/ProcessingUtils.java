package net.sparkworks.edgent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.edgent.analytics.math3.json.JsonAnalytics;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.TWindow;

import java.util.concurrent.TimeUnit;

import static org.apache.edgent.analytics.math3.stat.Statistic.MAX;
import static org.apache.edgent.analytics.math3.stat.Statistic.MEAN;
import static org.apache.edgent.analytics.math3.stat.Statistic.MIN;
import static org.apache.edgent.analytics.math3.stat.Statistic.STDDEV;

public class ProcessingUtils {
    /**
     * Create a stream containing the aggregated values.
     *
     * @param tach the stream to process.
     * @return Stream containing aggregated stream.
     */
    public static TStream<JsonObject> process(final TStream<JsonObject> tach) {
        
        // Create a window on the stream of the last 50 readings partitioned
        // by sensor name. In this case two independent windows are created (for a and b)
        final TWindow<JsonObject, JsonElement> sensorWindow = tach.last(10, TimeUnit.SECONDS, j -> j.get("urn"));
        
        // Aggregate the windows calculating the min, max, mean and standard deviation
        // across each window independently.
        final TStream<JsonObject> sensors = JsonAnalytics.aggregate(sensorWindow, "urn", "value", MIN, MAX, MEAN, STDDEV);
        
        // Filter so that only when the sensor is beyond 2.0 (absolute) is a reading sent.
        
        //        sensors = sensors.filter(j -> j.get("urn").getAsString().startsWith("0013a20040f6497b/0x331/sound"));
        
        return sensors;
        
    }
}
