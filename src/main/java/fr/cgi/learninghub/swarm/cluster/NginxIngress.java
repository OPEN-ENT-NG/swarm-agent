package fr.cgi.learninghub.swarm.cluster;


import fr.cgi.learning.hub.swarm.common.enums.State;
import fr.cgi.learninghub.swarm.deployment.DeploymentFactory;
import fr.cgi.learninghub.swarm.entity.Deployment;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.*;

import java.util.List;

public class NginxIngress {

    public static final String API_VERSION = "networking.k8s.io/v1";
    public static final String METADATA_LABELS_APP = "app.kubernetes.io/managed-by";
    public static final String METADATA_LABELS_APP_VALUE = "agent";
    public static final String METADATA_ANNOTATIONS_INGRESS_CLASS = "kubernetes.io/ingress.class";
    public static final String METADATA_ANNOTATIONS_SERVER_SNIPPETS = "nginx.ingress.kubernetes.io/server-snippet";
    public static final String DEFAULT_INGRESS_CLASS = "nginx";
    private final List<Deployment> deployments;

    private String publicHostName;
    private String tlsSecret;
    private String ingressOauth2ProxyAuthUrl;
    private String ingressOauth2ProxyAuthSignInUrl;
    private String ingressClassName = DEFAULT_INGRESS_CLASS;

    private String ingressName;

    public NginxIngress(List<Deployment> deployments, String ingressName) {
        this.deployments = deployments;
        this.ingressName = ingressName;
    }

    public Ingress get() {
        return new IngressBuilder()
                .withApiVersion(API_VERSION)
                .withMetadata(getMetadata())
                .withSpec(getSpec())
                .build();
    }

    private List<HTTPIngressPath> getPaths() {
        return deployments.stream().map(deployment ->
                new HTTPIngressPathBuilder()
                        .withPath(deployment.getPath())
                        .withPathType("Prefix")
                        .withBackend(this.getBackend(deployment))
                        .build()
        ).toList();
    }

    private IngressBackend getBackend(Deployment deployment) {
        var port = new ServiceBackendPortBuilder()
                .withNumber(80)
                .build();

        return new IngressBackendBuilder()
                .withNewService()
                .withName(deployment.getService().getServiceName())
                .withPort(port)
                .endService()
                .build();
    }

    private HTTPIngressRuleValue getHttpRule() {
        return new HTTPIngressRuleValueBuilder()
                .withPaths(this.getPaths())
                .build();
    }

    private List<IngressRule> getRules() {
        return List.of(
                new IngressRuleBuilder()
                        .withHost(publicHostName)
                        .withHttp(this.getHttpRule())
                        .build()
        );
    }


    private ObjectMeta getMetadata() {
        var disabledDeployments = deployments.stream()
                .filter(deployment -> State.DISABLED.equals(deployment.getService().getState()))
                .toList();

        var metadataBuilder = new ObjectMetaBuilder()
                .withName(ingressName)
                .addToLabels(METADATA_LABELS_APP, METADATA_LABELS_APP_VALUE)
                .addToAnnotations(METADATA_ANNOTATIONS_INGRESS_CLASS, ingressClassName);

        if (!disabledDeployments.isEmpty()) {
            StringBuilder snippet = new StringBuilder();
            disabledDeployments.forEach(deployment -> {
                snippet.append(String.format("""
                        location %s/%s {
                            return 403;
                        }
                        """, deployment.getPath(), DeploymentFactory.getAdminPathPrefix(deployment.getService().getType())));
            });

            metadataBuilder.addToAnnotations(METADATA_ANNOTATIONS_SERVER_SNIPPETS, snippet.toString());
        }

        return metadataBuilder.build();
    }

    private IngressTLS getTls() {
        return new IngressTLSBuilder()
                .withHosts(getPublicHostName())
                .withSecretName(getTlsSecret())
                .build();
    }

    private IngressSpec getSpec() {
        return new IngressSpecBuilder()
                .withIngressClassName(ingressClassName)
                .withTls(getTls())
                .withRules(getRules())
                .build();
    }

    public String getPublicHostName() {
        return publicHostName;
    }

    public NginxIngress setPublicHostName(String publicHostName) {
        this.publicHostName = publicHostName;
        return this;
    }

    public String getTlsSecret() {
        return tlsSecret;
    }

    public NginxIngress setTlsSecret(String tlsSecret) {
        this.tlsSecret = tlsSecret;
        return this;
    }

    public String getIngressName() {
        return ingressName;
    }

    public NginxIngress setIngressName(String ingressName) {
        this.ingressName = ingressName;
        return this;
    }

    public String getIngressClassName() {
        return ingressClassName;
    }

    public NginxIngress setIngressClassName(String ingressClassName) {
        this.ingressClassName = ingressClassName;
        return this;
    }
}
