package com.reshigo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reshigo.notifications.String2Base64Converter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * Created by dmitry103 on 03/01/2018.
 */

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "hide", nullable = false, columnDefinition = "BIT default 0")
    private Boolean hide = false;

    @JsonIgnoreProperties({ "pictures", "chat", "status", "theme", "date", "maturityDate", "comment", "user", "solver", "price", "tasksCnt", "review" })
    @OneToOne(mappedBy = "review", fetch = FetchType.EAGER)
    private Order order;

    @Max(5)
    @Min(1)
    @Column(name = "rating")
    private Long rating;

    @Size(max = 2000)
    @Column(name = "comment")
    @Convert(converter = String2Base64Converter.class)
    private String comment;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getHide() {
        return hide;
    }

    public void setHide(Boolean hide) {
        this.hide = hide;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
