package com.reshigo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "themes")
public class Theme implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;

    @ManyToOne(targetEntity = Subject.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "theme", nullable = false, unique = true)
    private String theme;

    @Column(name = "theme_ru", nullable = false)
    private byte[] themeRU;

    @JsonIgnore
    @OneToMany(mappedBy = "theme", fetch = FetchType.LAZY)
    private List<Order> orders;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public byte[] getThemeRU() {
        return themeRU;
    }

    public void setThemeRU(byte[] themeRU) {
        this.themeRU = themeRU;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
