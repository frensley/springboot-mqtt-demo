package mqtt.service;

import mqtt.domain.Topic;
import mqtt.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sfrensley on 3/15/15.
 */
@Service
@Transactional
public class TopicService {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private TopicRepository repository;

    private static final Sort SORT_BY_NAME_ASC = new Sort(Sort.Direction.ASC, "name");

    public Topic findOrCreateTopic(String name) {
        Topic t = repository.findByName(name);
        if (t == null) {
            t = new Topic();
            t.setName(name);
            t = repository.save(t);
        }
        return t;
    }

    public Topic save(Topic topic) {
        return repository.save(topic);
    }

    public Topic findById(Long id) {
        return repository.findOne(id);
    }

    public Topic findByName(String name) {
        return repository.findByName(name);
    }

    public List<Topic> findAllSortByNameAsc() {
        return repository.findAll(SORT_BY_NAME_ASC).as(ArrayList.class);
    }
}
