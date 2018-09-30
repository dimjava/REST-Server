package com.reshigo.dao;

import com.reshigo.model.entity.Topic;
import com.reshigo.model.entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dmitry103 on 01/01/2018.
 */

@Service
public class TopicDao {
    protected final SessionFactory sessionFactory;

    @Autowired
    public TopicDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<Topic> getByTopic(String topic) {
        List res = sessionFactory.getCurrentSession().createCriteria(Topic.class).add(Restrictions.eq("name", topic)).list();

        if (res != null) {
            return (List<Topic>) res;
        }

        return new LinkedList<>();
    }

    public void delete(Topic t) {
        sessionFactory.getCurrentSession().delete(t);
    }
}
