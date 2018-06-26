package net.sparkworks.edgent;

import com.google.gson.JsonObject;

public class MappingUtils {
    /**
     * Converts a value received from the RabbitMQ to a JSON object
     *
     * @param value the comma separated value received.
     * @return the JSON object.
     */
    public static JsonObject sensorDataMapFunction(final String value) {
        final JsonObject j = new JsonObject();
        
        final String[] items = value.split(",");
    
        final String urn = items[0];
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
