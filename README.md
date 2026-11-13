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
        MEM[("In-memory repo<br/>(demo data)")]
    end

    AI -- "MCP adapter · @McpTool" --> IN
    WEB -- "REST adapter · @RestController" --> IN
    OUT -- "memory adapter" --> MEM

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
│   ├── exception/              OrderNotFoundException
│   └── port/
│       ├── in/                 FindOrderUseCase, ListRecentOrdersUseCase
│       └── out/                OrderRepositoryPort
├── application/                OrderQueryService (no Spring annotations)
└── infrastructure/
    ├── adapter/
    │   ├── in/mcp/             OrderMcpTools, OrderView   ← AI entry door
    │   ├── in/rest/            OrderController            ← human entry door
    │   └── out/memory/         InMemoryOrderRepository
    └── config/                 UseCaseConfig
```

## 🚀 Run it

Requirements: **JDK 25**.

```bash
./gradlew bootRun
```

| Endpoint | What |
|---|---|
| `POST /mcp` | MCP server (Streamable HTTP) |
| `GET /api/orders/ORD-1042` | REST: one order |
| `GET /api/orders?customerId=CUS-1&limit=5` | REST: recent orders |
| `GET /actuator/prometheus` | Metrics: `mcp_tool_duration_seconds`, `mcp_tool_errors_total` |

Demo data: orders `ORD-1000` … `ORD-1059`, customers `CUS-1` … `CUS-6`.

### Connect an MCP client

Use the [MCP Inspector](https://github.com/modelcontextprotocol/inspector) with transport **Streamable HTTP** and URL `http://localhost:8080/mcp`, or add it to any MCP-compatible client.

## 🧪 Tests

```bash
./gradlew test
```

- **Unit tests** for the core and the MCP adapter (no Spring context).
- **ArchUnit rules** that keep the hexagon honest:
  - the domain is pure Java,
  - the core never depends on infrastructure,
  - only the MCP adapter knows about MCP,
  - inbound adapters talk to ports, not to the service.

## ⚡ Load test

```bash
k6 run k6/mcp-load-test.js
```

Ramps to 200 tool calls/second to simulate bursty agent traffic, with thresholds on error rate and p95 latency.

## 🛣️ Roadmap

- [ ] JPA/PostgreSQL driven adapter (the core does not change)
- [ ] OAuth 2.1 resource server + scopes per tool
- [ ] Grafana dashboard JSON
- [ ] Same server in **Go** with the official MCP Go SDK, with benchmarks

---

📝 Blog post: [cbuelvasc.hashnode.dev](https://cbuelvasc.hashnode.dev)
