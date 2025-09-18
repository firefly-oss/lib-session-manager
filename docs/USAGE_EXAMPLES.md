# Session Manager Usage Examples

This document provides comprehensive examples of how to use the Firefly Session Manager library in your microservices.

## Basic Setup

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.firefly</groupId>
    <artifactId>lib-session-manager</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Configuration

```yaml
# application.yml
firefly:
  session-manager:
    enabled: true
    session-timeout-minutes: 30
    client:
      customer-api-base-path: "http://customer-service:8080"
      contract-api-base-path: "http://contract-service:8080"
      product-api-base-path: "http://product-service:8080"
      reference-master-data-api-base-path: "http://reference-service:8080"
    cache:
      l1:
        maximum-size: 10000
        expire-after-write: 30
        expire-after-access: 15
      l2:
        enabled: true
        ttl-minutes: 60

# Redis configuration (if using L2 cache)
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
```

## Usage Examples

### 1. Basic Session Access

```java
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    
    @Autowired
    private FireflySessionManager sessionManager;
    
    @GetMapping
    public Mono<ResponseEntity<List<AccountDTO>>> getAccounts() {
        return sessionManager.getCurrentSession()
                .map(session -> {
                    CustomerProfile profile = session.getCustomerProfile();
                    List<ActiveContract> contracts = profile.getActiveContracts();
                    
                    // Filter accounts based on user's contracts
                    List<AccountDTO> accounts = contracts.stream()
                            .map(this::convertToAccountDTO)
                            .collect(Collectors.toList());
                    
                    return ResponseEntity.ok(accounts);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    private AccountDTO convertToAccountDTO(ActiveContract contract) {
        return AccountDTO.builder()
                .contractId(contract.getContractId())
                .productName(contract.getActiveProduct().getProductName())
                .permissions(contract.getPermissions())
                .build();
    }
}
```

### 2. Manual Session Management

```java
@Service
public class AuthenticationService {
    
    @Autowired
    private FireflySessionManager sessionManager;
    
    public Mono<SessionContext> authenticateUser(UUID partyId, ServerWebExchange exchange) {
        return sessionManager.createOrGetSession(exchange)
                .doOnSuccess(session -> 
                    log.info("User authenticated: {}", session.getPartyId()))
                .doOnError(error -> 
                    log.error("Authentication failed for party: {}", partyId, error));
    }
    
    public Mono<Void> logoutUser(String sessionId) {
        return sessionManager.invalidateSession(sessionId)
                .doOnSuccess(ignored -> 
                    log.info("User logged out, session invalidated: {}", sessionId));
    }
    
    public Mono<Boolean> isUserAuthenticated() {
        return sessionManager.getCurrentSession()
                .flatMap(sessionManager::isSessionValid)
                .onErrorReturn(false);
    }
}
```

### 3. Access Control with Existing Annotations

```java
@RestController
@RequestMapping("/api/contracts")
public class ContractController {
    
    @Autowired
    private FireflySessionManager sessionManager;
    
    @GetMapping("/{contractId}")
    @RequiresOwnership(resourceType = "CONTRACT", permissions = {"READ"})
    public Mono<ResponseEntity<ContractDTO>> getContract(@PathVariable UUID contractId) {
        return sessionManager.getCurrentSession()
                .map(session -> {
                    // Access is already validated by @RequiresOwnership
                    ActiveContract contract = findContractInSession(session, contractId);
                    ContractDTO dto = convertToDTO(contract);
                    return ResponseEntity.ok(dto);
                })
                .onErrorReturn(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{contractId}")
    @RequiresOwnership(resourceType = "CONTRACT", permissions = {"WRITE"})
    public Mono<ResponseEntity<ContractDTO>> updateContract(
            @PathVariable UUID contractId,
            @RequestBody UpdateContractRequest request) {
        
        return sessionManager.getCurrentSession()
                .flatMap(session -> {
                    // Perform update logic
                    return updateContractLogic(contractId, request);
                })
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    private ActiveContract findContractInSession(SessionContext session, UUID contractId) {
        return session.getCustomerProfile().getActiveContracts().stream()
                .filter(contract -> contract.getContractId().equals(contractId))
                .findFirst()
                .orElseThrow(() -> new ContractNotFoundException(contractId));
    }
}
```

### 4. Custom Session Context Extraction

```java
@Component
public class CustomSessionExtractor {
    
    @Autowired
    private FireflySessionManager sessionManager;
    
    public Mono<CustomerProfile> getCurrentCustomerProfile() {
        return sessionManager.getCurrentSession()
                .map(SessionContext::getCustomerProfile);
    }
    
    public Mono<List<String>> getCurrentUserPermissions() {
        return sessionManager.getCurrentSession()
                .map(session -> session.getCustomerProfile().getActiveContracts())
                .map(contracts -> contracts.stream()
                        .flatMap(contract -> contract.getPermissions().getOperationPermissions().stream())
                        .distinct()
                        .collect(Collectors.toList()));
    }
    
    public Mono<Boolean> hasPermission(String permission) {
        return getCurrentUserPermissions()
                .map(permissions -> permissions.contains(permission));
    }
}
```

