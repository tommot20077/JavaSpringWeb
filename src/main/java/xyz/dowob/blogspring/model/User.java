package xyz.dowob.blogspring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;

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

    @Column(name= "email_active_status", nullable = false, columnDefinition = "boolean default false")
    private boolean emailIsActive;

    @Column(nullable = false, updatable = false, name = "register_time", insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date register_time;


    @Column(nullable = false, columnDefinition = "int default 0")
    private int currentSendTimes;

    @Column(nullable = false)
    private LocalDateTime resetTime = LocalDateTime.now().minusHours(2);

    @Getter
    @Setter
    @Column(nullable = false, columnDefinition = "varchar(100) default 'Etc/UTC'")
    private String timezone = "Etc/UTC";

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

    public Date getRegister_time() { return register_time; }

    public boolean getEmailActiveStatus() {
        return emailIsActive;
    }
    public void setEmailActiveStatus(boolean emailIsActive) {
        this.emailIsActive = emailIsActive;
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
