# Firefly Session Manager Library

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Reactive](https://img.shields.io/badge/Reactive-WebFlux-purple.svg)](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)

A **production-ready, enterprise-grade session management library** for the **Firefly Core Banking Platform**, providing comprehensive customer context aggregation, contract-based authorization, and seamless integration with microservices architecture.

## 🏦 **Banking Domain Expertise**

This library is specifically designed for **core banking operations** with deep understanding of:

- **Customer-Party-Contract-Product Relationships**: Complete banking entity model
- **Contract Role-Based Access Control**: 22 comprehensive banking roles covering retail, corporate, lending, legal, and investment scenarios
- **Multi-Entity Authorization**: Support for customers acting on behalf of legal entities
- **Banking Product Hierarchy**: Accounts, loans, cards, investments, and complex financial products
- **Regulatory Compliance**: SOX, PCI-DSS, and banking regulation support with complete audit trails

## 📋 Table of Contents

- [🏗️ Architecture Overview](#️-architecture-overview)
- [🚀 Key Features](#-key-features)
- [💡 Core Banking Concepts](#-core-banking-concepts)
- [📦 Quick Start](#-quick-start)
- [🎯 Deep Usage Scenarios](#-deep-usage-scenarios)
- [🔐 Contract Role System](#-contract-role-system)
- [🌐 Integration with lib-common-domain](#-integration-with-lib-common-domain)
- [🔗 Integration with lib-common-auth](#-integration-with-lib-common-auth)
- [⚙️ Configuration](#️-configuration)
- [🔧 Technology Stack](#-technology-stack)
- [📄 License](#-license)

## 🏗️ Architecture Overview

The Firefly Session Manager operates within the **Core-Domain Layer** of Firefly's 4-tier microservices architecture, providing centralized customer context and authorization services:

```
┌─────────────────────────────────────────────────────────────┐
│                    Channels Layer                           │
│              (Mobile, Web, API Gateway)                     │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│               Application/Process Layer                     │
│            (Business processes & workflows)                 │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│ ★                    Core-Domain Layer                    ★ │
│        ┌─────────────────────────────────────────────┐      │
│        │         Session Manager Library             │      │
│        │  • Customer Context Aggregation             │      │
│        │  • Contract Role Authorization              │      │
│        │  • Product Access Validation                │      │
│        │  • lib-common-auth Integration              │      │
│        │  • lib-common-domain Compatibility          │      │
│        └─────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                  Core-Infrastructure Layer                  │
│              (Database CRUD & data persistence)             │
└─────────────────────────────────────────────────────────────┘
```

### **Session Context Flow**

```
HTTP Request (X-Party-Id) → Session Manager → Customer Context
                                    ↓
                            Contract Aggregation
                                    ↓
                            Role-Based Authorization
                                    ↓
                            Product Access Validation
                                    ↓
                            Cached Session Context
```

### **Core Components Architecture**

```
┌─────────────────────────────────────────────────────────────┐
│                 Session Manager Core                        │
├─────────────────────────────────────────────────────────────┤
│  FireflySessionManager                                      │
│  ├── SessionContextExtractor                                │
│  ├── CustomerProfileService                                 │
│  ├── ContractService                                        │
│  ├── ProductService                                         │
│  └── ContractRoleService                                    │
├─────────────────────────────────────────────────────────────┤
│                 Cache Layer                                 │
│  ├── Caffeine Cache (In-Memory)                             │
│  └── Redis Cache (Distributed) [Optional]                   │
├─────────────────────────────────────────────────────────────┤
│                 Integration Layer                           │
│  ├── lib-common-auth Access Validators                      │
│  ├── lib-common-domain CQRS Integration                     │
│  └── SDK Clients (Customer, Contract, Product, Reference)   │
├─────────────────────────────────────────────────────────────┤
│                 Resilience Layer                            │
│  ├── Circuit Breakers (Resilience4j)                        │
│  ├── Retry Mechanisms                                       │
│  └── Timeout Management                                     │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Key Features

### 🎯 **Customer Context Aggregation**
- **Party-Centric Design**: Centralized customer context using X-Party-Id headers
- **Real-Time Data**: Live aggregation from Customer, Contract, and Product SDKs
- **Multi-Entity Support**: Customers acting on behalf of legal entities through party relationships
- **Performance Optimized**: Intelligent caching with configurable TTL and cache strategies

### 🔐 **Contract Role-Based Access Control**
- **22 Default Banking Roles**: Comprehensive coverage for retail, corporate, lending, legal, and investment scenarios
- **Extensible Role System**: Custom roles through Reference Master Data SDK integration
- **Hierarchical Permissions**: Role priority system for conflict resolution
- **Resource-Based Authorization**: Product-specific access control using productId from contracts

### 🌐 **Seamless Microservice Integration**
- **lib-common-domain Compatibility**: Full integration with domain layer patterns
- **lib-common-auth Integration**: Automatic access validator registration
- **Spring Boot Auto-Configuration**: Zero-configuration setup with sensible defaults
- **Reactive Architecture**: Built on Spring WebFlux for non-blocking operations

### 🚀 **Production-Ready Features**
- **Circuit Breaker Pattern**: Resilience4j integration for API failure handling
- **Dual Cache Strategy**: Caffeine (in-memory) OR Redis (distributed) with graceful fallback
- **Comprehensive Monitoring**: Metrics, health checks, and observability
- **Banking-Grade Security**: Zero-trust architecture with explicit authorization

## 💡 Core Banking Concepts

Understanding these fundamental banking concepts is essential for effectively using this library:

### **Customer-Party-Contract-Product Relationships**

In core banking, the relationships between entities follow a specific hierarchy:

```
Natural Person (Customer)
    ↓ (partyId)
Party (Legal representation)
    ↓ (party relationships)
Legal Entity (When acting on behalf)
    ↓ (contract participation)
Contracts (With specific roles)
    ↓ (product linkage)
Products (Banking products and services)
```

**Key Principles:**
- **Natural Persons** are individual customers with personal information
- **Parties** represent the legal capacity to enter contracts
- **Party Relationships** enable customers to act on behalf of legal entities
- **Contracts** link parties to specific banking products with defined roles
- **Products** are the actual banking services (accounts, loans, cards, etc.)

### **Contract Roles and Permissions**

Contract roles define what actions a party can perform on a specific product:

**Retail Banking Roles:**
- `OWNER` - Full control over personal accounts
- `JOINT_OWNER` - Shared ownership with limitations
- `AUTHORIZED_USER` - Transaction permissions without ownership
- `VIEWER` - Read-only access to account information
- `BENEFICIARY` - Receives benefits but cannot control account

**Corporate Banking Roles:**
- `ACCOUNT_ADMINISTRATOR` - Full corporate account control
- `TRANSACTION_MANAGER` - High-value transaction authority
- `APPROVER` - Transaction approval authority
- `INITIATOR` - Transaction initiation (requires approval)
- `INQUIRY_USER` - Read-only corporate access

### **Resource-Based Access Control**

Access control is based on **product-specific resources** rather than contract IDs:

```java
// ✅ Correct: Resource is productId from contract
@RequiresOwnership(resource = "product", paramIndex = 0)
public Mono<AccountDetails> getAccountDetails(UUID productId) {
    // productId comes from the contract's linked product
}

// ❌ Incorrect: Resource should not be contractId
@RequiresOwnership(resource = "contract", paramIndex = 0)
public Mono<AccountDetails> getAccountDetails(UUID contractId) {
    // This bypasses product-specific authorization
}
```

## 📦 Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.firefly</groupId>
    <artifactId>lib-session-manager</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Enable Auto-Configuration

The library uses Spring Boot's auto-configuration. Simply add the dependency:

```java
@SpringBootApplication
public class BankingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankingServiceApplication.class, args);
    }
}
```

### 3. Basic Configuration

```yaml
firefly:
  session-manager:
    # Cache Strategy (choose ONE)
    cache:
      type: caffeine  # or redis
      caffeine:
        maximum-size: 10000
        expire-after-write: 30
        expire-after-access: 15
      redis:
        ttl-minutes: 120
        key-prefix: "firefly:session:"
        connection:
          host: localhost
          port: 6379
          database: 0
          password: your-password  # optional
          username: your-username  # optional (Redis 6+)
          timeout: 2000
          ssl: false

    # API Client Configuration
    client:
      customer-api-bash-path: ${CUSTOMER_API_URL:http://customer-service:8080}
      contract-api-bash-path: ${CONTRACT_API_URL:http://contract-service:8080}
      product-api-bash-path: ${PRODUCT_API_URL:http://product-service:8080}
      reference-master-data-api-bash-path: ${REFERENCE_API_URL:http://reference-service:8080}
```

### 4. Use in Controllers

```java
@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private FireflySessionManager sessionManager;

    @GetMapping("/{productId}/balance")
    @RequiresOwnership(resource = "product", paramIndex = 0, accessType = "read")
    public Mono<AccountBalance> getAccountBalance(
            @PathVariable UUID productId,
            ServerWebExchange exchange) {

        return sessionManager.createOrGetSession(exchange)
                .flatMap(session -> accountService.getBalance(productId, session));
    }

    @PostMapping("/{productId}/transfer")
    @RequiresOwnership(resource = "product", paramIndex = 0, accessType = "write")
    public Mono<TransferResult> transferMoney(
            @PathVariable UUID productId,
            @RequestBody TransferRequest request,
            ServerWebExchange exchange) {

        return sessionManager.createOrGetSession(exchange)
                .flatMap(session -> transferService.executeTransfer(productId, request, session));
    }
}
```
## 🎯 Deep Usage Scenarios

### **Scenario 1: Personal Banking Customer**

A customer accessing their personal checking account:

```java
// HTTP Request: GET /accounts/product-123/balance
// Headers: X-Party-Id: party-456

// Session Manager Flow:
// 1. Extract party-456 from X-Party-Id header
// 2. Fetch customer profile for party-456
// 3. Load active contracts where party-456 has roles
// 4. Find contract linking to product-123
// 5. Validate party-456 has OWNER role on product-123
// 6. Return cached session context

SessionContext session = sessionManager.createOrGetSession(exchange).block();
// session.getCustomerProfile() - Customer details
// session.getActiveContracts() - Contracts with roles
// session.getContractForProduct(productId) - Specific contract
```

### **Scenario 2: Corporate Banking Representative**

A corporate employee accessing company accounts:

```java
// HTTP Request: GET /accounts/corporate-product-789/transactions
// Headers: X-Party-Id: employee-party-123

// Session Manager Flow:
// 1. Extract employee-party-123 from X-Party-Id header
// 2. Fetch customer profile for employee-party-123
// 3. Load party relationships (employee acts on behalf of corporation)
// 4. Load contracts where corporation has products
// 5. Validate employee-party-123 has TRANSACTION_MANAGER role
// 6. Return session with corporate context

SessionContext session = sessionManager.createOrGetSession(exchange).block();
// session.getPartyRelationships() - Corporate relationships
// session.getActiveContracts() - Corporate contracts
// session.hasRole(productId, "TRANSACTION_MANAGER") - Role validation
```

### **Scenario 3: Joint Account Access**

Multiple parties accessing a shared account:

```java
// HTTP Request: GET /accounts/joint-product-456/details
// Headers: X-Party-Id: spouse-party-789

// Session Manager Flow:
// 1. Extract spouse-party-789 from X-Party-Id header
// 2. Fetch customer profile for spouse-party-789
// 3. Load contracts where spouse-party-789 has roles
// 4. Find joint contract for joint-product-456
// 5. Validate spouse-party-789 has JOINT_OWNER role
// 6. Return session with joint account access

SessionContext session = sessionManager.createOrGetSession(exchange).block();
ContractRole role = session.getRoleForProduct(productId);
// role.getRoleCode() == "JOINT_OWNER"
// role.getPriority() - Role priority for conflict resolution
```

## 🔐 Contract Role System

The library provides 22 comprehensive contract roles covering all banking scenarios:

### **Retail Banking (5 roles)**

| Role | Code | Description | Permissions |
|------|------|-------------|-------------|
| **Owner** | `OWNER` | Full control over personal accounts | All operations |
| **Joint Owner** | `JOINT_OWNER` | Shared ownership with limitations | Most operations, some restrictions |
| **Authorized User** | `AUTHORIZED_USER` | Transaction permissions | Transactions, no account changes |
| **Viewer** | `VIEWER` | Read-only access | View only |
| **Beneficiary** | `BENEFICIARY` | Receives benefits | Benefit access only |

### **Corporate Banking (5 roles)**

| Role | Code | Description | Permissions |
|------|------|-------------|-------------|
| **Account Administrator** | `ACCOUNT_ADMINISTRATOR` | Full corporate account control | All corporate operations |
| **Transaction Manager** | `TRANSACTION_MANAGER` | High-value transaction authority | Large transactions |
| **Approver** | `APPROVER` | Transaction approval authority | Approve transactions |
| **Initiator** | `INITIATOR` | Transaction initiation (requires approval) | Initiate transactions |
| **Inquiry User** | `INQUIRY_USER` | Read-only corporate access | View corporate data |

### **Lending (5 roles)**

| Role | Code | Description | Permissions |
|------|------|-------------|-------------|
| **Borrower** | `BORROWER` | Primary loan responsibility | Loan management |
| **Co-Borrower** | `CO_BORROWER` | Joint loan responsibility | Shared loan access |
| **Guarantor** | `GUARANTOR` | Loan guarantee provider | Guarantee management |
| **Loan Administrator** | `LOAN_ADMINISTRATOR` | Loan administration | Administrative operations |
| **Loan Viewer** | `LOAN_VIEWER` | Read-only loan access | View loan details |

### **Legal & Fiduciary (4 roles)**

| Role | Code | Description | Permissions |
|------|------|-------------|-------------|
| **Trustee** | `TRUSTEE` | Trust management authority | Trust operations |
| **Power of Attorney** | `POWER_OF_ATTORNEY` | Legal representative | Broad authority |
| **Guardian** | `GUARDIAN` | Legal guardian for minors/incapacitated | Guardian operations |
| **Executor** | `EXECUTOR` | Estate executor | Estate management |

### **Investment & Wealth Management (3 roles)**

| Role | Code | Description | Permissions |
|------|------|-------------|-------------|
| **Investment Manager** | `INVESTMENT_MANAGER` | Investment portfolio management | Investment operations |
| **Investment Advisor** | `INVESTMENT_ADVISOR` | Investment advisory services | Advisory access |
| **Investment Viewer** | `INVESTMENT_VIEWER` | Read-only investment access | View investments |

### **Custom Role Extension**

```java
// Add custom roles through Reference Master Data SDK
@Service
public class CustomRoleService {

    @Autowired
    private ContractRoleService contractRoleService;

    public Mono<List<ContractRole>> getAvailableRoles() {
        // Returns default roles + custom roles from Reference Master Data
        return contractRoleService.getAllAvailableRoles();
    }

    public Mono<ContractRole> createCustomRole(String roleCode, String description, int priority) {
        // Create custom role through Reference Master Data SDK
        return contractRoleService.createCustomRole(roleCode, description, priority);
    }
}
```

## 🌐 Integration with lib-common-domain

The Session Manager is designed for seamless integration with lib-common-domain's comprehensive CQRS framework, providing session context throughout the entire domain layer architecture.

### **Architecture Integration Overview**

The Session Manager operates as a **Core-Domain Layer** component that integrates with lib-common-domain's architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                    Channels Layer                           │
│              (Mobile, Web, API Gateway)                     │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│               Application/Process Layer                     │
│            (Business processes & workflows)                 │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│ ★                    Core-Domain Layer                    ★ │
│   ┌─────────────────────────────────────────────────────┐   │
│   │              lib-common-domain                      │   │
│   │  • CQRS Framework    • Domain Events                │   │
│   │  • ServiceClient     • Authorization System         │   │
│   │  • ExecutionContext  • Reactive Processing          │   │
│   └─────────────────────────────────────────────────────┘   │
│   ┌─────────────────────────────────────────────────────┐   │
│   │            Session Manager Library                  │   │
│   │  • Customer Context  • Contract Role Authorization  │   │
│   │  • Session Caching   • Product Access Validation    │   │
│   │  • Party Relationships • Banking Domain Logic       │   │
│   └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                  Core-Infrastructure Layer                  │
│              (Database CRUD & data persistence)             │
└─────────────────────────────────────────────────────────────┘
```

### **CQRS Framework Integration**

The Session Manager integrates with lib-common-domain's zero-boilerplate CQRS framework through **ExecutionContext** and automatic session injection:

#### **Command Handler Integration**

```java
@CommandHandlerComponent(timeout = 30000, retries = 3, metrics = true)
public class TransferMoneyHandler extends CommandHandler<TransferMoneyCommand, TransferResult> {

    @Autowired
    private FireflySessionManager sessionManager;

    @Override
    protected Mono<TransferResult> doHandle(TransferMoneyCommand command, ExecutionContext context) {
        // Extract session context from ExecutionContext
        String partyId = context.getUserId(); // X-Party-Id from request
        String sessionId = context.getSessionId();
        String correlationId = context.getRequestId();

        return sessionManager.getSessionByPartyId(UUID.fromString(partyId))
                .flatMap(session -> validateTransferPermissions(command, session, context))
                .flatMap(session -> executeTransfer(command, session, context))
                .flatMap(result -> publishTransferEvent(result, session, context));
    }

    private Mono<SessionContext> validateTransferPermissions(
            TransferMoneyCommand command, SessionContext session, ExecutionContext context) {

        // Validate customer has appropriate role for source product
        if (!session.hasRole(command.getSourceProductId(), "OWNER", "AUTHORIZED_USER")) {
            return Mono.error(new UnauthorizedAccessException(
                "Party " + session.getCustomerProfile().getPartyId() +
                " lacks sufficient permissions for product " + command.getSourceProductId()));
        }

        // Check feature flags from ExecutionContext
        boolean enhancedValidation = context.getFeatureFlag("enhanced-transfer-validation", false);
        if (enhancedValidation) {
            return validateEnhancedTransferLimits(command, session);
        }

        return Mono.just(session);
    }

    private Mono<TransferResult> executeTransfer(
            TransferMoneyCommand command, SessionContext session, ExecutionContext context) {

        // Use session context for service calls with proper headers
        return transferService.executeTransfer(command)
                .contextWrite(ctx -> ctx
                    .put("X-Party-Id", session.getCustomerProfile().getPartyId().toString())
                    .put("X-Session-Id", session.getSessionMetadata().getSessionId())
                    .put("X-Correlation-Id", context.getRequestId())
                    .put("X-Tenant-Id", context.getTenantId()));
    }

    // No getCommandType() needed - automatically detected from generics!
    // Built-in features: logging, metrics, error handling, correlation context
}
```

#### **Query Handler Integration with Session Context**

```java
@QueryHandlerComponent(cacheable = true, cacheTtl = 300, metrics = true)
public class GetAccountBalanceHandler extends QueryHandler<GetAccountBalanceQuery, AccountBalance> {

    @Autowired
    private FireflySessionManager sessionManager;

    @Override
    protected Mono<AccountBalance> doHandle(GetAccountBalanceQuery query, ExecutionContext context) {
        String partyId = context.getUserId();

        return sessionManager.getSessionByPartyId(UUID.fromString(partyId))
                .flatMap(session -> validateAccountAccess(query, session))
                .flatMap(session -> retrieveAccountBalance(query, session, context));
    }

    private Mono<SessionContext> validateAccountAccess(GetAccountBalanceQuery query, SessionContext session) {
        // Validate party has access to the requested product
        if (!session.hasAccessToProduct(query.getProductId())) {
            return Mono.error(new UnauthorizedAccessException(
                "Party does not have access to product " + query.getProductId()));
        }
        return Mono.just(session);
    }

    private Mono<AccountBalance> retrieveAccountBalance(
            GetAccountBalanceQuery query, SessionContext session, ExecutionContext context) {

        // Use session context to enrich the query with customer information
        return accountService.getBalance(query.getProductId())
                .map(balance -> enrichBalanceWithSessionContext(balance, session, context));
    }

    // ✅ NO BOILERPLATE NEEDED:
    // - No getQueryType() - automatically detected from generics!
    // - No supportsCaching() - handled by @QueryHandlerComponent annotation
    // - Built-in features: caching, logging, metrics, error handling
}
```

### **Authorization System Integration**

The Session Manager integrates with lib-common-domain's comprehensive authorization system, providing banking-grade security:

#### **Multi-Layer Authorization**

```java
// Command with integrated session-based authorization
public class TransferMoneyCommand implements Command<TransferResult> {

    @Override
    public Mono<AuthorizationResult> authorize(ExecutionContext context) {
        String partyId = context.getUserId();

        // Integration with Session Manager for banking-specific authorization
        return sessionManager.getSessionByPartyId(UUID.fromString(partyId))
                .flatMap(session -> validateTransferAuthorization(session, context))
                .onErrorReturn(AuthorizationResult.failure("session", "Unable to retrieve session context"));
    }

    private Mono<AuthorizationResult> validateTransferAuthorization(
            SessionContext session, ExecutionContext context) {

        // 1. Validate source account ownership through contract roles
        if (!session.hasRole(sourceProductId, "OWNER", "AUTHORIZED_USER")) {
            return Mono.just(AuthorizationResult.failure("sourceAccount",
                "Insufficient permissions for source account"));
        }

        // 2. Check transfer limits based on contract role
        ContractRole sourceRole = session.getRoleForProduct(sourceProductId);
        BigDecimal transferLimit = getTransferLimitForRole(sourceRole);

        if (amount.compareTo(transferLimit) > 0) {
            return Mono.just(AuthorizationResult.failure("amount",
                "Transfer amount exceeds limit for role " + sourceRole.getRoleCode()));
        }

        // 3. Validate destination account (if same customer)
        if (session.hasAccessToProduct(destinationProductId)) {
            if (!session.hasRole(destinationProductId, "OWNER", "JOINT_OWNER")) {
                return Mono.just(AuthorizationResult.failure("destinationAccount",
                    "Insufficient permissions for destination account"));
            }
        }

        // 4. Check feature flags for enhanced validation
        boolean fraudCheckRequired = context.getFeatureFlag("fraud-check-required", true);
        if (fraudCheckRequired && amount.compareTo(new BigDecimal("10000")) > 0) {
            return validateFraudCheck(session, context);
        }

        return Mono.just(AuthorizationResult.success());
    }
}
```

#### **Authorization Service Integration**

```java
@Service
public class SessionBasedAuthorizationService {

    @Autowired
    private FireflySessionManager sessionManager;

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Validates product access using session context and lib-common-domain authorization
     */
    public Mono<Boolean> validateProductAccess(UUID productId, ExecutionContext context) {
        String partyId = context.getUserId();

        return sessionManager.getSessionByPartyId(UUID.fromString(partyId))
                .map(session -> session.hasAccessToProduct(productId))
                .doOnNext(hasAccess -> {
                    if (!hasAccess) {
                        log.warn("Party {} denied access to product {}", partyId, productId);
                    }
                })
                .onErrorReturn(false);
    }

    /**
     * Validates contract role permissions for specific operations
     */
    public Mono<Boolean> validateRolePermissions(UUID productId, String operation, ExecutionContext context) {
        String partyId = context.getUserId();

        return sessionManager.getSessionByPartyId(UUID.fromString(partyId))
                .map(session -> {
                    ContractRole role = session.getRoleForProduct(productId);
                    return role != null && hasPermissionForOperation(role, operation);
                })
                .onErrorReturn(false);
    }

    private boolean hasPermissionForOperation(ContractRole role, String operation) {
        // Banking-specific role permission mapping
        return switch (operation) {
            case "READ" -> true; // All roles can read
            case "TRANSFER" -> Set.of("OWNER", "AUTHORIZED_USER", "TRANSACTION_MANAGER").contains(role.getRoleCode());
            case "ADMIN" -> Set.of("OWNER", "ACCOUNT_ADMINISTRATOR").contains(role.getRoleCode());
            default -> false;
        };
    }
}
```

### **Domain Events Integration**

The Session Manager integrates with lib-common-domain's multi-messaging event system for real-time session management:

#### **Session Lifecycle Events**

```java
@Component
public class SessionEventHandler {

    @Autowired
    private FireflySessionManager sessionManager;

    @Autowired
    private DomainEventPublisher eventPublisher;

    /**
     * Handle contract creation events to refresh session context
     */
    @EventListener
    public void handleContractCreated(DomainSpringEvent event) {
        if ("contract.created".equals(event.getEnvelope().getType())) {
            ContractCreatedEvent contractEvent = (ContractCreatedEvent) event.getEnvelope().getPayload();

            // Invalidate session cache for affected party to force refresh
            sessionManager.invalidateSession(contractEvent.getPartyId())
                    .doOnSuccess(v -> log.info("Session invalidated for party {} due to new contract {}",
                        contractEvent.getPartyId(), contractEvent.getContractId()))
                    .subscribe();
        }
    }

    /**
     * Handle role changes to update session permissions immediately
     */
    @EventListener
    public void handleRoleChanged(DomainSpringEvent event) {
        if ("contract.role.changed".equals(event.getEnvelope().getType())) {
            RoleChangedEvent roleEvent = (RoleChangedEvent) event.getEnvelope().getPayload();

            // Refresh session to reflect new permissions
            sessionManager.refreshSession(roleEvent.getPartyId())
                    .doOnSuccess(session -> {
                        log.info("Session refreshed for party {} due to role change on contract {}",
                            roleEvent.getPartyId(), roleEvent.getContractId());

                        // Publish session updated event
                        publishSessionUpdatedEvent(session);
                    })
                    .subscribe();
        }
    }

    /**
     * Handle customer profile updates
     */
    @EventListener
    public void handleCustomerProfileUpdated(DomainSpringEvent event) {
        if ("customer.profile.updated".equals(event.getEnvelope().getType())) {
            CustomerProfileUpdatedEvent profileEvent = (CustomerProfileUpdatedEvent) event.getEnvelope().getPayload();

            // Update session with new customer profile information
            sessionManager.updateCustomerProfile(profileEvent.getPartyId(), profileEvent.getUpdatedProfile())
                    .subscribe();
        }
    }

    private void publishSessionUpdatedEvent(SessionContext session) {
        SessionUpdatedEvent sessionEvent = SessionUpdatedEvent.builder()
                .sessionId(session.getSessionMetadata().getSessionId())
                .partyId(session.getCustomerProfile().getPartyId())
                .updatedAt(Instant.now())
                .activeContracts(session.getActiveContracts().size())
                .build();

        DomainEventEnvelope envelope = DomainEventEnvelope.builder()
                .topic("banking.sessions")
                .type("session.updated")
                .key(session.getCustomerProfile().getPartyId().toString())
                .payload(sessionEvent)
                .header("source", "session-manager")
                .header("version", "1.0")
                .build();

        eventPublisher.publish(envelope).subscribe();
    }
}
```

### **ServiceClient Framework Integration**

The Session Manager integrates with lib-common-domain's unified ServiceClient framework for session-aware service communication:

#### **Session-Aware Service Calls**

```java
@Service
public class AccountService {

    @Autowired
    private FireflySessionManager sessionManager;

    private final ServiceClient accountServiceClient;
    private final ServiceClient customerServiceClient;

    /**
     * Get account details with automatic session context injection
     */
    public Mono<AccountDetails> getAccountDetails(UUID productId, ExecutionContext context) {
        String partyId = context.getUserId();

        return sessionManager.getSessionByPartyId(UUID.fromString(partyId))
                .flatMap(session -> validateAccountAccess(productId, session))
                .flatMap(session -> retrieveAccountDetails(productId, session, context));
    }

    private Mono<AccountDetails> retrieveAccountDetails(
            UUID productId, SessionContext session, ExecutionContext context) {

        return accountServiceClient.get("/accounts/{productId}", AccountDetails.class)
                .withPathVariable("productId", productId)
                .withHeader("X-Party-Id", session.getCustomerProfile().getPartyId().toString())
                .withHeader("X-Session-Id", session.getSessionMetadata().getSessionId())
                .withHeader("X-Correlation-Id", context.getRequestId())
                .withHeader("X-Tenant-Id", context.getTenantId())
                .withQueryParam("includeBalance", "true")
                .withQueryParam("currency", session.getCustomerProfile().getPreferredCurrency())
                .execute()
                .map(details -> enrichAccountDetails(details, session));
    }

    /**
     * Batch account operations with session context
     */
    public Mono<List<AccountSummary>> getAccountSummaries(ExecutionContext context) {
        String partyId = context.getUserId();

        return sessionManager.getSessionByPartyId(UUID.fromString(partyId))
                .flatMap(session -> {
                    List<UUID> accessibleProducts = session.getActiveContracts().stream()
                            .flatMap(contract -> contract.getActiveProducts().stream())
                            .map(ActiveProduct::getProductId)
                            .toList();

                    // Batch request for all accessible products
                    return accountServiceClient.post("/accounts/batch-summary", List.class)
                            .withBody(BatchAccountRequest.builder()
                                .productIds(accessibleProducts)
                                .partyId(session.getCustomerProfile().getPartyId())
                                .includeBalances(true)
                                .build())
                            .withHeader("X-Party-Id", session.getCustomerProfile().getPartyId().toString())
                            .withHeader("X-Session-Id", session.getSessionMetadata().getSessionId())
                            .withHeader("X-Correlation-Id", context.getRequestId())
                            .execute()
                            .cast(List.class);
                });
    }

    private Mono<SessionContext> validateAccountAccess(UUID productId, SessionContext session) {
        if (!session.hasAccessToProduct(productId)) {
            return Mono.error(new UnauthorizedAccessException(
                "Party does not have access to product " + productId));
        }
        return Mono.just(session);
    }

    private AccountDetails enrichAccountDetails(AccountDetails details, SessionContext session) {
        // Enrich account details with session-specific information
        ContractRole role = session.getRoleForProduct(details.getProductId());

        return details.toBuilder()
                .customerRole(role != null ? role.getRoleCode() : "UNKNOWN")
                .accessLevel(determineAccessLevel(role))
                .lastAccessedAt(session.getSessionMetadata().getLastAccessTime())
                .build();
    }

    private String determineAccessLevel(ContractRole role) {
        if (role == null) return "NONE";

        return switch (role.getRoleCode()) {
            case "OWNER", "JOINT_OWNER" -> "FULL";
            case "AUTHORIZED_USER", "TRANSACTION_MANAGER" -> "TRANSACTIONAL";
            case "VIEW_ONLY", "BENEFICIARY" -> "READ_ONLY";
            default -> "LIMITED";
        };
    }
}
```

### **CQRS + Saga Integration**

The Session Manager integrates with lib-common-domain's saga orchestration capabilities for complex banking workflows:

#### **Session-Aware Saga Steps**

```java
@Component
@Saga(name = "account-opening")
@EnableTransactionalEngine
public class AccountOpeningSaga {

    @Autowired
    private FireflySessionManager sessionManager;

    @Autowired
    private CommandBus commandBus;

    @Autowired
    private QueryBus queryBus;

    /**
     * Step 1: Validate customer eligibility using session context
     */
    @SagaStep(id = "validate-customer", retry = 2)
    public Mono<CustomerValidation> validateCustomer(@Input AccountOpeningRequest request) {
        // Use session context for customer validation
        return sessionManager.getSessionByPartyId(request.getPartyId())
                .flatMap(session -> {
                    ValidateCustomerEligibilityQuery query = ValidateCustomerEligibilityQuery.builder()
                            .partyId(request.getPartyId())
                            .productType(request.getProductType())
                            .requestedCreditLimit(request.getCreditLimit())
                            .correlationId(request.getCorrelationId())
                            .build();

                    return queryBus.query(query)
                            .map(eligibility -> CustomerValidation.builder()
                                .partyId(request.getPartyId())
                                .isEligible(eligibility.isEligible())
                                .riskScore(eligibility.getRiskScore())
                                .sessionContext(session)
                                .build());
                });
    }

    /**
     * Step 2: Create account contract using CQRS command
     */
    @SagaStep(id = "create-contract",
              dependsOn = "validate-customer",
              compensate = "deleteContract")
    public Mono<ContractResult> createContract(
            @Input AccountOpeningRequest request,
            @FromStep("validate-customer") CustomerValidation validation) {

        if (!validation.isEligible()) {
            return Mono.error(new CustomerNotEligibleException("Customer not eligible for account opening"));
        }

        CreateContractCommand command = CreateContractCommand.builder()
                .partyId(request.getPartyId())
                .productType(request.getProductType())
                .initialRole("OWNER")
                .creditLimit(request.getCreditLimit())
                .correlationId(request.getCorrelationId())
                .build();

        return commandBus.send(command);
    }

    /**
     * Step 3: Update session cache with new contract
     */
    @SagaStep(id = "update-session", dependsOn = "create-contract")
    public Mono<SessionUpdateResult> updateSessionCache(
            @Input AccountOpeningRequest request,
            @FromStep("create-contract") ContractResult contract) {

        // Refresh session to include new contract and products
        return sessionManager.refreshSession(request.getPartyId())
                .map(updatedSession -> SessionUpdateResult.builder()
                        .sessionId(updatedSession.getSessionMetadata().getSessionId())
                        .partyId(request.getPartyId())
                        .newContractId(contract.getContractId())
                        .activeContractsCount(updatedSession.getActiveContracts().size())
                        .build());
    }

    /**
     * Compensation: Delete contract if saga fails
     */
    public Mono<Void> deleteContract(@FromStep("create-contract") ContractResult contract) {
        DeleteContractCommand command = DeleteContractCommand.builder()
                .contractId(contract.getContractId())
                .reason("SAGA_COMPENSATION")
                .build();

        return commandBus.send(command)
                .then(sessionManager.invalidateSession(contract.getPartyId()))
                .then();
    }
}
```

#### **Saga Orchestration with Session Context**

```java
@Service
public class BankingWorkflowService {

    @Autowired
    private SagaEngine sagaEngine;

    @Autowired
    private FireflySessionManager sessionManager;

    /**
     * Execute account opening workflow with session validation
     */
    public Mono<AccountOpeningResult> openAccount(AccountOpeningRequest request, ExecutionContext context) {
        String partyId = context.getUserId();

        // Validate session exists and customer is authenticated
        return sessionManager.getSessionByPartyId(UUID.fromString(partyId))
                .flatMap(session -> validateAccountOpeningPermissions(session, request))
                .flatMap(session -> executeAccountOpeningSaga(request, session))
                .map(this::buildAccountOpeningResult);
    }

    private Mono<SessionContext> validateAccountOpeningPermissions(
            SessionContext session, AccountOpeningRequest request) {

        // Check if customer can open additional accounts
        long currentAccountCount = session.getActiveContracts().stream()
                .flatMap(contract -> contract.getActiveProducts().stream())
                .filter(product -> "ACCOUNT".equals(product.getProductType()))
                .count();

        if (currentAccountCount >= 10) { // Business rule: max 10 accounts per customer
            return Mono.error(new MaxAccountLimitExceededException(
                "Customer has reached maximum account limit"));
        }

        return Mono.just(session);
    }

    private Mono<SagaResult> executeAccountOpeningSaga(
            AccountOpeningRequest request, SessionContext session) {

        // Enrich request with session context
        AccountOpeningRequest enrichedRequest = request.toBuilder()
                .partyId(session.getCustomerProfile().getPartyId())
                .customerTier(session.getCustomerProfile().getCustomerTier())
                .preferredCurrency(session.getCustomerProfile().getPreferredCurrency())
                .build();

        StepInputs inputs = StepInputs.of("validate-customer", enrichedRequest);

        return sagaEngine.execute("account-opening", inputs);
    }

    private AccountOpeningResult buildAccountOpeningResult(SagaResult sagaResult) {
        if (sagaResult.isSuccess()) {
            ContractResult contract = sagaResult.resultOf("create-contract", ContractResult.class)
                    .orElseThrow(() -> new IllegalStateException("Contract creation step not found"));

            SessionUpdateResult sessionUpdate = sagaResult.resultOf("update-session", SessionUpdateResult.class)
                    .orElseThrow(() -> new IllegalStateException("Session update step not found"));

            return AccountOpeningResult.builder()
                    .contractId(contract.getContractId())
                    .accountNumber(contract.getPrimaryProductId())
                    .status("OPENED")
                    .sessionId(sessionUpdate.getSessionId())
                    .openedAt(Instant.now())
                    .build();
        } else {
            return AccountOpeningResult.builder()
                    .status("FAILED")
                    .failedSteps(sagaResult.failedSteps())
                    .compensatedSteps(sagaResult.compensatedSteps())
                    .errorMessage("Account opening failed: " + String.join(", ", sagaResult.failedSteps()))
                    .build();
        }
    }
}
```

## 🔗 Integration with lib-common-auth

The Session Manager provides automatic access validators for lib-common-auth integration:

### **Automatic Validator Registration**

The library automatically registers access validators when lib-common-auth is present:

```java
// Automatically registered validators:
// - SessionBasedProductAccessValidator (resource = "product")
// - SessionBasedContractAccessValidator (resource = "contract")

@Configuration
@ConditionalOnClass(AccessValidatorRegistry.class)
public class SessionManagerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionBasedProductAccessValidator productAccessValidator(FireflySessionManager sessionManager) {
        return new SessionBasedProductAccessValidator(sessionManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionBasedContractAccessValidator contractAccessValidator(FireflySessionManager sessionManager) {
        return new SessionBasedContractAccessValidator(sessionManager);
    }
}
```

### **Access Validator Implementation**

```java
@AccessValidatorFor("product")
@Component
public class SessionBasedProductAccessValidator implements AccessValidator {

    private final FireflySessionManager sessionManager;

    @Override
    public String getResourceName() {
        return "product";
    }

    @Override
    public Mono<Boolean> canAccess(String resourceId, AuthInfo authInfo) {
        UUID productId = UUID.fromString(resourceId);
        UUID partyId = extractPartyId(authInfo);

        return sessionManager.getSessionByPartyId(partyId)
                .map(session -> session.hasAccessToProduct(productId))
                .onErrorReturn(false);
    }

    private UUID extractPartyId(AuthInfo authInfo) {
        // Extract party ID from auth info (X-Party-Id header)
        return authInfo.getHeaders().get("X-Party-Id")
                .map(UUID::fromString)
                .orElseThrow(() -> new IllegalArgumentException("X-Party-Id header is required"));
    }
}
```

### **Usage with @RequiresOwnership**

```java
@RestController
public class BankingController {

    // Product access validation (recommended)
    @GetMapping("/accounts/{productId}/balance")
    @RequiresOwnership(resource = "product", paramIndex = 0, accessType = "read")
    public Mono<AccountBalance> getAccountBalance(@PathVariable UUID productId) {
        // SessionBasedProductAccessValidator automatically validates:
        // 1. Extract party ID from X-Party-Id header
        // 2. Load session context for party
        // 3. Check if party has access to productId through contracts
        // 4. Validate role permissions for "read" access
        return accountService.getBalance(productId);
    }

    // Contract access validation (when needed)
    @GetMapping("/contracts/{contractId}/details")
    @RequiresOwnership(resource = "contract", paramIndex = 0, accessType = "read")
    public Mono<ContractDetails> getContractDetails(@PathVariable UUID contractId) {
        // SessionBasedContractAccessValidator automatically validates:
        // 1. Extract party ID from X-Party-Id header
        // 2. Load session context for party
        // 3. Check if party is participant in contractId
        // 4. Validate role permissions for "read" access
        return contractService.getDetails(contractId);
    }

    // Employee bypass for backoffice operations
    @PostMapping("/admin/accounts/{productId}/freeze")
    @RequiresOwnership(resource = "product", paramIndex = 0, accessType = "admin", bypassForBackoffice = true)
    public Mono<Void> freezeAccount(@PathVariable UUID productId) {
        // Employees with roles (ADMIN, CUSTOMER_SUPPORT, etc.) bypass validation
        // Regular customers go through normal product access validation
        return accountService.freezeAccount(productId);
    }
}
```

## ⚙️ Configuration

### **Cache Configuration**

Choose between Caffeine (in-memory) or Redis (distributed) caching:

#### **Caffeine Cache (Default)**
```yaml
firefly:
  session-manager:
    cache:
      type: caffeine
      caffeine:
        maximum-size: 10000      # Maximum cache entries
        expire-after-write: 30   # Minutes until expiration after write
        expire-after-access: 15  # Minutes until expiration after access
```

#### **Redis Cache (Production)**
```yaml
firefly:
  session-manager:
    cache:
      type: redis
      redis:
        ttl-minutes: 120
        key-prefix: "firefly:session:"
        connection:
          host: redis.example.com
          port: 6379
          database: 0
          password: ${REDIS_PASSWORD}
          username: ${REDIS_USERNAME}  # Redis 6+
          timeout: 2000
          ssl: true
```

### **API Client Configuration**

Configure connections to core banking services:

```yaml
firefly:
  session-manager:
    client:
      customer-api-bash-path: ${CUSTOMER_API_URL:http://customer-service:8080}
      contract-api-bash-path: ${CONTRACT_API_URL:http://contract-service:8080}
      product-api-bash-path: ${PRODUCT_API_URL:http://product-service:8080}
      reference-master-data-api-bash-path: ${REFERENCE_API_URL:http://reference-service:8080}
```

### **Environment-Specific Configuration**

#### **Development**
```yaml
spring:
  profiles:
    active: development

firefly:
  session-manager:
    cache:
      type: caffeine  # Fast local cache for development
    client:
      customer-api-bash-path: http://localhost:8081
      contract-api-bash-path: http://localhost:8082
      product-api-bash-path: http://localhost:8083
      reference-master-data-api-bash-path: http://localhost:8084

logging:
  level:
    com.firefly.common.auth.session: DEBUG
```

#### **Production**
```yaml
spring:
  profiles:
    active: production

firefly:
  session-manager:
    cache:
      type: redis  # Distributed cache for production
      redis:
        ttl-minutes: 120
        connection:
          host: ${REDIS_HOST}
          port: ${REDIS_PORT}
          database: ${REDIS_DATABASE:0}
          password: ${REDIS_PASSWORD}
          ssl: true
    client:
      customer-api-bash-path: ${CUSTOMER_API_URL}
      contract-api-bash-path: ${CONTRACT_API_URL}
      product-api-bash-path: ${PRODUCT_API_URL}
      reference-master-data-api-bash-path: ${REFERENCE_API_URL}

logging:
  level:
    com.firefly.common.auth.session: WARN
```

### **Complete Configuration Reference**

```yaml
firefly:
  session-manager:
    # Enable/disable session manager
    enabled: true

    # Session timeout configuration
    session-timeout-minutes: 30
    max-concurrent-sessions: 5

    # Cache configuration
    cache:
      type: caffeine  # caffeine or redis

      # Caffeine cache settings
      caffeine:
        maximum-size: 10000
        expire-after-write: 30
        expire-after-access: 15

      # Redis cache settings
      redis:
        ttl-minutes: 120
        key-prefix: "firefly:session:"
        connection:
          host: localhost
          port: 6379
          database: 0
          password: null
          username: null
          timeout: 2000
          ssl: false

    # API client configuration
    client:
      customer-api-bash-path: http://customer-service:8080
      contract-api-bash-path: http://contract-service:8080
      product-api-bash-path: http://product-service:8080
      reference-master-data-api-bash-path: http://reference-service:8080

# Spring Boot Actuator endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,sessions
  endpoint:
    health:
      show-details: always
```

## 🔧 Technology Stack

- **Java 21+** - Latest LTS with virtual threads and enhanced performance
- **Spring Boot 3.x** - Auto-configuration and reactive web stack
- **Spring WebFlux** - Reactive programming with Project Reactor
- **Resilience4j** - Circuit breakers and fault tolerance
- **Caffeine Cache** - High-performance in-memory caching
- **Redis** - Optional distributed caching support
- **lib-common-domain** - Domain layer integration and CQRS patterns
- **lib-common-auth** - Authentication and authorization framework
- **Micrometer** - Metrics and observability
- **Jackson** - JSON serialization with reactive support

### **Core Dependencies**

```xml
<!-- Session Manager -->
<dependency>
    <groupId>com.firefly</groupId>
    <artifactId>lib-session-manager</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- Automatically included transitive dependencies -->
<!-- lib-common-domain - Domain layer patterns -->
<!-- lib-common-auth - Authentication and authorization -->
<!-- Spring Boot WebFlux - Reactive web framework -->
<!-- Resilience4j - Circuit breakers and resilience -->
<!-- Caffeine - In-memory caching -->
```

### **Optional Dependencies**

```xml
<!-- Redis support (optional) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>

<!-- Metrics and monitoring (optional) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

## 📄 License

```
Copyright 2025 Firefly Software Solutions Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

**🏦 Ready to build secure, scalable banking microservices?**

The Firefly Session Manager provides everything you need for enterprise-grade customer context management and contract-based authorization in your core banking platform.

**Developed by Firefly Software Solutions Inc** - Building the future of open banking technology.

- **Website**: [getfirefly.io](https://getfirefly.io)
- **GitHub**: [firefly-oss](https://github.com/firefly-oss)
- **Documentation**: [docs.getfirefly.io](https://docs.getfirefly.io)

