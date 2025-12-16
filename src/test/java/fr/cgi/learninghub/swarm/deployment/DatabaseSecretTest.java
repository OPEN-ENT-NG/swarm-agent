package fr.cgi.learninghub.swarm.deployment;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DatabaseSecretTest {

    private static final String SECRET_NAME = "db-secret-wp-123456";
    private DatabaseSecret dbSecret;

    @BeforeEach
    public void setup() {
        dbSecret = new DatabaseSecret("wp-123456", "password");
    }

    @Test
    @DisplayName("creating a DatabaseSecret should create a valid base64 secret")
    public void testBase64DatabaseSecret() {
        Assertions.assertEquals("cGFzc3dvcmQ=", dbSecret.getSecret().getData().get("password"));
    }

    @Test
    @DisplayName("adding a name to a DatabaseSecret should set it in metadata")
    public void testSetName() {
        Assertions.assertEquals(SECRET_NAME, dbSecret.getSecret().getMetadata().getName());
    }
}
