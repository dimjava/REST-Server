package com.reshigo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reshigo.notifications.String2Base64Converter;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Created by dmitry103 on 25/08/17.
 */
@Entity
@Table(name = "price_suggestions")
public class PriceSuggest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "accepted", columnDefinition = "BIT default 0")
    private boolean accepted = false;

    @Column(name = "price")
    private Long price;

    @Size(max = 2000)
    @Column(name = "comment")
    @Convert(converter = String2Base64Converter.class)
    private String comment;

    @JsonIgnoreProperties({"email", "phone", "payments", "funds", "reservedFunds", "promocode"})
    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "solver")
    private User solver;

    @JsonIgnoreProperties({"user", "solver", "pictures", "chat", "status",
            "price", "theme", "date", "comment", "maturityDate"})
    @ManyToOne(targetEntity = Order.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public User getSolver() {
        return solver;
    }

    public void setSolver(User solver) {
        this.solver = solver;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
