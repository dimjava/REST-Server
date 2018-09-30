package com.reshigo.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public abstract class AbstractEntityDao<T, ID extends Serializable> {
    protected final SessionFactory sessionFactory;

    public AbstractEntityDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(T t) throws Exception {
        sessionFactory.getCurrentSession().save(t);
    }

    public void update(T t) {
        sessionFactory.getCurrentSession().update(t);
    }

    public void delete(T t) {
        sessionFactory.getCurrentSession().delete(t);
    }

    public abstract T findOne(ID id);

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }
}
