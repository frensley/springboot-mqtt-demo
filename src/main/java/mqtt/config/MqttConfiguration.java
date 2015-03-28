package mqtt.config;

import lombok.extern.slf4j.Slf4j;
import mqtt.consumer.MqttConsumer;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

import java.util.Properties;

/**
 * Created by sfrensley on 3/11/15.
 */
@Configuration
@Profile("!test")
@Slf4j
public class MqttConfiguration {

    @Bean
    @DependsOn({"brokerService"})
    @ConfigurationProperties(prefix = "mqtt.consumer")
    public MqttConsumer consumer() throws Exception {
        return new MqttConsumer();
    }

    @Bean
    @ConfigurationProperties("mqtt.server")
    public Properties mqttServerConfiguration() {
        return new Properties();
    }

    @Bean
    @DependsOn({"graphDatabaseService", "mqttServerConfiguration"})
    public BrokerService brokerService() throws Exception {

        log.info("Mqtt server configuration: {}", mqttServerConfiguration());
        Properties props = mqttServerConfiguration();


        String uri = new StringBuilder()
                .append("broker:(")
                .append("vm://localhost,")
                // + "stomp://localhost:%d,"
                .append(String.format("mqtt+nio://%s:%s", props.getProperty("server", "0.0.0.0"), props.getProperty("port", "8883")))
                .append(")?")
                .append(String.format("persistent=%s&useJmx=%s", props.getProperty("persistent","false"), props.getProperty("jmx","false")))
                .toString();

//        final SimpleAuthenticationPlugin authenticationPlugin = new SimpleAuthenticationPlugin();
//        authenticationPlugin.setAnonymousAccessAllowed(false);
//        authenticationPlugin.setUsers(Arrays.asList(new AuthenticationUser(properties.getUsername(), properties.getPassword(), "")));
//        rv.setPlugins(new BrokerPlugin[]{authenticationPlugin});
        log.info("Creating broker service with uri: {}",uri);
        return BrokerFactory.createBroker(uri);
    }
}
