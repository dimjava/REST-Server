package com.reshigo.controller.permanent;

import com.microsoft.azure.storage.StorageException;
import com.reshigo.exception.NotFound;
import com.reshigo.model.entity.Subject;
import com.reshigo.service.permanent.SubjectsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by dmitry103 on 11/07/16.
 */

@RestController
@RequestMapping(value = "/subjects")
public class SubjectsController {
    @Autowired
    private SubjectsService subjectsService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity< List<Subject> > getSubjects() {
        return new ResponseEntity<>(subjectsService.getAllSubjects(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{subjectId}/icon", method = RequestMethod.GET)
    public void getSubjectsIcon(@PathVariable(value = "subjectId") Long id, HttpServletResponse response) {
        byte[] file;
        try {
            file = subjectsService.getSubjectsIcon(id);
        } catch (NotFound notFound) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            return;
        } catch (IOException|StorageException|URISyntaxException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("image/jpeg");

        InputStream is = new ByteArrayInputStream(file);
        try {
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity addSubject(@RequestBody Subject subject) {
        subjectsService.addSubject(subject);

        return new ResponseEntity(HttpStatus.CREATED);
    }
}
