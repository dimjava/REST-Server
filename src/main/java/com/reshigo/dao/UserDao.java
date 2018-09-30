package com.reshigo.dao;


import com.reshigo.model.entity.User;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserDao extends AbstractEntityDao<User, String> {
    @Autowired
    public UserDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<User> getUsers(Long page, Long perPage, String nameFilter, String phoneFilter) {
        List<User> users = sessionFactory.getCurrentSession().createCriteria(User.class)
                .add(Restrictions.like("name", nameFilter, MatchMode.ANYWHERE))
                .add(Restrictions.like("phone", phoneFilter, MatchMode.ANYWHERE))
                .setFirstResult((int) ((page - 1) * perPage))
                .setMaxResults(perPage.intValue())
                .list();

        return users;
    }

    public User findOne(String username) {
        User user = (User) sessionFactory.getCurrentSession().get(User.class, username);

        return user;
    }

    public User getUserWithLock(String username) {
       List<User> users = sessionFactory.getCurrentSession().createCriteria(User.class)
               .add(Restrictions.eq("name", username))
               .setLockMode(LockMode.PESSIMISTIC_WRITE).list();

        if (!users.isEmpty()) {
            return users.get(0);
        }

        return null;
    }

    public List<User> findByPhone(String phone) {
        List<User> users = (List<User>) sessionFactory.getCurrentSession().createQuery("from User where phone = :phoneNumber")
                .setParameter("phoneNumber", phone).list();


        return users;
    }

    public List<User> findPromo(String promoCode) {
        return sessionFactory.getCurrentSession().createCriteria(User.class).add(Restrictions.eq("promocode", promoCode)).list();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Object[]> getUserFundsStats() {
        return sessionFactory.getCurrentSession().createQuery(
                "select u.name, " +
                        "(select COALESCE(sum(p.amount), 0) from u.payments p where p.completed=1) " +
                        "- u.funds - " +
                        "(select COALESCE(sum(ord.price), 0) from u.orders ord where ord.status in (:statuses)) " +
                        "from User u " +
                        "where u.isCustomer=1"
        ).setParameterList("statuses", new OrderStatusEnum[] {OrderStatusEnum.DONE, OrderStatusEnum.DONE_MODER, OrderStatusEnum.DONE_SYS}).list();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Object[]> getSolverFundsStats() {
        return sessionFactory.getCurrentSession().createQuery(
                "select u.name, " +
                        "(select COALESCE(sum(p.amount), 0) from u.payments p where p.completed=1) " +
                        "- u.funds + " +
                        "(select COALESCE(sum(ord.price), 0) from Order ord where ord.solver.name = u.name and ord.status in (:statuses)) " +
                        "from User u " +
                        "where u.isCustomer=0"
        ).setParameterList("statuses", new OrderStatusEnum[] {OrderStatusEnum.DONE, OrderStatusEnum.DONE_MODER, OrderStatusEnum.DONE_SYS}).list();
    }
}
