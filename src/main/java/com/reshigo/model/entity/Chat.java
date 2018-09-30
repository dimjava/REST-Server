package com.reshigo.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reshigo.model.entity.permanent.ChatStatusEnum;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

import static org.hibernate.annotations.CascadeType.SAVE_UPDATE;

/**
 * Created by dmitry103 on 17/07/16.
 */

@Entity
@Table(name = "chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ChatStatusEnum status;

    @JsonIgnoreProperties({ "pictures", "chat", "status", "theme", "date", "maturityDate", "comment" })
    @OneToOne(mappedBy = "chat", fetch = FetchType.LAZY)
    private Order order;

    @JsonIgnore
    @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Message> messages = new LinkedList<>();

    @JsonIgnore
    @ManyToMany(targetEntity = User.class, fetch = FetchType.LAZY)
    @Cascade(SAVE_UPDATE)
    private List<User> participants = new LinkedList<>();

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public ChatStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ChatStatusEnum status) {
        this.status = status;
    }
}
