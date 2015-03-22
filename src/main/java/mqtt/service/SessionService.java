package mqtt.service;

import lombok.extern.slf4j.Slf4j;
import mqtt.domain.Session;
import mqtt.domain.Topic;
import mqtt.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sfrensley on 3/15/15.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Service
@Transactional
@Slf4j
@Repository
public class SessionService {

    private SessionRepository repository;
    private TopicService topicService;
    private TrackService trackService;

    @Autowired
    public SessionService(SessionRepository sessionRepository, TopicService topicService, TrackService trackService) {
        this.repository = sessionRepository;
        this.topicService = topicService;
        this.trackService = trackService;
    }

    public Session save(Session session) {
        return repository.save(session);
    }

    public Session findById(Long id) {
        return repository.findOne(id);
    }

    public List<Session> findSessionsByTopicId(Long topicId) {
        return repository.findSessionsByTopicId(topicId).as(ArrayList.class);
    }

    public Session findLatestSession(Long topicId) {
        return repository.findLatestSession(topicId);
    }

    public Session findOrCreateSession(String topicName, Long windowSeconds) {
        Topic topic = topicService.findOrCreateTopic(topicName);
        return findOrCreateSession(topic, windowSeconds);
    }

    public Session findOrCreateSession(Topic t, Long windowSeconds) {
        Session s =  findLatestSession(t.getId());
        Long cutoff = System.currentTimeMillis() - windowSeconds;
        if (s == null || s.getDate() < cutoff) {
            s = new Session();
            s.setDate(System.currentTimeMillis());
            s.setTopic(t);
            s = repository.save(s);
        }
        return s;
    }

    public void deleteSession(Long sessionId) {
        trackService.deleteAllForSession(sessionId);
        repository.delete(sessionId);
    }

}
