package com.reshigo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reshigo.notifications.Notification;
import com.reshigo.notifications.String2Base64Converter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by dmitry103 on 27/06/17.
 */

@Entity
@Table(name = "feed")
public class Feed {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "notification", columnDefinition = "text")
    @Convert(converter = String2Base64Converter.class)
    private String notification;

    @Column(name = "date")
    private Timestamp date;

    @JsonIgnoreProperties({"email", "phone", "payments", "funds", "reservedFunds", "promocode"})
    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_name", nullable = false)
    private User user;

    @Column(name = "show")
    private boolean show;

    @Column(name = "echo")
    private boolean echo;

    public Feed() {}

    public Feed(Notification n, User user, boolean echo) throws Exception {
        setDate(new Timestamp(new Date().getTime()));
        setId(0L);
        setUser(user);
        setNotification(n.convert());
        setShow(n.getShow());
        setEcho(echo);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean getShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public boolean isEcho() {
        return echo;
    }

    public void setEcho(boolean echo) {
        this.echo = echo;
    }
}
