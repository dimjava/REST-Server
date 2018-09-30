package com.reshigo.dao.permanent;

import com.reshigo.model.entity.Theme;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by dmitry103 on 12/07/16.
 */

@Service
public class ThemesDao {
    @Autowired
    private SessionFactory sessionFactory;

    public Theme findOne(Long id) {
        return (Theme) sessionFactory.getCurrentSession().get(Theme.class, id);
    }

    public List<Theme> getAll() {
        return sessionFactory.getCurrentSession().createCriteria(Theme.class).list();
    }

    public void save(Theme theme) {
        sessionFactory.getCurrentSession().save(theme);
    }
}
