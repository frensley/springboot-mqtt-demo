package mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mqtt.domain.*;
import mqtt.service.SessionService;
import mqtt.service.TrackService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.util.StringUtils;

import javax.jms.*;
import java.io.IOException;

/**
 * Created by sfrensley on 3/29/15.
 */
@Data
@Slf4j
public class MqttConsumer extends SimpleMessageListenerContainer {

    private String host = "vm://localhost";
    private String subscriptions = "owntracks.user.*";
    private Long timeout = 5000L;
    //Activity window before a new session is established
    private Long sessionWindowSeconds = 5 * 60L;
    //How close before this is considered a new point (see withinDistance for Neo4j)
    private Double proximityWindow = .01;

    private ObjectMapper mapper = new ObjectMapper();
    private TrackService trackService;
    private SessionService sessionService;

    private static final String TOPIC_PREFIX = "topic://";

    public MqttConsumer(TrackService trackService,
                        SessionService sessionService) {

        this.trackService = trackService;
        this.sessionService = sessionService;
    }

    @Override
    public void afterPropertiesSet() {
        this.setupMessageListener(new Listener());
        this.setConnectionFactory(new ActiveMQConnectionFactory(host));
        this.setPubSubDomain(true);
        this.setDestinationName(subscriptions);
        super.afterPropertiesSet();
    }

    private class Listener implements MessageListener {
        @Override
        public void onMessage(Message message) {
            String text = null;
            Destination destination = null;
            try {
                destination = message.getJMSDestination();
                if (message instanceof TextMessage) {
                    text = ((TextMessage) message).getText();
                } else if (message instanceof BytesMessage) {
                    final BytesMessage bytesMessage = (BytesMessage) message;
                    byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                    bytesMessage.readBytes(bytes);
                    text = new String(bytes);
                }

            } catch (JMSException ex) {
                log.warn("Error: ", ex);
            }

            if (text == null) {
                return;
            }
            if (destination instanceof ActiveMQTopic) {
                //name without topic://
                String topic = ((ActiveMQTopic) destination).getPhysicalName();
                //topic names have periods in them. We use slashes.
                topic = StringUtils.replace(topic,".","/");
                log.info("Track received for: {} destination: {}", text, topic);
                try {
                    persistMessage(topic, mapper.readValue(text, Track.class));
                } catch (IOException e) {
                    log.error("Error processing message: ", e);
                }
            }
        }

        private void persistMessage(String topic, Track msg) {
            try {
                log.info("Entity: {}", msg);
                try {
                    if (trackService.exists(topic,msg)) {
                        log.info("Message exists.");
                    }

                    mqtt.domain.Session session = sessionService.findOrCreateSession(topic,sessionWindowSeconds * 1000);
                    if (session == null) {
                        log.error("Unable to process message because track session is null.");
                        return;
                    }
                    //Don't merge another point if it's inside our point tolerance radius for this session because it clutters the map
                    //This will be a problem for circular routes as the final route point will not appear on the map
                    //perhaps calculate distance and time (or just time)
                    if (trackService.isWithinDistanceForSession(msg, proximityWindow,session)) {
                        log.info("Message inside radius");
                    } else {
                        //Update session date to keep alive
                        session.setDate(System.currentTimeMillis());
                        session = sessionService.save(session);
                        msg.setSession(session);
                        msg = trackService.save(msg);
                        log.info("Saved Entity: {} Session: {}", msg,session);
                    }
                } catch (Exception e) {
                    log.error("Saved Exception:", e);
                }
            } catch (Exception e) {
                //must catch everything - client will exit if and exception is thrown.
                log.error("Something bad happened.",e);
            }
        }
    }
}
