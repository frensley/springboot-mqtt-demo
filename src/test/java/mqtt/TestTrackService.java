package mqtt;

import mqtt.domain.Session;
import mqtt.domain.Topic;
import mqtt.domain.Track;
import mqtt.service.SessionService;
import mqtt.service.TopicService;
import mqtt.service.TrackService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by sfrensley on 3/16/15.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MqttApplication.class)
@Transactional
public class TestTrackService {


    @Autowired TrackService trackService;
    @Autowired SessionService sessionService;
    @Autowired TopicService topicService;



    @Test
    public void testFindByTopicAndTimestamp() {
        Topic t = topicService.findOrCreateTopic("topic");
        //
        assertNotNull(t);
        Session s1 = new Session();
        s1.setDate(System.currentTimeMillis());
        s1.setTopic(t);
        s1 = sessionService.save(s1);
        assertNotNull(s1.getId());
        //
        Track track = new Track();
        track.setSession(s1);
        track.setTst(9999L);
        track = trackService.save(track);
        assertNotNull(track.getId());
        //
        Track track1 = trackService.findByTopicAndTimestamp(t.getName(), track.getTst().longValue());
        assertNotNull(track1);
        assertTrue(track1.getId() == track.getId());
    }

    @Test
    public void testFindWithinDistanceForSession() {
        Topic t = topicService.findOrCreateTopic("topic");
        //
        assertNotNull(t);
        Session s1 = new Session();
        s1.setDate(System.currentTimeMillis());
        s1.setTopic(t);
        s1 = sessionService.save(s1);
        assertNotNull(s1.getId());
        //
        Track track = new Track();
        track.setSession(s1);
        track.setTst(9999L);
        track.setLocation(-97.7477,30.2603);
        track = trackService.save(track);
        assertNotNull(track.getId());
        //Is point within 20 meters?
        boolean s3 = trackService.isWithinDistanceForSession(track, 20.0D, s1);
        assertTrue(s3);
        //Move point more than 20 meters.
        track.setLocation(-90.7477,30.2603);
        boolean s4 = trackService.isWithinDistanceForSession(track, 20.0D, s1);
        assertTrue(!s4);
    }
}
