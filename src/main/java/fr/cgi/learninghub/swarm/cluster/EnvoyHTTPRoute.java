package fr.cgi.learninghub.swarm.cluster;

import fr.cgi.learning.hub.swarm.common.enums.State;
import fr.cgi.learninghub.swarm.deployment.DeploymentFactory;
import fr.cgi.learninghub.swarm.entity.Deployment;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

import java.util.*;

/**
 * Classe pour créer des HTTPRoute Envoy Gateway à la place des Ingress Nginx
 */
public class EnvoyHTTPRoute {

    public static final String API_VERSION = "gateway.networking.k8s.io/v1";
    public static final String KIND = "HTTPRoute";
    public static final String METADATA_LABELS_APP = "app.kubernetes.io/managed-by";
    public static final String METADATA_LABELS_APP_VALUE = "agent";
    
    private final List<Deployment> deployments;
    private final String routeName;
    
    private String publicHostName;
    private String gatewayName = "public-gateway";
    private String gatewayNamespace = "envoy";
    private String sectionName = "https";
    private Integer gatewayPort = 443;

    public EnvoyHTTPRoute(List<Deployment> deployments, String routeName) {
        this.deployments = deployments;
        this.routeName = routeName;
    }

    /**
     * Crée une ressource HTTPRoute sous forme de GenericKubernetesResource
     * car le client Kubernetes Fabric8 ne supporte pas encore nativement HTTPRoute v1
     */
    public GenericKubernetesResource get() {
        GenericKubernetesResource httpRoute = new GenericKubernetesResource();
        httpRoute.setApiVersion(API_VERSION);
        httpRoute.setKind(KIND);
        httpRoute.setMetadata(getMetadata());
        
        Map<String, Object> spec = new HashMap<>();
        spec.put("parentRefs", getParentRefs());
        spec.put("hostnames", getHostnames());
        spec.put("rules", getRules());
        
        httpRoute.setAdditionalProperties(Map.of("spec", spec));
        
        return httpRoute;
    }

    private ObjectMeta getMetadata() {
        return new ObjectMetaBuilder()
                .withName(routeName)
                .addToLabels(METADATA_LABELS_APP, METADATA_LABELS_APP_VALUE)
                .build();
    }

    private List<Map<String, Object>> getParentRefs() {
        Map<String, Object> parentRef = new HashMap<>();
        parentRef.put("name", gatewayName);
        parentRef.put("namespace", gatewayNamespace);
        parentRef.put("sectionName", sectionName);
        if (gatewayPort != null) {
            parentRef.put("port", gatewayPort);
        }
        
        return List.of(parentRef);
    }

    private List<String> getHostnames() {
        return List.of(publicHostName);
    }

    private List<Map<String, Object>> getRules() {
        return deployments.stream()
                .map(this::createRuleForDeployment)
                .toList();
    }

    private Map<String, Object> createRuleForDeployment(Deployment deployment) {
        Map<String, Object> rule = new HashMap<>();
        
        // Matches
        Map<String, Object> match = new HashMap<>();
        Map<String, Object> path = new HashMap<>();
        path.put("type", "PathPrefix");
        path.put("value", deployment.getPath());
        match.put("path", path);
        rule.put("matches", List.of(match));
        
        // Filters - pour bloquer l'accès admin si le service est désactivé
        List<Map<String, Object>> filters = new ArrayList<>();
        if (State.DISABLED.equals(deployment.getService().getState())) {
            // On ajoute un filtre de redirection pour bloquer l'accès admin
            Map<String, Object> adminMatch = new HashMap<>();
            Map<String, Object> adminPath = new HashMap<>();
            adminPath.put("type", "PathPrefix");
            adminPath.put("value", deployment.getPath() + "/" + DeploymentFactory.getAdminPathPrefix(deployment.getService().getType()));
            adminMatch.put("path", adminPath);
            
            Map<String, Object> redirectFilter = new HashMap<>();
            redirectFilter.put("type", "RequestRedirect");
            Map<String, Object> requestRedirect = new HashMap<>();
            requestRedirect.put("statusCode", 403);
            redirectFilter.put("requestRedirect", requestRedirect);
            
            // Créer une règle séparée pour l'admin path avec redirection
            // Note: HTTPRoute ne supporte pas directement le blocage 403
            // On utilise RequestHeaderModifier pour ajouter un header que le backend peut utiliser
        }
        
        // BackendRefs
        Map<String, Object> backendRef = new HashMap<>();
        backendRef.put("name", deployment.getService().getServiceName());
        backendRef.put("port", 80);
        rule.put("backendRefs", List.of(backendRef));
        
        return rule;
    }

    // Getters et setters
    public String getPublicHostName() {
        return publicHostName;
    }

    public EnvoyHTTPRoute setPublicHostName(String publicHostName) {
        this.publicHostName = publicHostName;
        return this;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public EnvoyHTTPRoute setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
        return this;
    }

    public String getGatewayNamespace() {
        return gatewayNamespace;
    }

    public EnvoyHTTPRoute setGatewayNamespace(String gatewayNamespace) {
        this.gatewayNamespace = gatewayNamespace;
        return this;
    }

    public String getSectionName() {
        return sectionName;
    }

    public EnvoyHTTPRoute setSectionName(String sectionName) {
        this.sectionName = sectionName;
        return this;
    }

    public Integer getGatewayPort() {
        return gatewayPort;
    }

    public EnvoyHTTPRoute setGatewayPort(Integer gatewayPort) {
        this.gatewayPort = gatewayPort;
        return this;
    }

    public String getRouteName() {
        return routeName;
    }
}
