package fr.cgi.learninghub.swarm.cluster;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

import java.util.*;

/**
 * Classe pour créer des BackendTrafficPolicy Envoy Gateway
 * Permet de configurer les timeouts, rate limiting, etc.
 */
public class EnvoyBackendTrafficPolicy {

    public static final String API_VERSION = "gateway.envoyproxy.io/v1alpha1";
    public static final String KIND = "BackendTrafficPolicy";
    public static final String METADATA_LABELS_APP = "app.kubernetes.io/managed-by";
    public static final String METADATA_LABELS_APP_VALUE = "agent";
    
    private final String policyName;
    private final String targetHTTPRouteName;
    private String namespace;
    
    // Timeouts
    private String httpRequestTimeout = "30s";
    private String tcpConnectTimeout = "5s";
    
    // Rate limiting
    private boolean enableRateLimit = false;
    private Integer rateLimitRequests = 100;
    private String rateLimitUnit = "Minute";
    
    // Load balancer
    private String loadBalancerType = "RoundRobin";
    
    // Circuit breaker
    private Integer maxConnections = 1024;
    private Integer maxPendingRequests = 1024;

    public EnvoyBackendTrafficPolicy(String policyName, String targetHTTPRouteName, String namespace) {
        this.policyName = policyName;
        this.targetHTTPRouteName = targetHTTPRouteName;
        this.namespace = namespace;
    }

    /**
     * Crée une ressource BackendTrafficPolicy sous forme de GenericKubernetesResource
     */
    public GenericKubernetesResource get() {
        GenericKubernetesResource policy = new GenericKubernetesResource();
        policy.setApiVersion(API_VERSION);
        policy.setKind(KIND);
        policy.setMetadata(getMetadata());
        
        Map<String, Object> spec = new HashMap<>();
        spec.put("mergeType", "StrategicMerge");
        spec.put("targetRef", getTargetRef());
        
        // Timeouts
        spec.put("timeout", getTimeouts());
        
        // Rate limiting (si activé)
        if (enableRateLimit) {
            spec.put("rateLimit", getRateLimit());
        }
        
        // Load balancer
        spec.put("loadBalancer", getLoadBalancer());
        
        // Circuit breaker
        spec.put("circuitBreaker", getCircuitBreaker());
        
        policy.setAdditionalProperties(Map.of("spec", spec));
        
        return policy;
    }

    private ObjectMeta getMetadata() {
        return new ObjectMetaBuilder()
                .withName(policyName)
                .withNamespace(namespace)
                .addToLabels(METADATA_LABELS_APP, METADATA_LABELS_APP_VALUE)
                .build();
    }

    private Map<String, Object> getTargetRef() {
        Map<String, Object> targetRef = new HashMap<>();
        targetRef.put("group", "gateway.networking.k8s.io");
        targetRef.put("kind", "HTTPRoute");
        targetRef.put("name", targetHTTPRouteName);
        return targetRef;
    }

    private Map<String, Object> getTimeouts() {
        Map<String, Object> timeout = new HashMap<>();
        
        Map<String, Object> http = new HashMap<>();
        http.put("requestTimeout", httpRequestTimeout);
        timeout.put("http", http);
        
        Map<String, Object> tcp = new HashMap<>();
        tcp.put("connectTimeout", tcpConnectTimeout);
        timeout.put("tcp", tcp);
        
        return timeout;
    }

    private Map<String, Object> getRateLimit() {
        Map<String, Object> rateLimit = new HashMap<>();
        rateLimit.put("type", "Global");
        
        Map<String, Object> global = new HashMap<>();
        List<Map<String, Object>> rules = new ArrayList<>();
        
        Map<String, Object> rule = new HashMap<>();
        Map<String, Object> limit = new HashMap<>();
        limit.put("requests", rateLimitRequests);
        limit.put("unit", rateLimitUnit);
        rule.put("limit", limit);
        
        rules.add(rule);
        global.put("rules", rules);
        rateLimit.put("global", global);
        
        return rateLimit;
    }

    private Map<String, Object> getLoadBalancer() {
        Map<String, Object> loadBalancer = new HashMap<>();
        loadBalancer.put("type", loadBalancerType);
        return loadBalancer;
    }

    private Map<String, Object> getCircuitBreaker() {
        Map<String, Object> circuitBreaker = new HashMap<>();
        circuitBreaker.put("maxConnections", maxConnections);
        circuitBreaker.put("maxPendingRequests", maxPendingRequests);
        return circuitBreaker;
    }

    // Getters et setters
    public String getHttpRequestTimeout() {
        return httpRequestTimeout;
    }

    public EnvoyBackendTrafficPolicy setHttpRequestTimeout(String httpRequestTimeout) {
        this.httpRequestTimeout = httpRequestTimeout;
        return this;
    }

    public String getTcpConnectTimeout() {
        return tcpConnectTimeout;
    }

    public EnvoyBackendTrafficPolicy setTcpConnectTimeout(String tcpConnectTimeout) {
        this.tcpConnectTimeout = tcpConnectTimeout;
        return this;
    }

    public boolean isEnableRateLimit() {
        return enableRateLimit;
    }

    public EnvoyBackendTrafficPolicy setEnableRateLimit(boolean enableRateLimit) {
        this.enableRateLimit = enableRateLimit;
        return this;
    }

    public Integer getRateLimitRequests() {
        return rateLimitRequests;
    }

    public EnvoyBackendTrafficPolicy setRateLimitRequests(Integer rateLimitRequests) {
        this.rateLimitRequests = rateLimitRequests;
        return this;
    }

    public String getRateLimitUnit() {
        return rateLimitUnit;
    }

    public EnvoyBackendTrafficPolicy setRateLimitUnit(String rateLimitUnit) {
        this.rateLimitUnit = rateLimitUnit;
        return this;
    }

    public String getLoadBalancerType() {
        return loadBalancerType;
    }

    public EnvoyBackendTrafficPolicy setLoadBalancerType(String loadBalancerType) {
        this.loadBalancerType = loadBalancerType;
        return this;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public EnvoyBackendTrafficPolicy setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public Integer getMaxPendingRequests() {
        return maxPendingRequests;
    }

    public EnvoyBackendTrafficPolicy setMaxPendingRequests(Integer maxPendingRequests) {
        this.maxPendingRequests = maxPendingRequests;
        return this;
    }
}
