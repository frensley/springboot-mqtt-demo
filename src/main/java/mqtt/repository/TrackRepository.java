package mqtt.repository;

import mqtt.domain.Track;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.SpatialRepository;


/**
 * Created by sfrensley on 3/12/15.
 */
public interface TrackRepository extends GraphRepository<Track>,SpatialRepository<Track> {

    @Query("match (t:Topic)<--(s:Session)<--(tr:Track) where t.name = {0} and tr.tst = {1} return tr")
    public Track findByTopicAndTimestamp(String topic, Long timestamp);

    @Query("match (t:Topic)<--(s:Session)<--(tr:Track) where id(t)={0} return tr")
    public Result<Track> findAllForTopic(Long topicId);

    @Query("match (s:Session)<--(tr:Track) where id(s)={0} return tr")
    public Result<Track> findAllForSession(Long sessionId);

    @Query("start n=node:" + Track.wktIndexName + "({0}) match (s:Session)<--(n) where id(s)={1} return s")
    public Result<Track> findWithinDistanceForSession(String withinDistance, Long sessionId);

}
