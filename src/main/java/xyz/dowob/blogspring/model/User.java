package xyz.dowob.blogspring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Post> posts;


    @Column(nullable = false,unique = true)
    @NotBlank(message = "使用名稱為必填選項")
    private String username;

    @Column(nullable =false)
    @NotBlank(message = "密碼為必填選項")
    private String password;

    @Column
    @Email(message = "請輸入有效的E-mail")
    private String email;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthdate;

    @Column(name= "active", nullable = false)
    private boolean isActive;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int currentSendTimes;

    private LocalDateTime resetTime = LocalDateTime.now().minusHours(2);;

    public User() {
        this.isActive = false;
    }


    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password_hash) {password = password_hash;}
   
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Date getBirthdate() {
        return birthdate;
    }
    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public int getCurrentSendTimes() {
        return currentSendTimes;
    }
    public void setCurrentSendTimes(int currentSendTimes) {
        this.currentSendTimes = currentSendTimes;
    }

    public LocalDateTime getResetTime() {
        return resetTime;
    }
    public void setResetTime(LocalDateTime resetTime) {
        this.resetTime = resetTime;
    }
}
