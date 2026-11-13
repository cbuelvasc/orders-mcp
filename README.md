# orders-mcp ⬡🤖

> **An MCP server is just another driving adapter.** A hexagonal Spring Boot service that exposes the same use cases to humans (REST) and to AI agents (Model Context Protocol).

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6db33f)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0-6db33f)
![Gradle](https://img.shields.io/badge/Gradle-9.9%20Kotlin%20DSL-02303a)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-3b82f6)

| What | Choice |
|---|---|
| Language | Java 25 (LTS) |
| Framework | Spring Boot 4.1 · Spring Framework 7 · Spring AI 2.0 |
| MCP | `@McpTool` annotations · Streamable HTTP on `/mcp` |
| Build | Gradle 9.9 · Kotlin DSL · version catalog |
| Architecture | Hexagonal (Ports & Adapters), enforced with ArchUnit |
| Security | OAuth 2.1 resource server · scopes per tool · RFC 9728 |
| Persistence | PostgreSQL · JdbcClient · Flyway · Testcontainers |
| Observability | Micrometer → Prometheus → Grafana |
| Load testing | k6 |

---

## 🧩 Architecture

```mermaid
flowchart LR
    subgraph Driving["🤖 Driving side"]
        AI["AI Agent<br/>(Claude, Copilot, Cursor)"]
        WEB["Web / Mobile"]
    end

    subgraph Hexagon["⬡ Application core (pure Java)"]
        direction TB
        IN(["Inbound ports<br/>FindOrderUseCase<br/>ListRecentOrdersUseCase"])
        SVC["OrderQueryService"]
        DOM[("Domain<br/>Order · OrderId · OrderStatus")]
        OUT(["Outbound port<br/>OrderRepositoryPort"])
        IN --> SVC --> DOM
        SVC --> OUT
    end

    subgraph Driven["🔌 Driven side"]
        MEM[("PostgreSQL<br/>(Flyway)")]
    end

    AI -- "MCP adapter · @McpTool" --> IN
    WEB -- "REST adapter · @RestController" --> IN
    OUT -- "JDBC adapter" --> MEM

    style Hexagon fill:#eef6ff,stroke:#3b82f6,stroke-width:2px
    style AI fill:#fef3c7,stroke:#f59e0b
    style DOM fill:#dcfce7,stroke:#22c55e
```

## 🔄 A tool call, end to end

```mermaid
sequenceDiagram
    autonumber
    participant A as 🤖 AI Client
    participant M as OrderMcpTools<br/>(MCP adapter)
    participant U as FindOrderUseCase
    participant R as OrderRepositoryPort
    A->>M: initialize · tools/list
    M-->>A: findOrder, listRecentOrders (+ JSON schemas)
    A->>M: tools/call findOrder("ORD-1042")
    M->>U: findById(OrderId)
    U->>R: findById(OrderId)
    R-->>U: Order
    U-->>M: Order
    M-->>A: OrderView { status: "SHIPPED", total: 120.50 }
```

## 📁 Structure

```text
src/main/java/dev/cbuelvasc/orders/
├── domain/                     ← pure Java, no frameworks
│   ├── model/                  Order, OrderId, CustomerId, OrderStatus
│   ├── exception/              OrderNotFoundException, OrderCannotBeCancelledException
│   └── port/
│       ├── in/                 FindOrderUseCase, ListRecentOrdersUseCase, CancelOrderUseCase
│       └── out/                OrderRepositoryPort
├── application/                OrderQueryService, CancelOrderService
└── infrastructure/
    ├── adapter/
    │   ├── in/mcp/             OrderMcpTools, OrderView   ← AI entry door
    │   ├── in/rest/            OrderController            ← human entry door
    │   └── out/jdbc/           JdbcOrderRepository        ← PostgreSQL
    ├── security/               SecurityConfig, RFC 9728 metadata
    └── config/                 UseCaseConfig
```

## 🚀 Run it

Requirements: **JDK 25** and **Docker**.

```bash
./gradlew bootRun
```

`bootRun` starts the whole local stack from `compose.yaml` (thanks to `spring-boot-docker-compose`):

| Service | URL | What |
|---|---|---|
| orders-mcp | http://localhost:8080/mcp | MCP server (Streamable HTTP, OAuth 2.1 protected) |
| PostgreSQL | localhost:5432 | Orders, migrated and seeded by Flyway |
| Keycloak | http://localhost:8180 | Authorization server, realm `orders` (admin/admin) |
| Prometheus | http://localhost:9090 | Scrapes `/actuator/prometheus` |
| Grafana | http://localhost:3000 | Dashboard **MCP › Orders MCP · Tools** |

Demo data: orders `ORD-1000` … `ORD-1059`, customers `CUS-1` … `CUS-6`.

### Call it with a token

```bash
TOKEN=$(./scripts/get-token.sh orders-readonly orders:read)
curl -i http://localhost:8080/api/orders/ORD-1042 -H "Authorization: Bearer $TOKEN"

# Without a token the MCP endpoint answers 401 and tells the client where to look:
curl -i -X POST http://localhost:8080/mcp
# WWW-Authenticate: Bearer resource_metadata="http://localhost:8080/.well-known/oauth-protected-resource/mcp"
```

| Demo client | Secret | Scopes | Can call |
|---|---|---|---|
| `orders-readonly` | `orders-readonly-secret` | `orders:read` | `findOrder`, `listRecentOrders` |
| `orders-agent` | `orders-agent-secret` | `orders:read` + optional `orders:write` | all tools, including `cancelOrder` |

> Secrets and passwords in `compose.yaml` and the realm file are **local demo values**.

### Connect an MCP client

Use the [MCP Inspector](https://github.com/modelcontextprotocol/inspector) with transport **Streamable HTTP**, URL `http://localhost:8080/mcp` and a bearer token from the script above.

## 🔐 Security (part 2)

```mermaid
sequenceDiagram
    autonumber
    participant A as 🤖 MCP client
    participant M as orders-mcp
    participant K as 🔑 Keycloak
    A->>M: POST /mcp (no token)
    M-->>A: 401 + WWW-Authenticate: resource_metadata=...
    A->>M: GET /.well-known/oauth-protected-resource/mcp
    M-->>A: authorization_servers, scopes_supported
    A->>K: client_credentials, scope=orders:read
    K-->>A: access_token (JWT)
    A->>M: tools/call findOrder + Bearer token
    M->>M: verify JWT + @PreAuthorize(SCOPE_orders:read)
    M-->>A: result
```

| Tool | Required scope |
|---|---|
| `findOrder` | `orders:read` |
| `listRecentOrders` | `orders:read` |
| `cancelOrder` | `orders:write` |

## 🐘 Persistence (part 3)

The driven adapter is now `JdbcOrderRepository` (plain SQL with `JdbcClient` + Flyway). The core did not change. Both adapters, the in-memory fake and PostgreSQL, pass the **same contract test** (`OrderRepositoryContract`), the PostgreSQL one with **Testcontainers**.

## 📊 Observability (part 4)

Every tool call records `mcp.tool.duration` (histogram, p95/p99, SLO buckets) and `mcp.tool.errors`. Grafana is provisioned with a dashboard: calls/s, p95 by tool, error ratio, SLO under 100 ms, 401/403 on `/mcp` and JVM.

## 🧪 Tests

```bash
./gradlew test     # needs Docker for the Testcontainers tests
```

- **Unit tests** for the core and the MCP adapter (no Spring context).
- **Contract tests** for the outbound port: in-memory and PostgreSQL.
- **Integration tests** with Testcontainers: scopes per tool, 401 + resource metadata, Flyway seed.
- **ArchUnit rules** that keep the hexagon honest:
  - the domain is pure Java,
  - the core never depends on infrastructure,
  - only the MCP adapter knows about MCP,
  - only the JDBC adapter knows about SQL,
  - inbound adapters talk to ports, not to the service.

## ⚡ Load test

```bash
k6 run k6/mcp-load-test.js
```

Gets a real token with `client_credentials`, then ramps to 200 tool calls/second with thresholds on error rate and p95 latency per tool. Watch it live in Grafana.

## 🛣️ Roadmap

- [x] Part 1: hexagonal MCP server with Spring AI
- [x] Part 2: OAuth 2.1 resource server + scopes per tool
- [x] Part 3: PostgreSQL adapter, the core does not change
- [x] Part 4: Grafana dashboard + k6 with real tokens
- [ ] Part 5: the same server in **Go** with the official MCP Go SDK
- [ ] Part 6: Java vs Go benchmarks

## 👤 Author

**Carmelo Buelvas** · Senior Backend Engineer · Java, Go & AI (MCP)

[![LinkedIn](https://img.shields.io/badge/LinkedIn-cbuelvasc-0A66C2?logo=linkedin)](https://www.linkedin.com/in/cbuelvasc/)
[![Blog](https://img.shields.io/badge/Blog-cbuelvasc.hashnode.dev-2962FF?logo=hashnode)](https://cbuelvasc.hashnode.dev)
[![GitHub](https://img.shields.io/badge/GitHub-cbuelvasc-181717?logo=github)](https://github.com/cbuelvasc)
