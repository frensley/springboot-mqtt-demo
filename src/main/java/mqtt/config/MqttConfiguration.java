package mqtt.config;

import mqtt.consumer.MqttConsumer;
import mqtt.server.MoquetteServer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

/**
 * Created by sfrensley on 3/11/15.
 */
@Configuration
@Profile("!test")
public class MqttConfiguration {

    @Bean
    @DependsOn({"graphDatabaseService"})
    @ConfigurationProperties(prefix = "mqtt")
    public MoquetteServer server() {
        return new MoquetteServer();
    }

    @Bean
    @DependsOn({"server"})
    @ConfigurationProperties(prefix = "mqtt.consumer")
    public MqttConsumer consumer() throws Exception {
        return new MqttConsumer();
    }
}
