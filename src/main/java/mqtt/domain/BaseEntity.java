package mqtt.domain;

import lombok.Data;
import org.springframework.data.neo4j.annotation.GraphId;

/**
 * Created by sfrensley on 3/13/15.
 * Base entity abstraction used to hold identifier,
 * create/update times etc.
 */

@Data
public abstract class BaseEntity {
    @GraphId
    private Long id;
}
