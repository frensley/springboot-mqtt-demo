package mqtt;

import mqtt.repository.TrackRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MqttApplication.class)
public class MqttApplicationTests {


    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    TrackRepository repository;

    @Test
	public void contextLoads() {
	}

}
