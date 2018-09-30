package com.reshigo.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.reshigo.model.HttpResponseEntity;
import com.reshigo.notifications.String2Base64Converter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import static javax.persistence.CascadeType.REMOVE;

@Entity
@Table(name = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends HttpResponseEntity {
    @Id
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "orders_limit", nullable = false, columnDefinition = "int default 2")
    private Long ordersLimit = 3L;

    @JsonIgnore
    @Column(name = "sms_cnt", nullable = false, columnDefinition = "int default 0")
    private Long smsCnt = 0L;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = REMOVE)
    @Cascade(CascadeType.SAVE_UPDATE)
    private List<Authorities> authorities = new LinkedList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = REMOVE)
    @Cascade(CascadeType.SAVE_UPDATE)
    private List<Topic> topics = new LinkedList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Cascade(CascadeType.SAVE_UPDATE)
    private List<Message> messages;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Order> orders = new LinkedList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Cascade(CascadeType.ALL)
    private List<Payment> payments;

    @JsonIgnore
    @OneToMany(mappedBy = "solver", fetch = FetchType.LAZY)
    private List<PriceSuggest> suggests;

    @JsonIgnore
    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    private List<Chat> chats = new LinkedList<>();

    @JsonIgnore
    @Column(name = "fcm")
    private String fcmToken;

    @Column(name = "is_customer", nullable = false)
    private boolean isCustomer = true;

    @Column(name = "funds", precision = 8, scale = 2, nullable = false)
    private BigDecimal funds = BigDecimal.ZERO;

    @Column(name = "reserved_funds", precision = 8, scale = 2, nullable = false)
    private BigDecimal reservedFunds = BigDecimal.ZERO;

    @JsonIgnore
    @Column(name = "bonus_funds", precision = 8, scale = 2, nullable = false)
    private BigDecimal bonusFunds = BigDecimal.ZERO;

    @Column(name = "promocode")
    private String promocode;

    @JsonIgnore
    @Column(name = "used_promo")
    private String usedPromo;

    @JsonIgnore
    @Column(name = "code")
    private Long code;

    @JsonIgnore
    @Column(name = "attempts", nullable = false)
    private Long attempts = 0L;

    @Column(name = "registration_date")
    private Timestamp registrationDate;

    @Column(name = "last_visit")
    private Timestamp lastVisit;

    @Column(name = "rating")
    private Double rating;

    @JsonIgnore
    @Column(name = "feed_load_time")
    private Timestamp feedLoadTime;

    @Size(max = 2000)
    @Column(name = "education")
    @Convert(converter = String2Base64Converter.class)
    private String education;

    @Column(name = "degree")
    @Size(max = 2000)
    @Convert(converter =  String2Base64Converter.class)
    private String degree;

    @Column(name = "info")
    @Size(max = 2000)
    @Convert(converter = String2Base64Converter.class)
    private String info;

    @JsonIgnore
    @Column(name = "photo")
    private String photo;

    @JsonIgnore
    @Column(name = "commission", nullable = false, columnDefinition = "decimal(5,2) default 0")
    private BigDecimal commission;

    // Field to know the source from where this user came
    @Column(name = "campaign")
    private String campaign;

    @Column(name = "iosReview", nullable = false, columnDefinition = "BIT default 0")
    private boolean iosReview;

    @Column(name = "androidReview", nullable = false, columnDefinition = "BIT default 0")
    private boolean androidReview;

    public Timestamp getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Timestamp registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Timestamp getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(Timestamp lastVisit) {
        this.lastVisit = lastVisit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Authorities> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<Authorities> authorities) {
        this.authorities = authorities;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<Chat> getChats() {
        return chats;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
    }

    public boolean getIsCustomer() {
        return isCustomer;
    }

    public void setIsCustomer(boolean customer) {
        isCustomer = customer;
    }

    public String getPromocode() {
        return promocode;
    }

    public void setPromocode(String promocode) {
        this.promocode = promocode;
    }

    public BigDecimal getFunds() {
        return funds;
    }

    public void setFunds(BigDecimal funds) {
        this.funds = funds;
    }

    public String getUsedPromo() {
        return usedPromo;
    }

    public void setUsedPromo(String usedPromo) {
        this.usedPromo = usedPromo;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public BigDecimal getReservedFunds() {
        return reservedFunds;
    }

    public void setReservedFunds(BigDecimal reservedFunds) {
        this.reservedFunds = reservedFunds;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public Long getAttempts() {
        return attempts;
    }

    public void setAttempts(Long attempts) {
        this.attempts = attempts;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Long getSmsCnt() {
        return smsCnt;
    }

    public void setSmsCnt(Long smsCnt) {
        this.smsCnt = smsCnt;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Long getOrdersLimit() {
        return ordersLimit;
    }

    public void setOrdersLimit(Long ordersLimit) {
        this.ordersLimit = ordersLimit;
    }

    public Timestamp getFeedLoadTime() {
        return feedLoadTime;
    }

    public void setFeedLoadTime(Timestamp feedLoadTime) {
        this.feedLoadTime = feedLoadTime;
    }

    public List<PriceSuggest> getSuggests() {
        return suggests;
    }

    public void setSuggests(List<PriceSuggest> suggests) {
        this.suggests = suggests;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public BigDecimal getBonusFunds() {
        return bonusFunds;
    }

    public void setBonusFunds(BigDecimal bonusFunds) {
        this.bonusFunds = bonusFunds;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public boolean isAndroidReview() {
        return androidReview;
    }

    public void setAndroidReview(boolean androidReview) {
        this.androidReview = androidReview;
    }

    public boolean isIosReview() {
        return iosReview;
    }

    public void setIosReview(boolean iosReview) {
        this.iosReview = iosReview;
    }
}
