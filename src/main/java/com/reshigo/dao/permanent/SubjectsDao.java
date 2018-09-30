package com.reshigo.dao.permanent;

import com.reshigo.dao.AbstractEntityDao;
import com.reshigo.model.entity.Subject;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by dmitry103 on 12/07/16.
 */

@Service
public class SubjectsDao {
    @Autowired
    private SessionFactory sessionFactory;

    public List<Subject> getAll() {
        return sessionFactory.getCurrentSession().createCriteria(Subject.class).list();
    }

    public void save(Subject subject) {
        sessionFactory.getCurrentSession().save(subject);
    }

    public Subject findOne(Long id) {
        return (Subject) sessionFactory.getCurrentSession().get(Subject.class, id);
    }
}
