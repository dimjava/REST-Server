package com.reshigo.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.reshigo.model.HttpResponseEntity;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "orders")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order extends HttpResponseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @JsonIgnoreProperties({"email", "phone", "payments", "funds", "reservedFunds", "promocode", "enabled", "ordersLimit", "iosReview", "androidReview"})
    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_name", nullable = false)
    private User user;

    @JsonIgnoreProperties({"email", "phone", "payments", "funds", "reservedFunds", "promocode", "enabled", "ordersLimit", "iosReview", "androidReview"})
    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "solvername")
    private User solver;

    @JsonIgnoreProperties({ "img" })
    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
    @Size(max = 3)
    private List<Picture> pictures = new LinkedList<>();

    @JsonIgnoreProperties({ "order" })
    @OneToOne(targetEntity = Chat.class, fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @JsonIgnoreProperties({"order"})
    @OneToOne(targetEntity = Review.class, fetch = FetchType.LAZY)
    @Cascade(CascadeType.ALL)
    @JoinColumn(name = "review_id")
    private Review review;

    @JsonIgnore
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @Cascade(CascadeType.ALL)
    private List<PriceSuggest> suggests;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatusEnum status;

    @Min(value = 0L)
    @NotNull
    @Column(name = "price")
    private Long price;

    @Column(name = "tasks_cnt")
    private Long tasksCnt;

    @NotNull
    @ManyToOne(targetEntity = Theme.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @NotNull
//    @Past
    @Column(name = "date", nullable = false)
    private Timestamp date;

    @NotNull
//    @Future
    @Column(name = "maturity_date", nullable = false)
    private Timestamp maturityDate;

    @Size(max = 1600)
    @Column(name = "comment")
    private byte[] comment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getSolver() {
        return solver;
    }

    public void setSolver(User solver) {
        this.solver = solver;
    }

    public List<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(List<Picture> pictures) {
        this.pictures = pictures;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public Timestamp getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(Timestamp maturityDate) {
        this.maturityDate = maturityDate;
    }

    public byte[] getComment() {
        return comment;
    }

    public void setComment(byte[] comment) {
        this.comment = comment;
    }

    public OrderStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public List<PriceSuggest> getSuggests() {
        return suggests;
    }

    public void setSuggests(List<PriceSuggest> suggests) {
        this.suggests = suggests;
    }

    public Long getTasksCnt() {
        return tasksCnt;
    }

    public void setTasksCnt(Long tasksCnt) {
        this.tasksCnt = tasksCnt;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }
}
