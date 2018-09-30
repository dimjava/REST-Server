package com.reshigo.dao;

import com.reshigo.model.entity.PriceSuggest;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by dmitry103 on 25/08/17.
 */
@Service
public class PriceSuggestDao extends AbstractEntityDao<PriceSuggest, Long> {
    @Autowired
    public PriceSuggestDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public PriceSuggest findOne(Long aLong) {
        return (PriceSuggest) sessionFactory.getCurrentSession().get(PriceSuggest.class, aLong);
    }

    public List<PriceSuggest> getSuggestions(Long orderId) {
        return sessionFactory.getCurrentSession().createCriteria(PriceSuggest.class)
                .add(Restrictions.eq("order.id", orderId)).list();
    }
}
