package com.reshigo.model.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by dmitry103 on 01/01/2018.
 */

@Entity
@Table(name = "topic")
public class Topic implements Serializable {
    @Id
    @Column(name = "name", nullable = false)
    private String name;

    @Id
    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_name")
    private User user;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
