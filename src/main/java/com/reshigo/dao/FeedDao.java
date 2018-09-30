package com.reshigo.dao;

import com.reshigo.model.entity.Feed;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by dmitry103 on 28/06/17.
 */

@Service
public class FeedDao extends AbstractEntityDao<Feed, Long> {
    @Autowired
    public FeedDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Feed findOne(Long id) {
        List<Feed> list = sessionFactory.getCurrentSession().createCriteria(Feed.class).add(Restrictions.eq("id", id)).list();

        if (!list.isEmpty()) {
            return list.get(0);
        }

        return null;
    }

    public List<Feed> getFeeds(Long lastId, String username) {
        return sessionFactory.getCurrentSession().createCriteria(Feed.class)
                .add(Restrictions.eq("user.name", username))
                .add(Restrictions.ge("id", lastId))
                .list();
    }

    public List<Feed> getFeeds(Long page, Long perPage, String nameFilter) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Feed.class)
                .addOrder(org.hibernate.criterion.Order.desc("date"))
                .setFirstResult((int) ((page - 1) * perPage))
                .setMaxResults(perPage.intValue());

        if (!nameFilter.equals("")) {
            criteria.add(Restrictions.like("user.name", nameFilter, MatchMode.ANYWHERE));
        }

        return criteria.list();
    }
}
