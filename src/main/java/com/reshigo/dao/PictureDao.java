package com.reshigo.dao;

import com.microsoft.azure.storage.StorageException;
import com.reshigo.model.entity.Picture;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.PrePersist;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Service
public class PictureDao extends AbstractEntityDao<Picture, Long> {

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    public PictureDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Picture findOne(Long id) {
        Picture picture = (Picture) sessionFactory.getCurrentSession().get(Picture.class, id);

        if (picture == null) {
            return null;
        }

        try {
            picture.getImg().setData(fileUtil.getImage(picture.getImg().getPath()));
        } catch (IOException|StorageException|URISyntaxException e) {
            e.printStackTrace();

            return null;
        }

        return picture;
    }

    public List<Picture> getIdsAndCounters(Long orderId) {
        Criteria c = sessionFactory.getCurrentSession().createCriteria(Picture.class);

        c.add(Restrictions.eq("order.id", orderId));
        c.setProjection(Projections.projectionList().add(Projections.property("id"), "id")
                .add(Projections.property("counter"), "counter"));
        c.setResultTransformer(Transformers.aliasToBean(Picture.class));

        return c.list();
    }

    @Override
    public void save(Picture picture) throws IOException, URISyntaxException, StorageException {
        byte[] data = picture.getImg().getData();
        picture.getImg().setData(null);

        sessionFactory.getCurrentSession().save(picture);

        picture.getImg().setPath(fileUtil.saveOrdersImage(picture.getOrder().getId(), picture.getId(), data));
    }

    @Override
    public void delete(Picture picture) {
        super.delete(picture);

        fileUtil.deleteImage(picture.getImg().getPath());
    }
}
