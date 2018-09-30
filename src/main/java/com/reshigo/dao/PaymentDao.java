package com.reshigo.dao;

import com.reshigo.model.entity.Payment;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by dmitry103 on 24/11/16.
 */

@Service
public class PaymentDao extends AbstractEntityDao<Payment,Long> {
    @Autowired
    public PaymentDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Payment findOne(Long id) {
        return (Payment) sessionFactory.getCurrentSession().get(Payment.class, id);
    }

    public Payment getPaymentWithLock(Long id) {
        List<Payment> payments = sessionFactory.getCurrentSession().createCriteria(Payment.class)
                .add(Restrictions.eq("id", id))
                .setLockMode(LockMode.PESSIMISTIC_WRITE).list();

        if (!payments.isEmpty()) {
            return payments.get(0);
        }

        return null;
    }

    public List<Payment> getPayments(boolean completed, Long page, Long perPage, String nameFilter) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Payment.class)
                .add(Restrictions.eq("completed", completed));

        criteria.addOrder(org.hibernate.criterion.Order.desc("date"));

        if (!nameFilter.equals("")) {
            criteria.add(Restrictions.like("user.name", nameFilter, MatchMode.ANYWHERE));
        }

        criteria.setFirstResult((int) ((page - 1) * perPage))
                .setMaxResults(perPage.intValue());

        return criteria.list();
    }
    public List<Payment> getPayments(Date from, Date until) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Payment.class)
                .add(Restrictions.ge("date", from))
                .add(Restrictions.lt("date", until))
                .add(Restrictions.eq("completed", true));

        return criteria.list();
    }
}
