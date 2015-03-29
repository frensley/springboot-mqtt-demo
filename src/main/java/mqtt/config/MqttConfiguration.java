package mqtt.config;

import lombok.extern.slf4j.Slf4j;
import mqtt.MqttBroker;
import mqtt.MqttConsumer;
import mqtt.service.SessionService;
import mqtt.service.TrackService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

/**
 * Created by sfrensley on 3/11/15.
 */
@Configuration
@Profile("!test")
@Slf4j
public class MqttConfiguration {

    @Bean
    @DependsOn({"brokerService"})
    @ConfigurationProperties("mqtt.consumer")
    public SimpleMessageListenerContainer consumerService (
            TrackService trackService,
            SessionService sessionService) throws Exception {
        return new MqttConsumer(trackService, sessionService);
    }

    @Bean
    @DependsOn({"graphDatabaseService"})
    @ConfigurationProperties("mqtt.broker")
    public MqttBroker brokerService()  {
        return new MqttBroker();
    }

}
