package xyz.dowob.blogspring.model;

import jakarta.persistence.*;

@Entity
@Table(name = "api_token")
public class ApiToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "request_count")
    private int requestCount;
    @Column(name = "last_request_time", columnDefinition = "bigint", nullable = false)
    private Long lastRequestTime;

    public ApiToken(String ip) {
        ipAddress = ip;
        requestCount = 0;
        lastRequestTime = System.currentTimeMillis();
    }

    public ApiToken() {
    }


    public String getIp() {
        return ipAddress;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void incrementRequestCount() {
        requestCount++;
    }

    public long getLastResetTime() {
        return lastRequestTime;
    }

    public void resetRequestCount() {
        requestCount=0;
        lastRequestTime = System.currentTimeMillis();
    }
}
