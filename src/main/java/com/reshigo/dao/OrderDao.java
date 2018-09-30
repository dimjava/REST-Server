package com.reshigo.dao;

import com.reshigo.model.entity.Message;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.Picture;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

import static com.reshigo.model.entity.permanent.OrderStatusEnum.*;

@Service
public class OrderDao extends AbstractEntityDao<Order, Long> {

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    public OrderDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<Order> get(String username, boolean isCustomer, Long offset, Long limit, Timestamp offsetDate, Long subjectId, Long maturityDate, OrderStatusEnum... statuses) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Order.class);

        criteria.addOrder(org.hibernate.criterion.Order.desc("date"));

        if (username != null) {
            if (!isCustomer) {

                LogicalExpression solverRestriction = Restrictions.or(

                        Restrictions.and(
                                Restrictions.isNull("solver"),
                                Restrictions.and(
                                        Restrictions.in("status", statuses),
                                        Restrictions.eq("status", AVAILABLE)
                                )
                        ),

                        Restrictions.and(
                                Restrictions.eq("solver.name", username),
                                Restrictions.in("status", statuses)
                        )
                );

                criteria.add(solverRestriction);
            } else {
                criteria.add(Restrictions.or(Restrictions.eq("user.name", username), Restrictions.eq("solver.name", username)));
                criteria.add(Restrictions.in("status", statuses));
            }
        }

        if (offsetDate != null) {
            criteria.add(Restrictions.le("date", offsetDate));
        }

        if (subjectId >= 0) {
            criteria.add(Restrictions.between("theme.id", subjectId * 100, subjectId * 100 + 99));
        }

        if (maturityDate != null && maturityDate >= 0) {
            criteria.add(Restrictions.ge("maturityDate", new Date(maturityDate + 1000 * 60 * 5)));
        }

        if (offset < 0) {
            offset = 0L;
        }

        if (limit <= 0) {
            return new LinkedList<>();
        }

        criteria.setMaxResults(Math.toIntExact(limit));
        criteria.setFirstResult(Math.toIntExact(offset));

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        List<Order> orders = criteria.list();

        return orders;
/*
        if (limit + offset > orders.size()) {
            limit = 1L * orders.size() - offset;
        }

        return orders.subList(offset.intValue(), offset.intValue() + limit.intValue());
*/
    }

    public List<Order> getOrdersWithReviews(String solvername, Long offset, Long limit) {
        return (List<Order>) sessionFactory.getCurrentSession().createCriteria(Order.class)
                .add(Restrictions.eq("solver.name", solvername))
                .add(Restrictions.isNotNull("review"))
                .setMaxResults(limit.intValue())
                .setFirstResult(offset.intValue())
                .list();
    }

    public Order findOne(Long id) {
        Order order = (Order) sessionFactory.getCurrentSession().get(Order.class, id);

        return order;
    }

    public Order getWithLock(Long id) {
        List<Order> orders = sessionFactory.getCurrentSession().createCriteria(Order.class)
                .add(Restrictions.eq("id", id))
                .setLockMode(LockMode.PESSIMISTIC_WRITE)
                .list();

        if (orders.isEmpty()) {
            return null;
        }

        return orders.get(0);
    }

    public List<Picture> getPictures(Long id) {
        Order order = (Order) sessionFactory.getCurrentSession().get(Order.class, id);
        Hibernate.initialize(order.getPictures());

        return order.getPictures();
    }

    public void delete(Order order) {

        sessionFactory.getCurrentSession().delete(order);

        for (Picture picture : order.getPictures()) {
            fileUtil.deleteImage(picture.getImg().getPath());
        }

        List<Message> messages = order.getChat().getMessages();

        if (messages != null) {
            for (Message message : messages) {
                if (message.getPath() != null) {
                    fileUtil.deleteImage(message.getPath());
                }
            }
        }
    }

    public List<Order> orders(Long page, Long perPage, String nameFilter, String solverNameFilter, String status) {

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Order.class)
                //.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.like("user.name", nameFilter, MatchMode.ANYWHERE))
                .add(Restrictions.ne("status", OrderStatusEnum.DRAFT))
                .addOrder(org.hibernate.criterion.Order.desc("date"))
                .setFetchMode("pictures", FetchMode.LAZY)
                .setFetchMode("user", FetchMode.LAZY)
                .setFetchMode("solver", FetchMode.LAZY)
                .setFetchMode("theme", FetchMode.LAZY)
                .setFetchMode("chat", FetchMode.LAZY);

        if (!solverNameFilter.equals("")) {
            criteria.add(Restrictions.like("solver.name", solverNameFilter, MatchMode.ANYWHERE));
        }

        if (!status.equals("")) {
            criteria.add(Restrictions.sqlRestriction("status like ?", "%" + status + "%", StandardBasicTypes.STRING));
        }

        return criteria.setFirstResult((int)((page - 1) * perPage)).setMaxResults(perPage.intValue()).list();

//        List<Order> orders = criteria.list();
//
//        if ((page - 1) * perPage >= orders.size()) {
//            return new LinkedList<>();
//        }
//
//
//        int sz = (int) ((page - 1) * perPage);
//        return orders.subList(sz, Integer.min(sz + perPage.intValue(), orders.size()));
    }
}
