package com.reshigo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by dmitry103 on 16/10/16.
 */

@Entity
@Table(name = "pictures_data")
public class PictureData implements Serializable {
    @Id
    @Column(name="picture_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long pictureId;

    @JsonIgnore
    @OneToOne(mappedBy = "img", fetch = FetchType.LAZY, optional = false)
    private Picture picture;

    @JsonIgnore
    @Column(name = "path", nullable = true)
    private String path;

    @Transient
    private byte[] data;

    public Picture getPicture() {
        return picture;
    }

    public void setPicture(Picture picture) {
        this.picture = picture;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Long getPictureId() {
        return pictureId;
    }

    public void setPictureId(Long pictureId) {
        this.pictureId = pictureId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
