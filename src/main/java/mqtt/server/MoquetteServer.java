package mqtt.server;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dna.mqtt.moquette.messaging.spi.impl.SimpleMessaging;
import org.dna.mqtt.moquette.server.ServerAcceptor;
import org.dna.mqtt.moquette.server.netty.NettyAcceptor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by sfrensley on 3/10/15.
 * Starts the MQTT message broker on configured port.
 * The MQTT is not setup well for external configuration.
 * TODO: Make more server more configurable - considerable effort.
 */
@Slf4j
@Data
public class MoquetteServer {


    private Properties server = new Properties();
    private ServerAcceptor m_acceptor;
    private SimpleMessaging messaging;
    private String name;

    public MoquetteServer() {
    }

    @PostConstruct
    public void start() throws IOException {
        log.info("Moquette Server starting...");
        this.messaging = SimpleMessaging.getInstance();
        this.messaging.init(server);
        this.m_acceptor = new NettyAcceptor();
        this.m_acceptor.initialize(this.messaging,server);
        log.info("Moquette Server started.");
    }

    @PreDestroy
    public void stop() {
        log.info("MoquetteServer stopping...");
        this.messaging.stop();
        this.m_acceptor.close();
        log.info("MoquetteServer stopped. {}",server);
    }

}
