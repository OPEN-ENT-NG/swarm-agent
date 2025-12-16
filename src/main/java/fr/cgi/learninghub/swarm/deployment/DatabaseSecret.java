package fr.cgi.learninghub.swarm.deployment;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;

import java.util.*;

public class DatabaseSecret {

    public static final String PASSWORD = "password";

    private final Map<String, String> data = new HashMap<>();
    private String name;
    private OwnerReference ownerReference;

    public DatabaseSecret(String name, String value) {
        this.setName(name);
        var encoded = Base64.getEncoder().encodeToString(value.getBytes());
        this.data.put(PASSWORD, encoded);
    }

    public DatabaseSecret setName(String name) {
        this.name = String.format("db-secret-%s", name);
        return this;
    }

    public String name() {
        return this.name;
    }

    public Secret getSecret() {
        return new SecretBuilder()
                .withMetadata(metadata())
                .withData(data)
                .build();
    }

    private ObjectMeta metadata() {
        var metadata = new ObjectMeta();
        metadata.setName(name);

        if (!Objects.isNull(ownerReference)) {
            metadata.setOwnerReferences(Collections.singletonList(ownerReference));
        }

        return metadata;
    }


    public DatabaseSecret setOwnerReference(OwnerReference ownerReference) {
        this.ownerReference = ownerReference;
        return this;
    }
}
