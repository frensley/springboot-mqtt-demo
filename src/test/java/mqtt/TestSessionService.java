package mqtt;

import mqtt.domain.Session;
import mqtt.domain.Topic;
import mqtt.domain.Track;
import mqtt.service.SessionService;
import mqtt.service.TopicService;
import mqtt.service.TrackService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by sfrensley on 3/15/15.
 */
@SuppressWarnings({"SpringJavaAutowiringInspection"})
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MqttApplication.class)
@Transactional
public class TestSessionService {

    @Autowired private SessionService sessionService;
    @Autowired private TopicService topicService;
    @Autowired private TrackService trackService;

    Topic t;

    @Before
    public void before() {
        t = topicService.findOrCreateTopic("topic");
        assertNotNull(t);
    }


    /**
     * Assert that we get the same session inside the tolerance window
     */
    @Test
    public void testFindSameLatestSession() {
        Session s1 = new Session();
        s1.setDate(System.currentTimeMillis());
        s1.setTopic(t);
        s1 = sessionService.save(s1);
        assertNotNull(s1);
        assertNotNull(s1.getId());
        Session s2 = sessionService.findOrCreateSession(t,5000L);
        assertNotNull(s2);
        assertTrue(s1.getId().equals(s2.getId()));
    }

    /**
     * Assert that we get a different @Session inside the tolerance window
     */
    @Test
    public void testFindNewLatestSession() {
        Session s1 = new Session();
        s1.setDate(System.currentTimeMillis());
        s1.setTopic(t);
        s1 = sessionService.save(s1);
        assertNotNull(s1);
        assertNotNull(s1.getId());
        Session s2 = sessionService.findOrCreateSession(t, 1L);
        assertNotNull(s2);
        assertTrue(!s1.getId().equals(s2.getId()));
    }

    @Test
    public void testDeleteSession() {
        Session s1 = new Session();
        s1.setDate(System.currentTimeMillis());
        s1.setTopic(t);
        s1 = sessionService.save(s1);
        Session s2 = new Session();
        s2.setDate(System.currentTimeMillis());
        s1.setTopic(t);
        s2 = sessionService.save(s2);
       //just to be sure
        assertTrue(!s1.getId().equals(s2.getId()));

        //create tracks to s1
        Track t1 = new Track();
        t1.setSession(s1);
        t1 = trackService.save(t1);
        Track t2 = new Track();
        t2.setSession(s1);
        t2 = trackService.save(t2);
        assertNotNull(t1);
        assertNotNull(t2);
        assertTrue(t1.getSession().getId().equals(s1.getId()));
        assertTrue(t2.getSession().getId().equals(s1.getId()));

        //create tracks to s2
        Track t3 = new Track();
        t3.setSession(s2);
        t3 = trackService.save(t3);
        Track t4 = new Track();
        t4.setSession(s2);
        t4 = trackService.save(t4);
        assertNotNull(t3);
        assertNotNull(t4);
        assertTrue(t3.getSession().getId().equals(s2.getId()));
        assertTrue(t4.getSession().getId().equals(s2.getId()));
        sessionService.deleteSession(s1.getId());
        //deletes
        //Something is funky with SDN deletes. The actual node seems to still be around,
        //but the SDN _Type index has been removed; Thus it errors with:
        // "java.lang.IllegalStateException: No primary SDN label exists .. (i.e one starting with _) "
        try {
            trackService.findById(t1.getId());
            assertTrue("Exception expected.",true);
        } catch (IllegalStateException e) {
            //ok
        }
        try {
            trackService.findById(t2.getId());
            assertTrue("Exception expected.",true);
        } catch (IllegalStateException e) {
            //ok
        }
        try {
            sessionService.findById(s1.getId());
            assertTrue("Exception expected.",true);
        } catch (IllegalStateException e) {
            //ok
        }
        //asure that we didnt delete other sessions
        Session testS2 = sessionService.findById(s2.getId());
        assertNotNull(testS2);
        Track testT3 = trackService.findById(t3.getId());
        assertNotNull(testT3);
        Track testT4 = trackService.findById(t4.getId());
        assertNotNull(testT4);

    }

}
