package fr.cgi.learninghub.swarm.cluster;

import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class ClusterConfiguration {

    @ConfigProperty(name = "swarm.agent.kubernetes.namespace")
    private String k8sNamespace;

    @ConfigProperty(name = "swarm.agent.public.service.hostname")
    private String servicePublicHostname;

    @ConfigProperty(name = "swarm.agent.prestashop.email.hostname")
    private String psEmailHostname;

    @ConfigProperty(name = "swarm.agent.wordpress.email.hostname")
    private String wpEmailHostname;

    @ConfigProperty(name = "swarm.agent.kubernetes.tls.secret.name")
    private String tlsSecretName;

    @ConfigProperty(name = "swarm.agent.kubernetes.cluster.ip")
    private String clusterIp;

    @ConfigProperty(name = "swarm.agent.kubernetes.ingress.name", defaultValue = "swarm-ingress")
    private String ingressName;

    @ConfigProperty(name = "swarm.agent.kubernetes.ingress.class.name", defaultValue = "nginx")
    private String ingressClassName;

    // Configuration pour HTTPRoute (Envoy Gateway) - ACTIVÉ PAR DÉFAUT
    @ConfigProperty(name = "swarm.agent.kubernetes.use.httproute", defaultValue = "true")
    private boolean useHTTPRoute;

    @ConfigProperty(name = "swarm.agent.kubernetes.envoy.gateway.name", defaultValue = "public-gateway")
    private String envoyGatewayName;

    @ConfigProperty(name = "swarm.agent.kubernetes.envoy.gateway.namespace", defaultValue = "envoy")
    private String envoyGatewayNamespace;

    @ConfigProperty(name = "swarm.agent.kubernetes.envoy.gateway.section", defaultValue = "https")
    private String envoyGatewaySectionName;

    public String getK8sNamespace() {
        return k8sNamespace;
    }

    public ClusterConfiguration setK8sNamespace(String k8sNamespace) {
        this.k8sNamespace = k8sNamespace;
        return this;
    }

    public String getServicePublicHostname() {
        return servicePublicHostname;
    }

    public ClusterConfiguration setServicePublicHostname(String servicePublicHostname) {
        this.servicePublicHostname = servicePublicHostname;
        return this;
    }

    public String getTlsSecretName() {
        return tlsSecretName;
    }

    public ClusterConfiguration setTlsSecretName(String tlsSecretName) {
        this.tlsSecretName = tlsSecretName;
        return this;
    }

    public String getClusterIp() {
        return clusterIp;
    }

    public ClusterConfiguration setClusterIp(String clusterIp) {
        this.clusterIp = clusterIp;
        return this;
    }

    public String getIngressName() {
        return ingressName;
    }

    public ClusterConfiguration setIngressName(String ingressName) {
        this.ingressName = ingressName;
        return this;
    }

    public String getIngressClassName() {
        return ingressClassName;
    }

    public ClusterConfiguration setIngressClassName(String ingressClassName) {
        this.ingressClassName = ingressClassName;
        return this;
    }

    public String getPsEmailHostname() {
        return psEmailHostname;
    }

    public ClusterConfiguration setPsEmailHostname(String psEmailHostname) {
        this.psEmailHostname = psEmailHostname;
        return this;
    }

    public String getWpEmailHostname() {
        return wpEmailHostname;
    }

    public void setWpEmailHostname(String wpEmailHostname) {
        this.wpEmailHostname = wpEmailHostname;
    }

    public boolean isUseHTTPRoute() {
        return useHTTPRoute;
    }

    public ClusterConfiguration setUseHTTPRoute(boolean useHTTPRoute) {
        this.useHTTPRoute = useHTTPRoute;
        return this;
    }

    public String getEnvoyGatewayName() {
        return envoyGatewayName;
    }

    public ClusterConfiguration setEnvoyGatewayName(String envoyGatewayName) {
        this.envoyGatewayName = envoyGatewayName;
        return this;
    }

    public String getEnvoyGatewayNamespace() {
        return envoyGatewayNamespace;
    }

    public ClusterConfiguration setEnvoyGatewayNamespace(String envoyGatewayNamespace) {
        this.envoyGatewayNamespace = envoyGatewayNamespace;
        return this;
    }

    public String getEnvoyGatewaySectionName() {
        return envoyGatewaySectionName;
    }

    public ClusterConfiguration setEnvoyGatewaySectionName(String envoyGatewaySectionName) {
        this.envoyGatewaySectionName = envoyGatewaySectionName;
        return this;
    }
}