### 5. Session Refresh and Cache Management

```java
@Service
public class SessionMaintenanceService {
    
    @Autowired
    private FireflySessionManager sessionManager;
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void refreshActiveSessions() {
        // This would typically be done based on some criteria
        // For example, refresh sessions that haven't been updated in the last hour
        log.info("Performing scheduled session refresh");
    }
    
    public Mono<SessionContext> forceRefreshSession() {
        return sessionManager.getCurrentSession()
                .flatMap(session -> sessionManager.refreshSession(session.getSessionId()))
                .doOnSuccess(refreshed -> 
                    log.info("Session refreshed: {}", refreshed.getSessionId()));
    }
    
    public Mono<Void> clearUserCache(UUID partyId) {
        // This would require additional implementation to clear specific user data
        return sessionManager.getCurrentSession()
                .filter(session -> session.getPartyId().equals(partyId))
                .flatMap(session -> sessionManager.invalidateSession(session.getSessionId()));
    }
}
```

### 6. Error Handling and Fallbacks

```java
@RestController
public class RobustController {
    
    @Autowired
    private FireflySessionManager sessionManager;
    
    @GetMapping("/user-info")
    public Mono<ResponseEntity<UserInfoDTO>> getUserInfo() {
        return sessionManager.getCurrentSession()
                .map(this::buildUserInfo)
                .map(ResponseEntity::ok)
                .onErrorResume(SessionNotFoundException.class, ex -> {
                    log.warn("Session not found: {}", ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                })
                .onErrorResume(SessionExpiredException.class, ex -> {
                    log.warn("Session expired: {}", ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .header("X-Session-Expired", "true")
                            .build());
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Unexpected error retrieving user info", ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
    
    private UserInfoDTO buildUserInfo(SessionContext session) {
        CustomerProfile profile = session.getCustomerProfile();
        return UserInfoDTO.builder()
                .partyId(profile.getPartyId())
                .fullName(profile.getFirstName() + " " + profile.getFirstSurname())
                .lastLogin(profile.getLastLogin())
                .activeContracts(profile.getActiveContracts().size())
                .sessionId(session.getSessionId())
                .build();
    }
}
```

### 7. Testing with Session Manager

```java
@SpringBootTest
@TestPropertySource(properties = {
    "firefly.session-manager.enabled=true",
    "firefly.session-manager.cache.l2.enabled=false" // Disable Redis for tests
})
class SessionManagerIntegrationTest {
    
    @Autowired
    private FireflySessionManager sessionManager;
    
    @Test
    void shouldCreateSessionWithValidPartyId() {
        UUID partyId = UUID.randomUUID();
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Party-Id", partyId.toString())
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        StepVerifier.create(sessionManager.createOrGetSession(exchange))
                .assertNext(session -> {
                    assertThat(session.getPartyId()).isEqualTo(partyId);
                    assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
                })
                .verifyComplete();
    }
    
    @Test
    void shouldHandleInvalidPartyId() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Party-Id", "invalid-uuid")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        StepVerifier.create(sessionManager.createOrGetSession(exchange))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
```

## Best Practices

### 1. Header Management

Always ensure the `X-Party-Id` header is present in requests:

```java
@Component
public class PartyIdHeaderFilter implements WebFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String partyId = extractPartyIdFromToken(exchange);
        if (partyId != null) {
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-Party-Id", partyId)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }
        return chain.filter(exchange);
    }
}
```

### 2. Performance Optimization

```java
@Configuration
public class SessionManagerOptimization {
    
    @Bean
    @ConditionalOnProperty(name = "firefly.session-manager.cache.l1.enabled", havingValue = "true")
    public CacheManager optimizedCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(20000) // Increased for high-traffic applications
                .expireAfterWrite(45, TimeUnit.MINUTES)
                .expireAfterAccess(20, TimeUnit.MINUTES)
                .recordStats()); // Enable metrics
        return cacheManager;
    }
}
```

### 3. Monitoring and Alerting

```java
@Component
public class SessionMetrics {
    
    private final MeterRegistry meterRegistry;
    private final FireflySessionManager sessionManager;
    
    @EventListener
    public void handleSessionCreated(SessionCreatedEvent event) {
        meterRegistry.counter("session.created").increment();
    }
    
    @EventListener
    public void handleSessionExpired(SessionExpiredEvent event) {
        meterRegistry.counter("session.expired").increment();
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void recordActiveSessionCount() {
        // This would require additional implementation
        meterRegistry.gauge("session.active.count", getActiveSessionCount());
    }
}
```

This comprehensive guide should help you integrate and use the Session Manager library effectively in your Firefly microservices.
