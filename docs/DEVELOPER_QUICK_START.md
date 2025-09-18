# Firefly Session Manager Library - Developer Quick Start Guide

## Table of Contents

- [Getting Started](#getting-started)
- [Basic Usage](#basic-usage)
- [Extending the System](#extending-the-system)
- [Implementing Custom Validators](#implementing-custom-validators)
- [Improving Session Manager Context](#improving-session-manager-context)
- [Advanced Configuration](#advanced-configuration)
- [Testing Strategies](#testing-strategies)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

## Getting Started

### Prerequisites

- Java 21 or higher
- Spring Boot 3.2+
- Maven 3.8+
- Redis (for L2 caching, optional)

### Adding the Dependency

Add the session manager library to your `pom.xml`:

```xml
<dependency>
    <groupId>com.firefly</groupId>
    <artifactId>lib-session-manager</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Basic Configuration

Add to your `application.yml`:

```yaml
firefly:
  session-manager:
    enabled: true
    session-timeout-minutes: 30
    client:
      customer-api-bash-path: "http://customer-service"
      contract-api-bash-path: "http://contract-service"
      product-api-bash-path: "http://product-service"
      reference-master-data-api-bash-path: "http://reference-service"
```

### Enable Auto-Configuration

The session manager uses Spring Boot auto-configuration. Simply add the dependency and it will be automatically configured.

## Basic Usage

### 1. Injecting the Session Manager

```java
@RestController
@RequiredArgsConstructor
public class BankingController {
    
    private final FireflySessionManager sessionManager;
    
    @GetMapping("/accounts")
    public Mono<List<AccountDTO>> getAccounts(ServerWebExchange exchange) {
        return sessionManager.getCurrentSession()
            .cast(SessionContext.class)
            .flatMap(session -> {
                // Use session context to get customer's accounts
                return accountService.getAccountsByPartyId(session.getPartyId());
            });
    }
}
```

### 2. Using Access Validators

```java
@RestController
public class AccountController {
    
    @GetMapping("/accounts/{accountId}")
    @RequiresOwnership("product") // Uses SessionBasedProductAccessValidator
    public Mono<AccountDTO> getAccount(@PathVariable UUID accountId) {
        return accountService.getAccount(accountId);
    }
    
    @GetMapping("/contracts/{contractId}")
    @RequiresOwnership("contract") // Uses SessionBasedContractAccessValidator
    public Mono<ContractDTO> getContract(@PathVariable UUID contractId) {
        return contractService.getContract(contractId);
    }
}
```

### 3. Creating Sessions

```java
@PostMapping("/sessions")
public Mono<SessionContext> createSession(
        @RequestHeader("X-Party-Id") UUID partyId,
        ServerWebExchange exchange) {
    
    return sessionManager.createSession(partyId, exchange);
}
```

## Extending the System

### 1. Custom Session Context Extractor

Create a custom extractor for specialized header handling:

```java
@Component
@Primary
public class CustomSessionContextExtractor implements SessionContextExtractor {
    
    @Override
    public Mono<UUID> extractPartyId(ServerWebExchange exchange) {
        // Custom logic for extracting party ID
        String customHeader = exchange.getRequest()
            .getHeaders()
            .getFirst("X-Custom-Party-Id");
            
        if (customHeader != null) {
            return Mono.just(UUID.fromString(customHeader));
        }
        
        // Fallback to default behavior
        return extractPartyIdFromStandardHeader(exchange);
    }
    
    @Override
    public Mono<SessionMetadata> extractSessionMetadata(ServerWebExchange exchange) {
        // Extract custom metadata
        return Mono.just(SessionMetadata.builder()
            .deviceType(getDeviceType(exchange))
            .applicationVersion(getAppVersion(exchange))
            .build());
    }
}
```

### 2. Custom Service Implementations

Extend existing services for specialized business logic:

```java
@Service
@Primary
public class EnhancedCustomerProfileService extends CustomerProfileServiceImpl {
    
    private final RiskAssessmentService riskService;
    
    public EnhancedCustomerProfileService(
            NaturalPersonsApi naturalPersonsApi,
            PartyRelationshipsApi partyRelationshipsApi,
            PartyStatusesApi partyStatusesApi,
            ContractService contractService,
            RiskAssessmentService riskService) {
        super(naturalPersonsApi, partyRelationshipsApi, partyStatusesApi, contractService);
        this.riskService = riskService;
    }
    
    @Override
    public Mono<CustomerProfile> getCustomerProfile(UUID partyId) {
        return super.getCustomerProfile(partyId)
            .flatMap(profile -> enhanceWithRiskData(profile));
    }
    
    private Mono<CustomerProfile> enhanceWithRiskData(CustomerProfile profile) {
        return riskService.getRiskScore(profile.getPartyId())
            .map(riskScore -> profile.toBuilder()
                .riskScore(riskScore)
                .build())
            .defaultIfEmpty(profile);
    }
}
```

### 3. Adding New Domain Models

Extend the session context with custom data:

```java
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class EnhancedSessionContext extends SessionContext {
    
    private RiskProfile riskProfile;
    private List<Notification> pendingNotifications;
    private PreferenceSettings preferences;
    
    // Custom methods
    public boolean isHighRiskCustomer() {
        return riskProfile != null && riskProfile.getScore() > 80;
    }
    
    public boolean hasUnreadNotifications() {
        return pendingNotifications != null && 
               pendingNotifications.stream().anyMatch(n -> !n.isRead());
    }
}
```

## Implementing Custom Validators

### 1. Resource-Specific Validator

Create validators for new resource types:

```java
@Component
@AccessValidatorFor("loan")
@RequiredArgsConstructor
@Slf4j
public class SessionBasedLoanAccessValidator implements AccessValidator {
    
    private final FireflySessionManager sessionManager;
    private final LoanService loanService;
    
    @Override
    public String getResourceName() {
        return "loan";
    }
    
    @Override
    public Mono<Boolean> canAccess(String resourceId, AuthInfo authInfo) {
        log.debug("Validating loan access for resource ID: {}", resourceId);
        
        try {
            UUID loanId = UUID.fromString(resourceId);
            
            return sessionManager.getCurrentSession()
                .cast(SessionContext.class)
                .flatMap(session -> validateLoanAccess(loanId, session, authInfo))
                .defaultIfEmpty(false);
                
        } catch (IllegalArgumentException e) {
            log.warn("Invalid loan ID format: {}", resourceId);
            return Mono.just(false);
        }
    }
    
    private Mono<Boolean> validateLoanAccess(UUID loanId, SessionContext session, AuthInfo authInfo) {
        // Check employee bypass
        if (hasEmployeeRole(authInfo)) {
            return Mono.just(true);
        }
        
        // Check if party has access to this loan
        return loanService.getLoanByPartyId(session.getPartyId(), loanId)
            .map(loan -> loan != null && "ACTIVE".equals(loan.getStatus()))
            .onErrorReturn(false);
    }
    
    private boolean hasEmployeeRole(AuthInfo authInfo) {
        return authInfo != null && 
               authInfo.getRoles() != null &&
               authInfo.getRoles().stream()
                   .anyMatch(role -> "LOAN_OFFICER".equals(role) || 
                                   "ADMIN".equals(role));
    }
}
```

### 2. Complex Business Logic Validator

Implement validators with sophisticated business rules:

```java
@Component
@AccessValidatorFor("investment")
@RequiredArgsConstructor
@Slf4j
public class InvestmentAccessValidator implements AccessValidator {
    
    private final FireflySessionManager sessionManager;
    private final InvestmentService investmentService;
    private final ComplianceService complianceService;
    
    @Override
    public String getResourceName() {
        return "investment";
    }
    
    @Override
    public Mono<Boolean> canAccess(String resourceId, AuthInfo authInfo) {
        UUID investmentId = UUID.fromString(resourceId);
        
        return sessionManager.getCurrentSession()
            .cast(SessionContext.class)
            .flatMap(session -> validateInvestmentAccess(investmentId, session, authInfo));
    }
    
    private Mono<Boolean> validateInvestmentAccess(UUID investmentId, SessionContext session, AuthInfo authInfo) {
        return Mono.zip(
            checkOwnership(investmentId, session.getPartyId()),
            checkComplianceRules(investmentId, session),
            checkRiskTolerance(investmentId, session)
        ).map(tuple -> tuple.getT1() && tuple.getT2() && tuple.getT3());
    }
    
    private Mono<Boolean> checkOwnership(UUID investmentId, UUID partyId) {
        return investmentService.isOwner(investmentId, partyId);
    }
    
    private Mono<Boolean> checkComplianceRules(UUID investmentId, SessionContext session) {
        return complianceService.validateAccess(investmentId, session.getPartyId());
    }
    
    private Mono<Boolean> checkRiskTolerance(UUID investmentId, SessionContext session) {
        return investmentService.getInvestmentRisk(investmentId)
            .flatMap(risk -> customerService.getRiskTolerance(session.getPartyId())
                .map(tolerance -> risk.getLevel() <= tolerance.getMaxLevel()));
    }
}
```

### 3. Time-Based Access Validator

Implement validators with temporal constraints:

```java
@Component
@AccessValidatorFor("trading")
@RequiredArgsConstructor
@Slf4j
public class TradingAccessValidator implements AccessValidator {
    
    private final FireflySessionManager sessionManager;
    private final TradingService tradingService;
    
    @Override
    public String getResourceName() {
        return "trading";
    }
    
    @Override
    public Mono<Boolean> canAccess(String resourceId, AuthInfo authInfo) {
        UUID tradingAccountId = UUID.fromString(resourceId);
        
        return sessionManager.getCurrentSession()
            .cast(SessionContext.class)
            .flatMap(session -> validateTradingAccess(tradingAccountId, session, authInfo));
    }
    
    private Mono<Boolean> validateTradingAccess(UUID accountId, SessionContext session, AuthInfo authInfo) {
        return Mono.zip(
            checkAccountOwnership(accountId, session.getPartyId()),
            checkTradingHours(),
            checkAccountStatus(accountId),
            checkDailyLimits(accountId, session.getPartyId())
        ).map(tuple -> tuple.getT1() && tuple.getT2() && tuple.getT3() && tuple.getT4());
    }
    
    private Mono<Boolean> checkTradingHours() {
        LocalTime now = LocalTime.now();
        LocalTime marketOpen = LocalTime.of(9, 30);
        LocalTime marketClose = LocalTime.of(16, 0);
        
        return Mono.just(now.isAfter(marketOpen) && now.isBefore(marketClose));
    }
    
    private Mono<Boolean> checkDailyLimits(UUID accountId, UUID partyId) {
        return tradingService.getDailyTradingVolume(accountId, LocalDate.now())
            .flatMap(volume -> tradingService.getDailyLimit(partyId)
                .map(limit -> volume.compareTo(limit) < 0));
    }
}
```

## Improving Session Manager Context

### 1. Enhanced Session Context

Add new fields and capabilities to the session context:

```java
@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class EnhancedSessionContext extends SessionContext {
    
    // Additional customer insights
    private CustomerSegment segment;
    private List<ProductRecommendation> recommendations;
    private RiskProfile riskProfile;
    
    // Behavioral data
    private SessionBehavior behavior;
    private List<RecentTransaction> recentActivity;
    
    // Preferences and settings
    private NotificationPreferences notificationPrefs;
    private SecuritySettings securitySettings;
    
    // Business context
    private List<ActiveCampaign> eligibleCampaigns;
    private ComplianceStatus complianceStatus;
    
    // Helper methods
    public boolean isEligibleForProduct(String productType) {
        return recommendations.stream()
            .anyMatch(rec -> rec.getProductType().equals(productType) && 
                           rec.getEligibilityScore() > 0.7);
    }
    
    public boolean requiresAdditionalVerification() {
        return riskProfile.getScore() > 75 || 
               behavior.hasUnusualActivity() ||
               !complianceStatus.isFullyCompliant();
    }
}
```

### 2. Context Enrichment Service

Create a service to enrich session context with additional data:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionContextEnrichmentService {
    
    private final CustomerInsightsService insightsService;
    private final RecommendationEngine recommendationEngine;
    private final RiskAssessmentService riskService;
    private final ComplianceService complianceService;
    
    public Mono<EnhancedSessionContext> enrichSessionContext(SessionContext baseContext) {
        UUID partyId = baseContext.getPartyId();
        
        return Mono.zip(
            getCustomerSegment(partyId),
            getProductRecommendations(partyId),
            getRiskProfile(partyId),
            getComplianceStatus(partyId),
            getEligibleCampaigns(partyId)
        ).map(tuple -> EnhancedSessionContext.builder()
            .from(baseContext)
            .segment(tuple.getT1())
            .recommendations(tuple.getT2())
            .riskProfile(tuple.getT3())
            .complianceStatus(tuple.getT4())
            .eligibleCampaigns(tuple.getT5())
            .build())
        .doOnSuccess(context -> log.debug("Enriched session context for party: {}", partyId))
        .onErrorResume(error -> {
            log.warn("Failed to enrich session context for party: {}", partyId, error);
            return Mono.just(EnhancedSessionContext.builder()
                .from(baseContext)
                .build());
        });
    }
    
    private Mono<CustomerSegment> getCustomerSegment(UUID partyId) {
        return insightsService.getCustomerSegment(partyId)
            .timeout(Duration.ofSeconds(2))
            .onErrorReturn(CustomerSegment.STANDARD);
    }
    
    private Mono<List<ProductRecommendation>> getProductRecommendations(UUID partyId) {
        return recommendationEngine.getRecommendations(partyId)
            .timeout(Duration.ofSeconds(3))
            .onErrorReturn(Collections.emptyList());
    }
}
```

### 3. Custom Session Manager Implementation

Extend the session manager with enhanced capabilities:

```java
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class EnhancedFireflySessionManager extends FireflySessionManager {
    
    private final SessionContextEnrichmentService enrichmentService;
    private final SessionAnalyticsService analyticsService;
    private final FraudDetectionService fraudService;
    
    @Override
    public Mono<SessionContext> createSession(UUID partyId, ServerWebExchange exchange) {
        return super.createSession(partyId, exchange)
            .cast(SessionContext.class)
            .flatMap(this::enhanceAndValidateSession)
            .doOnSuccess(session -> recordSessionCreation(session, exchange));
    }
    
    private Mono<EnhancedSessionContext> enhanceAndValidateSession(SessionContext baseSession) {
        return enrichmentService.enrichSessionContext(baseSession)
            .flatMap(this::validateSessionSecurity)
            .flatMap(this::applyBusinessRules);
    }
    
    private Mono<EnhancedSessionContext> validateSessionSecurity(EnhancedSessionContext session) {
        return fraudService.validateSession(session)
            .flatMap(isValid -> {
                if (!isValid) {
                    return Mono.error(new SecurityException("Session failed fraud validation"));
                }
                return Mono.just(session);
            });
    }
    
    private Mono<EnhancedSessionContext> applyBusinessRules(EnhancedSessionContext session) {
        // Apply business rules based on customer segment, risk profile, etc.
        if (session.getRiskProfile().getScore() > 90) {
            session = session.toBuilder()
                .securitySettings(SecuritySettings.HIGH_SECURITY)
                .build();
        }
        
        return Mono.just(session);
    }
    
    private void recordSessionCreation(SessionContext session, ServerWebExchange exchange) {
        analyticsService.recordSessionEvent(SessionEvent.builder()
            .sessionId(session.getSessionId())
            .partyId(session.getPartyId())
            .eventType("SESSION_CREATED")
            .ipAddress(getClientIpAddress(exchange))
            .userAgent(getUserAgent(exchange))
            .timestamp(LocalDateTime.now())
            .build());
    }
}
```

## Advanced Configuration

### 1. Custom Cache Configuration

Configure specialized caching strategies:

```java
@Configuration
public class CustomCacheConfiguration {

    @Bean
    @Primary
    public CacheManager customSessionCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Different cache configurations for different data types
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(20000)  // Larger cache for high-traffic applications
            .expireAfterWrite(45, TimeUnit.MINUTES)
            .expireAfterAccess(20, TimeUnit.MINUTES)
            .recordStats()
            .removalListener((key, value, cause) -> {
                log.debug("Cache entry removed: key={}, cause={}", key, cause);
            }));

        return cacheManager;
    }

    @Bean
    @ConditionalOnProperty(name = "firefly.session-manager.cache.redis.cluster.enabled", havingValue = "true")
    public RedisConnectionFactory clusterRedisConnectionFactory() {
        // Configure Redis cluster for high availability
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration()
            .clusterNode("redis-node-1", 6379)
            .clusterNode("redis-node-2", 6379)
            .clusterNode("redis-node-3", 6379);

        return new JedisConnectionFactory(clusterConfig);
    }
}
```

### 2. Custom Client Configuration

Configure external service clients with advanced settings:

```java
@Configuration
public class CustomClientConfiguration {

    @Bean
    @Primary
    public ClientsFactory enhancedClientsFactory(SessionManagerClientFactoryProperties properties) {
        return new EnhancedClientsFactory(properties);
    }
}

@Component
public class EnhancedClientsFactory extends ClientsFactory {

    public EnhancedClientsFactory(SessionManagerClientFactoryProperties properties) {
        super(properties);
    }

    @Bean
    @Primary
    public NaturalPersonsApi enhancedNaturalPersonsApi() {
        ApiClient client = createEnhancedApiClient(getCustomerApiBasePath());
        return new NaturalPersonsApi(client);
    }

    private ApiClient createEnhancedApiClient(String basePath) {
        ApiClient client = new ApiClient();
        client.setBasePath(basePath);

        // Add custom interceptors
        client.addDefaultHeader("X-Service-Name", "session-manager");
        client.setConnectTimeout(5000);
        client.setReadTimeout(10000);

        // Add retry configuration
        client.setRetryOnConnectionFailure(true);

        return client;
    }
}
```

### 3. Environment-Specific Configuration

Configure different settings for different environments:

```yaml
# application-dev.yml
firefly:
  session-manager:
    cache:
      l1:
        maximum-size: 1000
        expire-after-write: 10
      l2:
        enabled: false  # Disable Redis in development

# application-prod.yml
firefly:
  session-manager:
    cache:
      l1:
        maximum-size: 50000
        expire-after-write: 60
      l2:
        enabled: true
        ttl-minutes: 120
        connection:
          host: redis-cluster.prod.firefly.com
          port: 6379
          ssl: true
```

## Testing Strategies

### 1. Unit Testing Session Manager

```java
@ExtendWith(MockitoExtension.class)
class FireflySessionManagerTest {

    @Mock
    private CustomerProfileService customerProfileService;

    @Mock
    private SessionContextExtractor contextExtractor;

    @InjectMocks
    private FireflySessionManager sessionManager;

    @Test
    void shouldCreateSessionSuccessfully() {
        // Given
        UUID partyId = UUID.randomUUID();
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerRequest.builder()
                .header("X-Party-Id", partyId.toString())
                .build());

        CustomerProfile profile = CustomerProfile.builder()
            .partyId(partyId)
            .givenName("John")
            .familyName1("Doe")
            .build();

        when(contextExtractor.extractPartyId(exchange))
            .thenReturn(Mono.just(partyId));
        when(customerProfileService.getCustomerProfile(partyId))
            .thenReturn(Mono.just(profile));

        // When
        Mono<SessionContext> result = sessionManager.createSession(partyId, exchange);

        // Then
        StepVerifier.create(result)
            .assertNext(session -> {
                assertThat(session.getPartyId()).isEqualTo(partyId);
                assertThat(session.getCustomerProfile()).isEqualTo(profile);
                assertThat(session.getSessionId()).isNotNull();
            })
            .verifyComplete();
    }
}
```

### 2. Integration Testing with Testcontainers

```java
@SpringBootTest
@Testcontainers
class SessionManagerIntegrationTest {

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("firefly.session-manager.cache.l2.enabled", () -> true);
    }

    @Autowired
    private FireflySessionManager sessionManager;

    @Test
    void shouldPersistSessionInRedis() {
        // Test session persistence across application restarts
        UUID partyId = UUID.randomUUID();
        ServerWebExchange exchange = createMockExchange(partyId);

        // Create session
        SessionContext session = sessionManager.createSession(partyId, exchange)
            .block(Duration.ofSeconds(5));

        assertThat(session).isNotNull();

        // Verify session can be retrieved
        SessionContext retrieved = sessionManager.getCurrentSession()
            .cast(SessionContext.class)
            .block(Duration.ofSeconds(5));

        assertThat(retrieved.getSessionId()).isEqualTo(session.getSessionId());
    }
}
```

### 3. Testing Custom Validators

```java
@ExtendWith(MockitoExtension.class)
class CustomValidatorTest {

    @Mock
    private FireflySessionManager sessionManager;

    @Mock
    private LoanService loanService;

    @InjectMocks
    private SessionBasedLoanAccessValidator validator;

    @Test
    void shouldAllowAccessToOwnedLoan() {
        // Given
        UUID loanId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();

        SessionContext session = SessionContext.builder()
            .partyId(partyId)
            .build();

        LoanDTO loan = LoanDTO.builder()
            .loanId(loanId)
            .status("ACTIVE")
            .build();

        when(sessionManager.getCurrentSession())
            .thenReturn(Mono.just(session));
        when(loanService.getLoanByPartyId(partyId, loanId))
            .thenReturn(Mono.just(loan));

        AuthInfo authInfo = AuthInfo.builder()
            .roles(List.of("CUSTOMER"))
            .build();

        // When
        Mono<Boolean> result = validator.canAccess(loanId.toString(), authInfo);

        // Then
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete();
    }
}
```

## Best Practices

### 1. Performance Optimization

- **Use Reactive Streams**: Always return `Mono` or `Flux` for non-blocking operations
- **Cache Strategically**: Cache frequently accessed, slowly changing data
- **Batch Operations**: Combine multiple API calls when possible
- **Set Timeouts**: Always set reasonable timeouts for external calls

```java
// Good: Non-blocking with timeout
public Mono<CustomerProfile> getCustomerProfile(UUID partyId) {
    return customerApi.getCustomer(partyId)
        .timeout(Duration.ofSeconds(5))
        .retry(2)
        .onErrorResume(this::handleCustomerServiceError);
}

// Bad: Blocking operation
public CustomerProfile getCustomerProfileBlocking(UUID partyId) {
    return customerApi.getCustomer(partyId).block(); // Don't do this!
}
```

### 2. Error Handling

- **Graceful Degradation**: Provide fallback mechanisms
- **Specific Error Types**: Use specific exceptions for different error conditions
- **Comprehensive Logging**: Log errors with sufficient context

```java
public Mono<SessionContext> createSession(UUID partyId, ServerWebExchange exchange) {
    return customerProfileService.getCustomerProfile(partyId)
        .onErrorResume(CustomerNotFoundException.class, ex -> {
            log.warn("Customer not found: {}", partyId);
            return Mono.error(new SessionCreationException("Invalid customer", ex));
        })
        .onErrorResume(ServiceUnavailableException.class, ex -> {
            log.error("Customer service unavailable", ex);
            return createMinimalSession(partyId); // Fallback
        });
}
```

### 3. Security Considerations

- **Validate All Inputs**: Never trust external input
- **Audit Access**: Log all access attempts
- **Principle of Least Privilege**: Grant minimum necessary permissions

```java
@Override
public Mono<Boolean> canAccess(String resourceId, AuthInfo authInfo) {
    // Always validate input
    if (resourceId == null || authInfo == null) {
        log.warn("Invalid access validation request: resourceId={}, authInfo={}",
                resourceId, authInfo);
        return Mono.just(false);
    }

    // Audit the access attempt
    auditService.logAccessAttempt(resourceId, authInfo.getUserId(), "product");

    // Perform validation
    return performValidation(resourceId, authInfo);
}
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Cache Configuration Issues

**Problem**: `NoSuchBeanDefinitionException` for cache managers

**Solution**: Ensure proper configuration classes are included:

```java
@SpringBootTest(classes = {
    SessionManagerCacheConfiguration.class,
    SessionManagerCacheProperties.class
})
```

#### 2. Redis Connection Issues

**Problem**: Redis connection failures in tests

**Solution**: Use Testcontainers for integration tests:

```java
@Container
static RedisContainer redis = new RedisContainer("redis:7-alpine");
```

#### 3. Session Context Not Found

**Problem**: `getCurrentSession()` returns empty

**Solution**: Ensure session is created and headers are properly set:

```java
// Verify X-Party-Id header is present
String partyId = exchange.getRequest().getHeaders().getFirst("X-Party-Id");
if (partyId == null) {
    return Mono.error(new SessionNotFoundException("Missing X-Party-Id header"));
}
```

#### 4. Validator Not Working

**Problem**: Custom validator not being called

**Solution**: Ensure proper annotations and component scanning:

```java
@Component
@AccessValidatorFor("your-resource-type")  // Must match @RequiresOwnership value
public class YourValidator implements AccessValidator {
    // Implementation
}
```

### Debugging Tips

1. **Enable Debug Logging**:
   ```yaml
   logging:
     level:
       com.firefly.common.auth.session: DEBUG
   ```

2. **Monitor Cache Statistics**:
   ```java
   @EventListener
   public void handleCacheStatistics(CacheStatisticsEvent event) {
       log.info("Cache stats: {}", event.getStatistics());
   }
   ```

3. **Use Actuator Endpoints**:
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,caches,metrics
   ```

---

*This developer guide provides comprehensive information for extending and customizing the Firefly Session Manager Library. For architectural details, see the [Architecture Documentation](ARCHITECTURE.md).*
```
