package com.reshigo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.test.context.ContextHierarchy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Timestamp;

/**
 * Created by dmitry103 on 17/07/16.
 */

@Entity
@Table(name = "message")
public class Message {
    public static long DEFAULT_WIDTH = 320;
    public static long DEFAULT_HEIGHT = 480;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @JsonIgnore
    @ManyToOne(targetEntity = Chat.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @JsonIgnoreProperties({ "gcmInstallationId", "apnsInstallationId", "email", "phone", "funds",
            "reservedFunds", "payments", "registrationDate", "enabled", "ordersLimit", "promocode",
            "education", "info", "degree", "lastVisit", "rating", "isCustomer", "iosReview", "androidReview" })
    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_name", nullable = false)
    private User user;

    @Column(name = "date", nullable = false)
    private Timestamp date;

    @JsonIgnore
    @Column(name = "path")
    private String path;

    @NotNull
    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "width", nullable = false)
    private Long width = DEFAULT_WIDTH;

    @Column(name = "height", nullable = false)
    private Long height = DEFAULT_HEIGHT;

    @NotNull
    @JsonIgnoreProperties(value = {"title"})
    @ManyToOne(targetEntity = MessageType.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "message_type_id")
    private MessageType messageType;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public Message cloneNoData() throws IllegalAccessException {
        Message ret = new Message();

        for (Field f: Message.class.getDeclaredFields()) {
            if (f.getName().equals("data")) {
                continue;
            }

            f.set(ret, f.get(this));
        }

        return ret;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }
}
