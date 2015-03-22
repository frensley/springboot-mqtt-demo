package mqtt.service;

import mqtt.domain.Session;
import mqtt.domain.Track;
import mqtt.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sfrensley on 3/13/15.
 * Mid-tier service to access repository and execute business logic
 */
@Service
@Transactional
public class TrackService {

    TopicService topicService;
    TrackRepository repository;
    Neo4jTemplate template;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public TrackService(TrackRepository repository, Neo4jTemplate template) {
        this.repository = repository;
        this.template = template;
    }

    /**
     * Find @Track by it's id
     * @param id
     * @return
     */
    public Track findById(Long id) {
        return repository.findOne(id);
    }

    /**
     * Return all tracks
     * @return
     */
    public List<Track> findAll() {
        return repository.findAll().as(ArrayList.class);
    }

    /**
     * Merge @Track to graph
     * @param entity
     * @return
     */
    public Track save(Track entity) {
        return repository.save(entity);
    }

    /**
     * Are there any @Track within distance.
     * @param track
     * @param distance
     * @return
     */
    public boolean isWithinDistance(Track track, double distance) {
        List<Track> p = repository.findWithinDistance(Track.wktIndexName, track.getLat().doubleValue(), track.getLon().doubleValue(), distance).as(ArrayList.class);
        return !p.isEmpty();
    }

    /**
     * Are there any @Track within distance for this @Session only.
     * @param track
     * @param distance
     * @param session
     * @return
     */
    public boolean isWithinDistanceForSession(Track track, double distance, Session session) {
        //TODO: Framework having issues parsing old spatial format for SDN. Move to extended repository? Also prime for injection attack?
        String withinDistance = String.format("withinDistance:[%f,%f,%f]",track.getLat().doubleValue(), track.getLon().doubleValue(), distance);

        //TODO: This is so broken. There has to be a better way to execute legacy index queries.
        String query = "start n=node:" + Track.wktIndexName + "('" + withinDistance + "') match (s:Session)<--(n) where id(s)={id} return s";
        Map<String,Object> params = new HashMap<>();
        params.put("lat",track.getLat());
        params.put("lon",track.getLon());
        params.put("distance",distance);
        params.put("id",session.getId());

        Result p  = template.query(query, params);
        return p.iterator().hasNext();
    }

    /**
     * Find @Track with @Topic and timestamp
     * @param topic
     * @param timestamp
     * @return
     */
    public Track findByTopicAndTimestamp(String topic, Long timestamp) {
        return repository.findByTopicAndTimestamp(topic, timestamp);
    }

    /**
     * Check for existance of @Track with @Topic and timestamp
     * @param topicName
     * @param track
     * @return
     */
    public boolean exists(String topicName, Track track) {
        Track t = repository.findByTopicAndTimestamp(topicName, track.getTst().longValue());
        return t != null;
    }

    /**
     * Find all @Track for @Topic id
     * @param topicId
     * @return
     */
    public List<Track> findAllForTopic(Long topicId) {
        return repository.findAllForTopic(topicId).as(ArrayList.class);
    }

    /**
     * Find all @Track for @Session id
     * @param sessionId
     * @return
     */
    public List<Track> findAllForSession(Long sessionId) {
        return repository.findAllForSession(sessionId).as(ArrayList.class);
    }

    /**
     * Delete all @Track for specific session
     * @param sessionId
     */
    public void deleteAllForSession(Long sessionId) {
        repository.delete(findAllForSession(sessionId));
    }
}
