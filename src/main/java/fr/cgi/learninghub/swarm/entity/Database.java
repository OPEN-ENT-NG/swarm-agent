package fr.cgi.learninghub.swarm.entity;

import jakarta.persistence.*;

@Entity
public class Database {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column
    private Integer port;

    @Column
    private String host;

    @Column
    private String username;

    @Column
    private String password;

    @Column(name = "default_db")
    private String defaultDB;

    @Column(columnDefinition = "boolean default true")
    private Boolean enabled;

    @Column(columnDefinition = "boolean default false")
    private Boolean ssl;

    @Column(name = "ca_cert", columnDefinition = "Text")
    private String caCert;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPort() {
        return port;
    }

    public Database setPort(Integer port) {
        this.port = port;
        return this;
    }

    public String getHost() {
        return host;
    }

    public Database setHost(String host) {
        this.host = host;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Database setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Database setPassword(String password) {
        this.password = password;
        return this;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Database setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getDefaultDB() {
        return defaultDB;
    }

    public Database setDefaultDB(String defaultDB) {
        this.defaultDB = defaultDB;
        return this;
    }

    public String getCaCert() {
        return caCert;
    }

    public Database setCaCert(String caCert) {
        this.caCert = caCert;
        return this;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public Database setSsl(Boolean ssl) {
        this.ssl = ssl;
        return this;
    }
}
