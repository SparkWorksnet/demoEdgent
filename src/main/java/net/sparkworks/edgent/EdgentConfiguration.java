package   net.sparkworks.edgent;

/**
 * Central point for setting the configuration parameters
 *
 * @author ichatz@gmail.com
 */
public interface EdgentConfiguration {

    public final String brokerHost = "rabbitmq";

    public final int brokerPort = 5672;

    public final String brokerVHost = "/";

    public final String queue = "annotated-readings-processing";

    public final String username = "bugs";

    public final String password = "bunny";

    public final String outExchange = "summary";

    public final boolean doOutput = false;

}
