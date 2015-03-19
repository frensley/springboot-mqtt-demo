package mqtt;

import mqtt.domain.Topic;
import mqtt.service.TopicService;
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
public class TestTopicService {

    @Autowired
    TopicService service;

    /**
     * Assert that same session is returned for same name.
     * Assert that different session is return for different name.
     */
    @Test
    public void testFindOrCreate() {
        Topic t1 = service.findOrCreateTopic("foo");

        assertNotNull(t1);
        assertNotNull(t1.getId());

        Topic t2 = service.findOrCreateTopic("foo");
        assertTrue(t1.getId() == t2.getId());

        Topic t3 = service.findOrCreateTopic("bar");
        assertTrue(t3.getId() != t2.getId());
    }

}
