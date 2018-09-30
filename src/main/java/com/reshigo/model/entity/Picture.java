package com.reshigo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "pictures")
public class Picture implements Serializable {
    @NotNull
    @Id
    @GeneratedValue(generator="gen")
    @GenericGenerator(name="gen", strategy="foreign", parameters = {@Parameter(name = "property", value = "img")})
    @Column(name = "id")
    private Long id;

    @JsonIgnore
    @ManyToOne(targetEntity = Order.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    //for sorting pictures, usually is a simple unix-timestamp
    @NotNull
    @Column(name = "counter", nullable = false)
    private Long counter;

    @NotNull
    @OneToOne(cascade = javax.persistence.CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private PictureData img;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public PictureData getImg() {
        return img;
    }

    public void setImg(PictureData img) {
        this.img = img;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getCounter() {
        return counter;
    }

    public void setCounter(Long counter) {
        this.counter = counter;
    }
}
