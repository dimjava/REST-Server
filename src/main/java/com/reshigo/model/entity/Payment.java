package com.reshigo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.reshigo.notifications.String2Base64Converter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Created by dmitry103 on 23/11/16.
 */

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "bonus_amount", nullable = false)
    private BigDecimal bonusAmount;

    @Column(name = "completed")
    private boolean completed = false;

    @Column(name = "date", nullable = false)
    private Timestamp date;

    @Column(name = "commission", nullable = false, columnDefinition = "decimal(5,2) default 0")
    private BigDecimal commission;

    @Column(name = "comment")
    @Convert(converter = String2Base64Converter.class)
    private String comment;

    @JsonIgnoreProperties({"email", "phone", "payments", "funds", "reservedFunds", "promocode", "enabled", "ordersLimit",
            "education", "degree", "info", "isCustomer", "registrationDate", "lastVisit", "rating", "iosReview", "androidReview" })
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_name")
    private User user;

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public BigDecimal getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(BigDecimal bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
