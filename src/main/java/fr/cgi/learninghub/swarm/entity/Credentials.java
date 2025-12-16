package fr.cgi.learninghub.swarm.entity;

import jakarta.persistence.*;

import java.util.Arrays;

@Entity
public class Credentials {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    @Column(
            name = "id"
    )
    private String id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private CredentialType type;

    @OneToOne
    @JoinColumn(name = "deployment_id")
    private Deployment deployment;

    public String getRole() {
        return role;
    }

    public Credentials setRole(String role) {
        this.role = role;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Credentials setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Credentials setUsername(String username) {
        this.username = username;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public Credentials setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public Credentials setDeployment(Deployment deployment) {
        this.deployment = deployment;
        return this;
    }

    public CredentialType getType() {
        return type;
    }

    public Credentials setType(CredentialType type) {
        this.type = type;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Credentials{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", type=" + type +
                ", deployment=" + deployment +
                '}';
    }

    public enum CredentialType {
        ROLE("ROLE"),
        ADMIN_CREDENTIAL("ADMIN_CREDENTIAL");

        private final String value;

        private CredentialType(String value) {
            this.value = value;
        }

        public static CredentialType getCredentialType(String value) {
            return Arrays.stream(values()).filter((type) -> type.getValue().equals(value)).findFirst().orElse(null);
        }

        public String getValue() {
            return this.value;
        }
    }
}
