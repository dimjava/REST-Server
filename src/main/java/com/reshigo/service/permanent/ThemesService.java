package com.reshigo.service.permanent;

import com.reshigo.dao.permanent.SubjectsDao;
import com.reshigo.dao.permanent.ThemesDao;
import com.reshigo.model.entity.Theme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by dmitry103 on 12/07/16.
 */

@Service
public class ThemesService {
    @Autowired
    private ThemesDao themesDao;

    @Autowired
    private SubjectsDao subjectsDao;

    @Transactional(readOnly = true)
    public List<Theme> getAllThemes() {
        return themesDao.getAll();
    }

    @Transactional
    public void addTheme(Theme theme) {
        theme.setSubject(subjectsDao.findOne((long) (theme.getId() / 100)));

        themesDao.save(theme);
    }
}
