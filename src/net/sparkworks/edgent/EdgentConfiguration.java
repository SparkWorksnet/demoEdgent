package net.sparkworks;

/**
 * Central point for setting the configuration parameters
 *
 * @author ichatz@gmail.com
 */
public interface EdgentConfiguration {

    public final String brokerHost = "broker.sparkworks.net";

    public final int brokerPort = 5672;

    public final String brokerVHost = "/";

    public final String queue = "ichatz-annotated-readings";

    public final String username = "username";

    public final String password = "password";

    public final String outExchange = "ichatz-flink-result";

    public final boolean doOutput = false;

}
