package mqtt.repository;

import mqtt.domain.Session;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.repository.GraphRepository;


/**
 * Created by sfrensley on 3/15/15.
 */
public interface SessionRepository extends GraphRepository<Session> {

    @Query("match (t:Topic)<--(s:Session) where id(t)={0} return s order by s.date desc limit 1")
    public Session findLatestSession(Long topicId);

    @Query("match (t:Topic)<--(s:Session) where t.name={0} return s order by s.date desc limit 1")
    public Session findLatestSession(String topicName);

    @Query("match (t:Topic)<--(s:Session) where id(t)={0} return s order by s.date desc")
    public Result<Session> findSessionsByTopicId(Long topicId);
}
