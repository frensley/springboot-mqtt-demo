package mqtt.api;

import mqtt.domain.Session;
import mqtt.domain.Topic;
import mqtt.domain.Track;
import mqtt.service.SessionService;
import mqtt.service.TopicService;
import mqtt.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by sfrensley on 3/14/15.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@RestController
@RequestMapping("/api/")
public class RestApi {

    /**
     * TODO: Convert everything to page requests that need it
     */

    @Autowired TrackService trackService;
    @Autowired TopicService topicService;
    @Autowired SessionService sessionService;

    /**
     * Returns List of @Track for given sessionId
     * @param sessionId
     * @return
     */
    @RequestMapping("/tracks/{sessionId}")
    public List<Track> points(@PathVariable("sessionId") Long sessionId) {
        return trackService.findAllForSession(sessionId);
    }

    /**
     * Returns List of @Session for given topicId
     * @param topicId
     * @return
     */
    @RequestMapping("/sessions/{topicId}")
    public List<Session> sessions(@PathVariable("topicId") Long topicId) {
        return sessionService.findSessionsByTopicId(topicId);
    }

    /**
     * Returns List of @Topic for entire system
     * @return
     */
    @RequestMapping("/topics")
    public List<Topic> topics() {
        return topicService.findAllSortByNameAsc();
    }
}
