package org.artyomcool.ravenproxy.data;

public class SentryUser {

    private String id;
    private String username;
    private String email;
    private String ip_address;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIpAddress(String ipAddress) {
        this.ip_address = ipAddress;
    }
}
