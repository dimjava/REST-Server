package com.reshigo.controller.permanent;

import com.reshigo.model.entity.Theme;
import com.reshigo.service.permanent.ThemesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by dmitry103 on 11/07/16.
 */

@RestController
@RequestMapping(value = "/themes")
public class ThemesController {
    @Autowired
    private ThemesService themesService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity< List<Theme> > getThemes() {
        return new ResponseEntity<>(themesService.getAllThemes(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity addTheme(@RequestBody Theme theme) {
        themesService.addTheme(theme);

        return new ResponseEntity(HttpStatus.CREATED);
    }
}
