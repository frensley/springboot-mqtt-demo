package mqtt.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mqtt.domain.Session;
import mqtt.domain.Track;
import mqtt.service.SessionService;
import mqtt.service.TrackService;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;

/**
 * Created by sfrensley on 3/11/15.
 * This is simply another client on the MQTT broker.
 * It listens with a wild card subscription and persists messages that it deems worthy.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Slf4j
@Data
public class MqttConsumer {


    @Autowired TrackService trackService;
    @Autowired SessionService sessionService;

    MqttClient client;

    String host = "unitialized";
    String port = "unitialized";
    String name = "unitialized";
    List<String> subscriptions = Collections.emptyList();
    Long timeout = 5000L;
    //Activity window before a new session is established
    Long sessionWindowSeconds = 5 * 60L;
    //
    Double proximityWindow = .01;

    public MqttConsumer() {

    }

    @PostConstruct
    public void start () throws MqttException {
        log.info("Starting consumer");
        String uri = new StringBuilder()
                .append("tcp://")
                .append(host)
                .append(":")
                .append(port)
                .toString();
        client = new MqttClient(uri,name);
        client.setTimeToWait(timeout);
        client.setCallback(new ConsumerCallback());
        client.connect();
        client.subscribe(subscriptions.toArray(new String[subscriptions.size()]));
        log.info("Consumer started.");
    }

    @PreDestroy
    public void stop() {
        if (client != null) {
            try {
                client.close();
            } catch (MqttException e) {
                //
            }
        }
    }

    /**
     * TODO: This needs better solution. Checking distance will/could be slow under load.
     * PAHO client uses message queue of depth 10 and 1 thread to process.
     */
    class ConsumerCallback implements MqttCallback {

        ObjectMapper mapper = new ObjectMapper();

        @Override
        public void connectionLost(Throwable cause) {

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            try {
                log.info("Topic: {}", topic);
                log.info("Message: {}", message);
                Track msg = mapper.readValue(message.toString(), Track.class);
                log.info("Entity: {}", msg);
                try {
                    if (trackService.exists(topic,msg)) {
                        log.info("Message exists.");
                    }


                    Session session = sessionService.findOrCreateSession(topic,sessionWindowSeconds * 1000);
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

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    }

}
