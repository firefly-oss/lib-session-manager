# Firefly Session Manager Library - Architecture Documentation

## Table of Contents

- [Overview](#overview)
- [Architecture Diagram](#architecture-diagram)
- [Core Components](#core-components)
- [Package Structure](#package-structure)
- [Data Flow](#data-flow)
- [Caching Strategy](#caching-strategy)
- [Security & Access Control](#security--access-control)
- [Integration Points](#integration-points)
- [Configuration](#configuration)
- [Performance Considerations](#performance-considerations)

## Overview

The Firefly Session Manager Library is a comprehensive session management solution designed for the Firefly Core Banking Platform. It provides centralized customer session management, role-based access control, and intelligent caching across all microservices in the banking ecosystem.

### Key Features

- **Reactive Session Management**: Built on Spring WebFlux for high-performance, non-blocking operations
- **Dual-Layer Caching**: L1 (Caffeine) + L2 (Redis) for optimal performance and scalability
- **Role-Based Access Control**: Integration with lib-common-auth for fine-grained permissions
- **Banking Domain Models**: Native support for contracts, products, and customer relationships
- **Circuit Breaker Pattern**: Resilience4j integration for fault tolerance
- **Auto-Configuration**: Spring Boot auto-configuration for seamless integration

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           Firefly Session Manager                               │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐              │
│  │   Validation    │    │   Core Session  │    │   Configuration │              │
│  │     Layer       │    │    Manager      │    │     Layer       │              │
│  │                 │    │                 │    │                 │              │
│  │ • Product       │    │ • Session       │    │ • Auto Config   │              │
│  │   Validator     │    │   Lifecycle     │    │ • Cache Config  │              │
│  │ • Contract      │    │ • Context       │    │ • Properties    │              │
│  │   Validator     │    │   Extraction    │    │ • Client        │              │
│  └─────────────────┘    └─────────────────┘    │   Factory       │              │
│           │                       │             └─────────────────┘             │
│           └───────────┬───────────┘                       │                     │
│                       │                                   │                     │
│  ┌──────────────────────────────────────────────────────────────────────────────┤
│  │                        Service Layer                                         │
│  │                                                                              │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐               │
│  │  │ Customer Profile│  │   Contract      │  │    Product      │               │
│  │  │    Service      │  │   Service       │  │   Service       │               │
│  │  │                 │  │                 │  │                 │               │
│  │  │ • Profile Mgmt  │  │ • Contract Data │  │ • Product Data  │               │
│  │  │ • Relationships │  │ • Role Mapping  │  │ • Category Mgmt │               │
│  │  │ • Status Check  │  │ • Lifecycle     │  │ • Relationships │               │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘               │
│  └──────────────────────────────────────────────────────────────────────────────┤
│                                      │                                          │
│  ┌──────────────────────────────────────────────────────────────────────────────┤
│  │                        Caching Layer                                         │
│  │                                                                              │
│  │  ┌─────────────────┐                    ┌─────────────────┐                  │
│  │  │   L1 Cache      │                    │   L2 Cache      │                  │
│  │  │   (Caffeine)    │                    │   (Redis)       │                  │
│  │  │                 │                    │                 │                  │
│  │  │ • In-Memory     │◄──────────────────►│ • Distributed   │                  │
│  │  │ • Fast Access   │                    │ • Shared State  │                  │
│  │  │ • Local Node    │                    │ • Persistence   │                  │
│  │  └─────────────────┘                    └─────────────────┘                  │
│  └──────────────────────────────────────────────────────────────────────────────┤
│                                      │                                          │
│  ┌──────────────────────────────────────────────────────────────────────────────┤
│  │                      External Integration                                    │
│  │                                                                              │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐               │
│  │  │   Customer      │  │   Contract      │  │   Product       │               │
│  │  │   Management    │  │   Management    │  │   Management    │               │
│  │  │     SDK         │  │     SDK         │  │     SDK         │               │
│  │  │                 │  │                 │  │                 │               │
│  │  │ • Parties API   │  │ • Contracts API │  │ • Products API  │               │
│  │  │ • Natural       │  │ • Status        │  │ • Categories    │               │
│  │  │   Persons API   │  │   History API   │  │ • Relationships │               │
│  │  │ • Relationships │  │ • Parties API   │  │                 │               │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘               │
│  └──────────────────────────────────────────────────────────────────────────────┤
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Session Manager Core (`com.firefly.common.auth.session.core`)

#### FireflySessionManager
- **Purpose**: Central orchestrator for all session operations
- **Key Methods**:
  - `createSession(UUID partyId, ServerWebExchange exchange)`: Creates new customer session
  - `getCurrentSession()`: Retrieves current session context
  - `invalidateSession(String sessionId)`: Terminates session
  - `refreshSession(String sessionId)`: Updates session data
- **Features**: Reactive operations, caching integration, audit logging

#### SessionContextExtractor
- **Purpose**: Extracts session information from HTTP requests
- **Implementation**: `DefaultSessionContextExtractor`
- **Responsibilities**: Header parsing, party ID extraction, session validation

### 2. Service Layer (`com.firefly.common.auth.session.services`)

#### CustomerProfileService
- **Interface**: Defines customer profile operations
- **Implementation**: `CustomerProfileServiceImpl`
- **Key Features**:
  - Customer profile aggregation from multiple sources
  - Party relationship management
  - Circuit breaker and retry patterns
  - Intelligent caching with `@Cacheable` and `@CacheEvict`

#### ContractService
- **Interface**: Manages contract-related operations
- **Implementation**: `ContractServiceImpl`
- **Responsibilities**:
  - Active contract retrieval
  - Contract-party relationship mapping
  - Role-based access validation

#### ProductService
- **Interface**: Handles product management
- **Implementation**: `ProductServiceImpl`
- **Features**:
  - Product catalog integration
  - Product-contract relationships
  - Category and type management

### 3. Validation Layer (`com.firefly.common.auth.session.validation`)

#### SessionBasedProductAccessValidator
- **Purpose**: Validates access to banking products
- **Integration**: `@AccessValidatorFor("product")` annotation
- **Logic**: Checks if party has active contracts linking to requested products
- **Employee Bypass**: Supports role-based bypass for bank employees

#### SessionBasedContractAccessValidator
- **Purpose**: Validates access to contracts
- **Integration**: `@AccessValidatorFor("contract")` annotation
- **Logic**: Verifies party relationship to contracts
- **Security**: Prevents unauthorized contract access

## Package Structure

```
src/main/java/com/firefly/common/auth/session/
├── clients/                    # External API client factories
│   └── ClientsFactory.java    # SDK client bean configuration
├── config/                     # Configuration classes
│   ├── SessionManagerAutoConfiguration.java
│   ├── SessionManagerCacheConfiguration.java
│   ├── SessionManagerCacheProperties.java
│   └── SessionManagerClientFactoryProperties.java
├── core/                       # Core session management
│   ├── FireflySessionManager.java
│   ├── SessionContextExtractor.java
│   └── DefaultSessionContextExtractor.java
├── models/                     # Domain models
│   ├── SessionContext.java
│   ├── CustomerProfile.java
│   ├── ActiveContract.java
│   ├── ActiveProduct.java
│   ├── SessionMetadata.java
│   ├── SessionStatus.java
│   └── PartyRelationshipInfo.java
├── services/                   # Business logic interfaces
│   ├── CustomerProfileService.java
│   ├── ContractService.java
│   ├── ProductService.java
│   └── ContractRoleService.java
├── services/impl/              # Service implementations
│   ├── CustomerProfileServiceImpl.java
│   ├── ContractServiceImpl.java
│   ├── ProductServiceImpl.java
│   └── ContractRoleServiceImpl.java
└── validation/                 # Access validators
    ├── SessionBasedProductAccessValidator.java
    └── SessionBasedContractAccessValidator.java
```

## Data Flow

### Session Creation Flow

1. **Request Arrives**: HTTP request with `X-Party-Id` header
2. **Context Extraction**: `SessionContextExtractor` parses party information
3. **Profile Aggregation**: `CustomerProfileService` fetches customer data
4. **Contract Loading**: `ContractService` retrieves active contracts
5. **Product Mapping**: `ProductService` maps products to contracts
6. **Session Creation**: `FireflySessionManager` creates session context
7. **Caching**: Session stored in L1 (Caffeine) and L2 (Redis) caches
8. **Response**: Session context returned to calling service

### Access Validation Flow

1. **Resource Request**: Service requests access to product/contract
2. **Validator Selection**: Framework selects appropriate validator
3. **Session Retrieval**: Current session context fetched from cache
4. **Permission Check**: Validator checks party-resource relationship
5. **Employee Bypass**: Check for employee roles if applicable
6. **Access Decision**: Boolean result returned to framework
7. **Audit Logging**: Access attempt logged for security

## Caching Strategy

### Dual-Layer Architecture

The session manager implements a sophisticated dual-layer caching strategy:

#### L1 Cache (Caffeine)
- **Technology**: Ben Manes Caffeine (high-performance Java caching library)
- **Scope**: Local to each application instance
- **Configuration**:
  - Maximum Size: 10,000 entries (configurable)
  - Expire After Write: 30 minutes (configurable)
  - Expire After Access: 15 minutes (configurable)
- **Use Cases**: Frequently accessed session data, hot customer profiles

#### L2 Cache (Redis)
- **Technology**: Redis distributed cache
- **Scope**: Shared across all application instances
- **Configuration**:
  - TTL: 60 minutes (configurable)
  - Key Prefix: `firefly:session:` (configurable)
  - Serialization: JSON with Jackson
- **Use Cases**: Session persistence, cross-instance data sharing

### Cache Names and Usage

```java
// Defined in SessionManagerCacheConfiguration
public static final String SESSION_CONTEXT_CACHE = "sessionContext";
public static final String CUSTOMER_PROFILE_CACHE = "customerProfile";
public static final String CONTRACT_CACHE = "contractCache";
public static final String PRODUCT_CACHE = "productCache";
```

### Cache Eviction Strategy

- **Time-based**: Automatic expiration based on TTL settings
- **Manual**: `@CacheEvict` annotations for data updates
- **Size-based**: LRU eviction when cache reaches maximum size
- **Event-driven**: Cache invalidation on customer profile updates

## Security & Access Control

### Role-Based Access Control (RBAC)

The session manager integrates with `lib-common-auth` to provide comprehensive access control:

#### Access Validators

1. **Product Access Validator**
   - Validates access to banking products (accounts, loans, cards)
   - Checks party-contract-product relationships
   - Supports employee role bypass

2. **Contract Access Validator**
   - Validates access to contracts
   - Verifies party participation in contracts
   - Enforces contract status requirements

#### Employee Role Bypass

Certain roles can bypass ownership checks:
- `ADMIN`: Full system access
- `CUSTOMER_SUPPORT`: Customer assistance access
- `SUPERVISOR`: Supervisory access
- `MANAGER`: Management access
- `BRANCH_STAFF`: Branch operations access

### Security Features

- **Session Isolation**: Each session is isolated by party ID
- **Audit Logging**: All access attempts are logged
- **Secure Headers**: Session information extracted from secure headers
- **Timeout Management**: Automatic session expiration
- **Concurrent Session Limits**: Configurable maximum sessions per user

## Integration Points

### External SDKs

The session manager integrates with multiple Firefly platform SDKs:

#### Customer Management SDK
- **APIs Used**:
  - `PartiesApi`: Party information retrieval
  - `NaturalPersonsApi`: Individual customer data
  - `PartyRelationshipsApi`: Customer relationships
  - `PartyStatusesApi`: Customer status information
- **Purpose**: Customer profile aggregation and validation

#### Contract Management SDK
- **APIs Used**:
  - `ContractsApi`: Contract data retrieval
  - `ContractStatusHistoryApi`: Contract lifecycle tracking
- **Purpose**: Contract-party relationship management

#### Product Management SDK
- **APIs Used**:
  - `ProductApi`: Product information
  - `ProductCategoryApi`: Product categorization
  - `ProductRelationshipApi`: Product relationships
- **Purpose**: Product catalog integration and mapping

#### Reference Master Data SDK
- **APIs Used**:
  - `ContractRoleApi`: Contract role definitions
  - `ContractTypeApi`: Contract type information
- **Purpose**: Reference data for contract and role management

### Spring Framework Integration

- **Spring Boot Auto-Configuration**: Seamless integration with Spring Boot applications
- **Spring Cache Abstraction**: Leverages Spring's caching annotations
- **Spring WebFlux**: Reactive web framework support
- **Spring Security**: Integration with authentication and authorization

## Configuration

### Application Properties

The session manager supports extensive configuration through application properties:

```yaml
firefly:
  session-manager:
    enabled: true                           # Enable/disable session manager
    session-timeout-minutes: 30             # Session timeout
    max-concurrent-sessions: 5              # Max sessions per user

    cache:
      l1:                                   # L1 Cache (Caffeine)
        maximum-size: 10000                 # Maximum cache entries
        expire-after-write: 30              # Expire after write (minutes)
        expire-after-access: 15             # Expire after access (minutes)
      l2:                                   # L2 Cache (Redis)
        enabled: true                       # Enable Redis cache
        ttl-minutes: 60                     # TTL for cache entries
        key-prefix: "firefly:session:"     # Redis key prefix
        connection:                         # Redis connection settings
          host: localhost
          port: 6379
          database: 0
          timeout: 2000
          ssl: false

    client:                                 # External API configuration
      customer-api-bash-path: "http://customer-service"
      contract-api-bash-path: "http://contract-service"
      product-api-bash-path: "http://product-service"
      reference-master-data-api-bash-path: "http://reference-service"
```

### Auto-Configuration

The `SessionManagerAutoConfiguration` class provides automatic bean configuration:

- **Conditional Configuration**: Beans created only when required dependencies are present
- **Default Implementations**: Sensible defaults for all components
- **Customization Support**: Easy override of default implementations

## Performance Considerations

### Reactive Programming

- **Non-blocking I/O**: All operations use reactive streams (Mono/Flux)
- **Backpressure Handling**: Proper handling of downstream pressure
- **Resource Efficiency**: Minimal thread usage through event loops

### Caching Optimization

- **Cache Hit Ratio**: Dual-layer strategy maximizes cache hits
- **Memory Management**: Configurable cache sizes prevent memory issues
- **Network Efficiency**: L1 cache reduces network calls to Redis

### Circuit Breaker Pattern

- **Fault Tolerance**: Resilience4j integration for external service calls
- **Fallback Mechanisms**: Graceful degradation when services are unavailable
- **Recovery**: Automatic recovery when services become available

### Monitoring and Metrics

- **Cache Statistics**: Caffeine provides detailed cache metrics
- **Performance Logging**: Comprehensive logging for performance analysis
- **Health Checks**: Integration with Spring Boot Actuator

---

*This architecture documentation reflects the current implementation of the Firefly Session Manager Library. For implementation details and usage examples, see the [Developer Quick Start Guide](DEVELOPER_QUICK_START.md).*

