package mqtt.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 * Created by sfrensley on 3/15/15.
 * Domain model used to to segment groups of messages (points)
 * by logical time.
 */
@NodeEntity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Session extends BaseEntity {
    Long date;
    Topic topic;
}
