package mqtt.repository;

import mqtt.domain.Topic;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * Created by sfrensley on 3/15/15.
 */
public interface TopicRepository extends GraphRepository<Topic> {

    public Topic findByName(String name);
}
