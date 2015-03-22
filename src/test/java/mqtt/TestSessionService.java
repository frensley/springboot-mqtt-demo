package mqtt;

import mqtt.domain.Session;
import mqtt.domain.Topic;
import mqtt.service.SessionService;
import mqtt.service.TopicService;
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
@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MqttApplication.class)
@Transactional
public class TestSessionService {

    @Autowired private SessionService sessionService;
    @Autowired private TopicService topicService;

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



}
