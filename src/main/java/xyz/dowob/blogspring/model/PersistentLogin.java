package xyz.dowob.blogspring.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PersistentLogin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;



    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }
    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public void createOrUpdateUserRememberMeToken(String token, int expireTimeDays) {
        setToken(token);
        setExpiryTime(LocalDateTime.now().plusDays(expireTimeDays));
    }
}
