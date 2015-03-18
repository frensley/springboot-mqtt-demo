package mqtt.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 * Created by sfrensley on 3/15/15.
 * @Topic is domain model used to represent the MQTT queue name
 */
@NodeEntity
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"name"}) //use only id for equality
@ToString(callSuper = true)
public class Topic extends BaseEntity {
    /**
     * This is the name of the MQTT queue
     */
    String name;
}
