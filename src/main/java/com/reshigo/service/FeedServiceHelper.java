package com.reshigo.service;

import com.reshigo.dao.FeedDao;
import com.reshigo.dao.TopicDao;
import com.reshigo.dao.UserDao;
import com.reshigo.model.entity.Feed;
import com.reshigo.model.entity.Topic;
import com.reshigo.model.entity.User;
import com.reshigo.notifications.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Created by dmitry103 on 18/08/17.
 */

@Service
public class FeedServiceHelper {
    private Logger logger = LoggerFactory.getLogger(FeedServiceHelper.class);

    @Autowired
    private FeedDao feedDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TopicDao topicDao;

    @Transactional
    public void send(Notification o, User user, boolean echo) {
        Feed f;

        try {
            f = new Feed(o, user, echo);
        } catch (Exception e) {
            logger.error("Failed to convert notification to string. Notification type: {}", o.getType(), e);

            return;
        }

        try {
            feedDao.save(f);
        } catch (Exception e) {
            logger.error("Failed to save feed in database: {}", f, e);
        }
    }

    @Transactional
    public void send(Notification o, User user) {
        send(o, user, false);
    }

    @Transactional
    public User send(Notification o, String name) {
        User solver = userDao.findOne(name);

        send(o, solver, false);

        return solver;
    }

    @Transactional
    public List<Topic> getUsersByTopic(String topic) {
        return topicDao.getByTopic(topic);
    }

    @Transactional
    public void subscribe(User user, String topic) {
        User u = userDao.findOne(user.getName());

        if (u == null) {
            return;
        }

        for (Topic t : u.getTopics()) {
            if (t.getName().equalsIgnoreCase(topic)) {
                return;
            }
        }

        Topic t = new Topic();
        t.setUser(u);
        t.setName(topic);
        u.getTopics().add(t);

        userDao.update(user);
    }

    @Transactional
    public void unsubscribe(User user, String topic) {
        User u = userDao.findOne(user.getName());

        if (u == null) {
            return;
        }

        for (Topic t : u.getTopics()) {
            if (t.getName().equalsIgnoreCase(topic)) {
                u.getTopics().remove(t);
                topicDao.delete(t);

                break;
            }
        }
    }
}
