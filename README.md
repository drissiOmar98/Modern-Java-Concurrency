# 🧵 Loom Virtual Threads Microservices Demo

> A hands-on Spring Boot playground for Java's **Project Loom** — Virtual Threads, Structured Concurrency, and Scoped Values — built around a realistic "loan application" microservices scenario.

![Java](https://img.shields.io/badge/Java-26-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?logo=springboot)
![Preview Features](https://img.shields.io/badge/Preview%20Features-enabled-blueviolet)


---

## 📚 Table of Contents

- [🧵 Loom Virtual Threads Microservices Demo](#-loom-virtual-threads-microservices-demo)
  - [🌱 Why This Project Exists](#-why-this-project-exists)
  - [🧠 A Quick Primer on Project Loom](#-a-quick-primer-on-project-loom)
    - [🚕 Virtual Threads: Taxis, Not Trains](#-virtual-threads-taxis-not-trains)
    - [🚫 Why We Stopped Pooling Threads](#-why-we-stopped-pooling-threads)
    - [🧩 Structured Concurrency: Code That Reads Like Logic](#-structured-concurrency-code-that-reads-like-logic)
    - [🔒 Scoped Values: A Safer ThreadLocal](#-scoped-values-a-safer-threadlocal)
  - [🏗️ Architecture](#️-architecture)
  - [📦 Project Structure](#-project-structure)
  - [🧵 Where Loom Shows Up in This Codebase](#-where-loom-shows-up-in-this-codebase)
  - [⚙️ Configuration](#️-configuration)
  - [🚀 Getting Started](#-getting-started)
    - [✅ Prerequisites](#-prerequisites)
    - [▶️ Running the Services](#️-running-the-services)
    - [🧪 Trying It Out](#-trying-it-out)
  - [🔌 API Reference](#-api-reference)
  - [🌿 Branches](#-branches)
  - [🐞 Observability Tip](#-observability-tip)
  - [🤝 Contributing](#-contributing)
  - [📬 Contact](#-contact)

---

## 🌱 Why This Project Exists

Concurrent programming has long had a reputation as *the* hard part of Java — juggling thread pools, wrestling with callback chains, and losing hours to unreadable thread dumps. **Project Loom** rethinks all of that from the ground up, letting you write simple, blocking-style code that scales like a reactive system under the hood.

This repo turns that theory into something you can actually run: two small Spring Boot microservices that simulate a bank processing a loan application, deliberately designed to show off each Loom feature in a context where it naturally belongs. 🏦

---

## 🧠 A Quick Primer on Project Loom

### 🚕 Virtual Threads: Taxis, Not Trains

Traditional Java threads map 1:1 onto OS kernel threads — think of them as **trains**: expensive to build, heavy to run, and wasteful to leave idling at a platform while waiting on I/O.

**Virtual threads** flip that model. They're **taxis** 🚕 — cheap to summon, used for exactly the trip you need, and released the moment you're done. Under the hood, the JVM "mounts" a virtual thread onto a real OS thread (a *carrier thread*) only while it's doing actual work, and "unmounts" it the instant it blocks on I/O — freeing the carrier to serve someone else. The result: you keep writing plain, sequential, easy-to-debug code, but get the throughput of a reactive system for free.

### 🚫 Why We Stopped Pooling Threads

Decades of expensive platform threads trained us to **pool** them carefully. With virtual threads, that instinct becomes an anti-pattern: they're cheap enough to create one per task and throw it away. Pooling virtual threads adds complexity for no benefit and can even choke the scalability Loom is designed to give you.

> 💡 **Rule of thumb:** if you catch yourself building an `ExecutorService` with a fixed pool of virtual threads "just to be safe" — don't. Spin up a new one per task instead.

### 🧩 Structured Concurrency: Code That Reads Like Logic

If virtual threads are the engine, **Structured Concurrency** is the steering wheel. Its core idea: whenever a task forks into subtasks, they must **rejoin in the same block of code** — no orphaned background threads left running after their parent has moved on or failed.

Using `StructuredTaskScope` in a `try`-with-resources block gives you, for free:
- 🛑 **Short-circuiting** — one subtask fails, the rest are cancelled automatically
- 📉 **Cancellation propagation** — a cancelled/timed-out parent instantly cancels its children
- 📖 **Clarity** — the code's shape mirrors the business logic's shape

A great example is racing two redundant services for the same answer (say, two credit bureaus) using the `anySuccessfulOrThrow` joiner: fork both calls, take whichever succeeds first, and the scope automatically cancels the loser. You'll find exactly this pattern in `CreditScoreService` below. 👇

### 🔒 Scoped Values: A Safer ThreadLocal

`ThreadLocal` has always come with sharp edges: unconstrained mutability, unbounded lifetime, and expensive value-copying to every child thread — a real liability once you're spawning threads by the millions.

**Scoped Values** fix this:

| | `ThreadLocal` | `ScopedValue` |
|---|---|---|
| Mutability | Read/write anywhere | Immutable within scope |
| Lifetime | Manual `.remove()` needed | Bounded automatically by scope |
| Inheritance | Deep-copied to children | Cheaply shared via `StructuredTaskScope` |
| Cleanup | Risk of leaks | Automatic on scope exit |

Bind a request ID as a `ScopedValue` once, and every subtask forked inside that scope can read it — safely, cheaply, with zero manual cleanup.

---

## 🏗️ Architecture

```
                 ┌───────────────────────────┐
   HTTP POST     │   loan-origination-service │        HTTP GET/POST
  /loan-        ─▶│         (port 8082)        │─┬────▶ /customer/{id}
  applications    │                            │ │
                 │  RequestContext (Scoped    │ ├────▶ /customer/{id}/accounts
                 │  Value: request ID)        │ │
                 │                            │ ├────▶ /customer/{id}/loans
                 │  StructuredTaskScope        │ │
                 │  fans out 3 concurrent      │ ├────▶ /customer/{id}/credit-score1  ─┐ raced with
                 │  calls, joins the results   │ └────▶ /customer/{id}/credit-score2  ─┘ anySuccessfulOrThrow
                 └───────────────┬────────────┘
                                 │ POST /customer/{id}/loans/offer
                                 ▼
                 ┌───────────────────────────┐
                 │    banking-data-service    │
                 │        (port 8081)         │
                 │  Simulated system of       │
                 │  record — dummy data +     │
                 │  configurable latency      │
                 └───────────────────────────┘
```

- **`loan-origination-service`** 🧮 — the orchestrator. Receives a loan application, fans out concurrent calls to `banking-data-service`, and calculates an offer.
- **`banking-data-service`** 🗄️ — a stand-in backend system of record, returning dummy data with a simulated processing delay so you can actually *see* concurrency at work.

---

## 📦 Project Structure

Both modules follow the same clean layout:

```
src/main/java/.../<service>/
├── config/       # @ConfigurationProperties + bean wiring
├── controller/   # REST endpoints
├── service/      # calls to banking-data-service (loan-origination-service only)
├── domain/       # request/response records
├── context/      # RequestContext (Scoped Values) — loan-origination-service only
└── exception/    # service-specific exceptions
src/main/resources/
└── application.yml
```

Every class, record, and non-trivial method carries a Javadoc explaining not just *what* it does but *why* — especially around the Loom-specific pieces.

---

## 🧵 Where Loom Shows Up in This Codebase

| Feature | Where | What it's doing |
|---|---|---|
| 🚕 Virtual Threads | `application.yml` (`spring.threads.virtual.enabled: true`) in both services | Every incoming HTTP request is served on a virtual thread |
| 🧩 Structured Concurrency | `LoanApplicationController.fetchCustomerInfo()` | Forks 3 concurrent calls (accounts, loans, credit score) and joins them as one unit |
| 🏁 Joiner (`anySuccessfulOrThrow`) | `CreditScoreService.getCreditScore()` | Races two credit-bureau endpoints, takes the first success, cancels the other |
| 🔒 Scoped Values | `RequestContext` | Binds a per-request correlation ID, automatically inherited by every forked subtask, for traceable logs |

---

## ⚙️ Configuration

Settings are typed, validated `@ConfigurationProperties` records — no scattered `@Value` fields.

**`loan-origination-service/application.yml`**
```yaml
banking-data-service:
  base-url: http://localhost:8081
```
Bound by `BankingDataServiceProperties` and used to build the shared `RestClient` bean.

**`banking-data-service/application.yml`**
```yaml
simulated-latency:
  min-seconds: 1
  max-seconds: 5
```
Bound by `SimulatedLatencyProperties` and used by `ServiceSimulator` to add a random delay to every endpoint — tune this down for fast local runs, or up to make concurrency effects more visible in a demo.

---

## 🚀 Getting Started

### ✅ Prerequisites

- JDK **26**
- `--enable-preview` VM argument (Structured Concurrency is still a preview API)

### ▶️ Running the Services

Start the data service first, then the orchestrator:

```bash
./mvnw -pl banking-data-service spring-boot:run
./mvnw -pl loan-origination-service spring-boot:run
```

### 🧪 Trying It Out

Submit a loan application:

```bash
curl -X POST http://localhost:8082/loan-applications \
  -H "Content-Type: application/json" \
  -d '{"customerId":"C1","amount":"5000","purpose":"home renovation"}'
```

Watch the logs — you'll see the accounts, loans, and credit-score calls kick off together and complete independently, all tagged with the same request ID. 🔍

Check that requests are riding on virtual threads:

```bash
curl http://localhost:8082/hello
```

---

## 🔌 API Reference

**`loan-origination-service` (port 8082)**

| Method | Path | Description |
|---|---|---|
| `GET` | `/hello` | Health check; shows the serving thread |
| `POST` | `/loan-applications` | Submit a loan application, returns a calculated `Offer` |

**`banking-data-service` (port 8081)**

| Method | Path | Description |
|---|---|---|
| `GET` | `/customer/{id}` | Dummy customer lookup |
| `GET` | `/customer/{id}/accounts` | Dummy account list |
| `GET` | `/customer/{id}/loans` | Dummy existing loans |
| `GET` | `/customer/{id}/credit-score1` | Dummy credit score (bureau 1) |
| `GET` | `/customer/{id}/credit-score2` | Dummy credit score (bureau 2) |
| `POST` | `/customer/{id}/loans/offer` | Calculates a dummy offer |

---

## 🌿 Branches

| Branch | What it shows |
|---|---|
| `main` | The baseline implementation (this README) |
| `with-structured-concurrency` | The loan application flow via the Structured Concurrency API |
| `with-completable-future` | The same use case via `CompletableFuture` — good for comparison |
| `with-scoped-values` | Another use case built around the Scoped Values API |
| `custom-joiners` | Examples of writing and using custom `Joiner`s |

---

## 🐞 Observability Tip

Modern debuggers (IntelliJ IDEA included) can render the parent/child hierarchy that `StructuredTaskScope` creates. Set a breakpoint inside `fetchCustomerInfo()` and pause execution — you'll see the request's virtual thread and exactly which subtasks (accounts, loans, credit score) it has spawned, instead of a flat, disconnected list of threads. Genuinely useful when hunting down a slow call in a larger microservices setup. 🕵️

---

## 🤝 Contributing

This is talk companion code, kept intentionally small and readable — issues and PRs that improve clarity or fix bugs are welcome. If you're extending it for your own experiments, the `config/` + `domain/` split should make it easy to add a third downstream service.

## 📬 Contact

**Omar Drissi**

- GitHub: [@drissiOmar98](https://github.com/drissiOmar98)
- LinkedIn: [omar-drissi](https://www.linkedin.com/in/omar-drissi-4798171a4/)
