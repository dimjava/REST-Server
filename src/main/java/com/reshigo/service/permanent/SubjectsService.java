package com.reshigo.service.permanent;

import com.microsoft.azure.storage.StorageException;
import com.reshigo.dao.FileUtil;
import com.reshigo.dao.permanent.SubjectsDao;
import com.reshigo.exception.NotFound;
import com.reshigo.model.entity.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by dmitry103 on 12/07/16.
 */

@Service
public class SubjectsService {
    @Autowired
    private SubjectsDao subjectsDao;

    @Autowired
    private FileUtil fileUtil;

    @Transactional(readOnly = true)
    public List<Subject> getAllSubjects() {
        List<Subject> s =  subjectsDao.getAll();

        return s;
    }

    @Transactional
    public void addSubject(Subject subject) {
        subjectsDao.save(subject);
    }

    @Transactional
    public byte[] getSubjectsIcon(Long id) throws NotFound, IOException, URISyntaxException, StorageException {
        Subject subject = subjectsDao.findOne(id);

        if (subject == null || subject.getIcon() == null) {
            throw new NotFound(null);
        }

        return fileUtil.getImage(subject.getIcon());
    }
}
