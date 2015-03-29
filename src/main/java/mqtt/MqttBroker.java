package mqtt;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by sfrensley on 3/29/15.
 */
@Data
@Slf4j
public class MqttBroker {

    private BrokerService brokerService;
    String host = "0.0.0.0";
    Integer port = 8883;
    Boolean persistent = false;
    Boolean jmx = false;


    @PostConstruct
    public void start() {
        String uri = new StringBuilder()
                .append("broker:(")
                .append("vm://localhost,")
                        // + "stomp://localhost:%d,"
                .append(String.format("mqtt+nio://%s:%d", host, port))
                .append(")?")
                .append(String.format("persistent=%s&useJmx=%s", persistent, jmx))
                .toString();
        //Authentication
//        final SimpleAuthenticationPlugin authenticationPlugin = new SimpleAuthenticationPlugin();
//        authenticationPlugin.setAnonymousAccessAllowed(false);
//        authenticationPlugin.setUsers(Arrays.asList(new AuthenticationUser(properties.getUsername(), properties.getPassword(), "")));
//        rv.setPlugins(new BrokerPlugin[]{authenticationPlugin});
        log.info("Creating broker service with uri: {}",uri);
        try {
            brokerService =  BrokerFactory.createBroker(uri);
            brokerService.autoStart();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @PreDestroy
    public void stop() {
        try {
            brokerService.stop();
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }

}
