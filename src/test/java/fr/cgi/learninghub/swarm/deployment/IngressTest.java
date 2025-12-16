package fr.cgi.learninghub.swarm.deployment;

import fr.cgi.learning.hub.swarm.common.entities.Service;
import fr.cgi.learning.hub.swarm.common.enums.State;
import fr.cgi.learning.hub.swarm.common.enums.Type;
import fr.cgi.learninghub.swarm.cluster.NginxIngress;
import fr.cgi.learninghub.swarm.entity.Deployment;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@QuarkusTest
public class IngressTest {

    public static final String SERVICE_ID = "0fccdff1-dbc2-47fd-96bd-27236be9c43f";

    public static final String CLUSTER_CONFIGURATION_INGRESS_NAME = "swarm-ingress";
    public static final String CLUSTER_CONFIGURATION_INGRESS_API = "networking.k8s.io/v1";
    public static final String CLUSTER_CONFIGURATION_INGRESS_KIND = "Ingress";

    public static final String CLUSTER_CONFIGURATION_PUBLIC_HOSTNAME = "swarm.support-ent.fr";
    public static final String CLUSTER_CONFIGURATION_TLS_SECRET = "support-ent-tls";
    public static final String SERVICE_NAME = "wp-doe";
    public static final String SERVICE_PATH = "/wp-" + SERVICE_ID;
    private static final String CREATION_DATE = "15/09/2024 14:23:31";
    private static final String DELETION_DATE = "01/10/2024 00:00:00";
    private static NginxIngress ingress;

    @BeforeAll
    public static void setUp() throws ParseException {
        ingress = new NginxIngress(initDeployments(), CLUSTER_CONFIGURATION_INGRESS_NAME)
                .setPublicHostName(CLUSTER_CONFIGURATION_PUBLIC_HOSTNAME)
                .setTlsSecret(CLUSTER_CONFIGURATION_TLS_SECRET);

        // Debug mode via test
        // System.out.println(Serialization.asYaml(ingress.get()));
    }

    public static List<Deployment> initDeployments() throws ParseException {
        List<Deployment> deployments = new ArrayList<>();

        Service service1 = new Service()
                .setId(SERVICE_ID)
                .setServiceName(SERVICE_NAME)
                .setState(State.SCHEDULED)
                .setFirstName("John")
                .setLastName("DOE")
                .setCreated(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(CREATION_DATE))
                .setDeletionDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(DELETION_DATE))
                .setStructureId("a74eb656-42e3-4b22-a841-d5dc62f93c4a")
                .setType(Type.WORDPRESS);
        Deployment deployment1 = new Deployment()
                .setService(service1)
                .setPath(SERVICE_PATH);

        Service service2 = new Service()
                .setId(SERVICE_ID)
                .setServiceName(SERVICE_NAME)
                .setState(State.SCHEDULED)
                .setFirstName("John")
                .setLastName("DOE")
                .setCreated(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(CREATION_DATE))
                .setDeletionDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(DELETION_DATE))
                .setStructureId("a74eb656-42e3-4b22-a841-d5dc62f93c4a")
                .setType(Type.WORDPRESS);
        Deployment deployment2 = new Deployment()
                .setService(service2)
                .setPath(SERVICE_PATH);

        deployments.add(deployment1);
        deployments.add(deployment2);

        return deployments;
    }
    @Test
    @DisplayName("CRD apiVersion should be correct meaning : " + CLUSTER_CONFIGURATION_INGRESS_API)
    void testAPIVersion() {
        var crd = ingress.get();
        Assertions.assertEquals(CLUSTER_CONFIGURATION_INGRESS_API, crd.getApiVersion());
    }

    @Test
    @DisplayName("CRD kind should be correct meaning : " + CLUSTER_CONFIGURATION_INGRESS_KIND)
    void testKind() {
        var crd = ingress.get();
        Assertions.assertEquals(CLUSTER_CONFIGURATION_INGRESS_KIND, crd.getKind());
    }
//
//    @Test
//    @DisplayName("CRD metadata should contains name, labels, annotations and its right data")
//    public void testMetadata() {
//        var crd = ingress.getCRD();
//        var metadata = crd.getMetadata();
//
//        // name field
//        Assertions.assertEquals(METADATA_NAME, metadata.getName());
//
//        // labels field
//        var labels = metadata.getLabels();
//        Assertions.assertEquals(METADATA_LABELS_APP_VALUE, labels.get(METADATA_LABELS_APP));
//
//        // annotations field
//        var annotations = metadata.getAnnotations();
//        Assertions.assertEquals(METADATA_ANNOTATIONS_INGRESS_CLASS_VALUE, annotations.get(METADATA_ANNOTATIONS_INGRESS_CLASS));
//    }
//
//    @Test
//    @DisplayName("CRD spec should contains a ingressClassName with the right data")
//    void testIngressSiteSpec() {
//        var crd = ingress.getCRD();
//        var spec = crd.getSpec();
//
//        // Check that spec contains ingressClassName key
//        Assertions.assertTrue(spec.getAdditionalProperties().containsKey(BaseDeployment.IngressSpecFields.SPEC_NAME));
//
//        // Check that ingressClassName object contains value
//        var ingressSpec = spec.getAdditionalProperties().get(BaseDeployment.IngressSpecFields.SPEC_NAME);
//        Assertions.assertEquals(INGRESS_CLASS_NAME, ingressSpec);
//    }
//
//    @Test
//    @DisplayName("CRD spec should contains a Tls field in spec with the right data")
//    void testTlsSiteSpec() {
//        var crd = ingress.getCRD();
//        var spec = crd.getSpec();
//
//        // Check that spec contains tls key
//        Assertions.assertTrue(spec.getAdditionalProperties().containsKey(BaseDeployment.TlsSpecFields.SPEC_NAME));
//
//        // Check that tls object contains value
//        var tlsSpec = (Map<String, Object>) spec.getAdditionalProperties().get(BaseDeployment.TlsSpecFields.SPEC_NAME);
//
//        // SecretName TLS Check
//        Assertions.assertEquals(TLS_SPEC_LOCAL_TLS, tlsSpec.get(BaseDeployment.TlsSpecFields.SECRET_NAME));
//
//        // Hosts TLS Check
//        var hosts = (List<String>) tlsSpec.get(BaseDeployment.TlsSpecFields.HOSTS);
//        Assertions.assertEquals("swarm-dev.support-ent.fr", hosts.getFirst());
//    }
//
//    @Test
//    @DisplayName("CRD spec should contains a rules field in spec with 1 object where we have host and http")
//    void testGlobalRulesSiteSpec() {
//        var crd = ingress.getCRD();
//        var spec = crd.getSpec();
//
//        // Check that spec contains rules field
//        Assertions.assertTrue(spec.getAdditionalProperties().containsKey(BaseDeployment.RulesSpecFields.SPEC_NAME));
//
//        var rules = (List<Map<String, Object>>) spec.getAdditionalProperties().get(BaseDeployment.RulesSpecFields.SPEC_NAME);
//        var globalRule = (Map<String, Object>) rules.getFirst();
//
//        // Check that inside rules "global" that contains host
//        Assertions.assertEquals(HOST_VALUE, globalRule.get(BaseDeployment.RulesSpecFields.HOST));
//
//        // Check that inside rules contains http
//        Assertions.assertTrue(globalRule.containsKey(BaseDeployment.RulesSpecFields.HTTP));
//    }
}
