// Each topic has levels: basic, medium, senior. Question: { q, a, code? }
window.QA = {
  spring: {
    title: "Java Spring Boot",
    levels: {
      basic: [
        { q: "What is Spring Boot and how does it differ from Spring Framework?",
          a: "Spring Boot is an opinionated layer on top of Spring that provides auto-configuration, starter dependencies, an embedded server (Tomcat by default) and production features (Actuator). Spring Framework gives you DI/AOP/MVC but requires explicit configuration; Boot removes most of that boilerplate." },
        { q: "What does @SpringBootApplication do?",
          a: "It combines `@Configuration`, `@EnableAutoConfiguration` and `@ComponentScan`. It marks the main config class, turns on auto-configuration based on the classpath, and scans the current package and sub-packages for beans.",
          code: "@SpringBootApplication\npublic class App {\n  public static void main(String[] args) {\n    SpringApplication.run(App.class, args);\n  }\n}" },
        { q: "What is dependency injection and how is it done in Spring?",
          a: "DI means objects receive their collaborators instead of creating them. Spring's IoC container creates and wires beans. Prefer constructor injection: it makes dependencies explicit, supports immutability (final fields) and eases testing.",
          code: "@Service\npublic class OrderService {\n  private final OrderRepository repo;\n  public OrderService(OrderRepository repo) { this.repo = repo; }\n}" },
        { q: "Difference between @Component, @Service, @Repository and @Controller?",
          a: "All are stereotypes that register a bean via component scanning. `@Service` and `@Controller` are semantic markers; `@Repository` additionally translates persistence exceptions into Spring's DataAccessException; `@RestController` = `@Controller` + `@ResponseBody`." },
        { q: "How do you configure an application (application.properties / yml, profiles)?",
          a: "Put settings in `application.properties` or `application.yml`. Use profile files like `application-dev.yml` and activate with `spring.profiles.active=dev`. Values are injected with `@Value` or, preferably, type-safe `@ConfigurationProperties`. Environment variables and command-line args override files." }
      ],
      medium: [
        { q: "Explain how auto-configuration works.",
          a: "Starters bring libraries onto the classpath. `@EnableAutoConfiguration` loads candidate classes listed in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Each uses conditions (`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`) so it only applies when relevant and backs off when you define your own bean. Use `--debug` to see the conditions report." },
        { q: "What are bean scopes and the bean lifecycle?",
          a: "Scopes: singleton (default), prototype, request, session, application. Lifecycle: instantiate → inject dependencies → `BeanPostProcessor` before-init → `@PostConstruct`/`InitializingBean` → after-init (proxies created here) → in use → `@PreDestroy`. Beware injecting a prototype/request bean into a singleton; use `ObjectProvider` or scoped proxies." },
        { q: "How does @Transactional work and what are common pitfalls?",
          a: "It's implemented with AOP proxies: the proxy opens a transaction, calls the method, commits or rolls back. Pitfalls: self-invocation bypasses the proxy; only public methods through the proxy are advised; by default only unchecked exceptions trigger rollback (use `rollbackFor`); wrong propagation (`REQUIRES_NEW`) or long transactions holding connections; lazy loading outside a transaction causes LazyInitializationException." },
        { q: "How do you do validation and global error handling in REST controllers?",
          a: "Annotate DTOs with Jakarta Bean Validation (`@NotBlank`, `@Email`) and add `@Valid` on the request body. Handle failures centrally with `@RestControllerAdvice` + `@ExceptionHandler`, returning a consistent error body (or RFC 7807 `ProblemDetail`).",
          code: "@RestControllerAdvice\nclass ApiErrors {\n  @ExceptionHandler(MethodArgumentNotValidException.class)\n  ProblemDetail invalid(MethodArgumentNotValidException ex) {\n    var pd = ProblemDetail.forStatus(400);\n    pd.setDetail(ex.getBindingResult().getFieldErrors().toString());\n    return pd;\n  }\n}" },
        { q: "What is the N+1 select problem in Spring Data JPA and how do you fix it?",
          a: "Loading N parents then lazily loading each child triggers 1 + N queries. Fix with `JOIN FETCH` in JPQL, `@EntityGraph`, batch fetching (`hibernate.default_batch_fetch_size`), or DTO projections. Detect it via SQL logging or tools like Hibernate statistics." }
      ],
      senior: [
        { q: "How would you design a Spring Boot service for high availability and observability?",
          a: "Stateless instances behind a load balancer, externalised config, health/readiness probes (Actuator), graceful shutdown (`server.shutdown=graceful`), connection-pool sizing (HikariCP), timeouts + retries + circuit breakers (Resilience4j), idempotent endpoints. Observability: Micrometer metrics → Prometheus, distributed tracing (OpenTelemetry) with trace IDs in structured logs, alerts on SLOs (latency, error rate, saturation)." },
        { q: "Virtual threads vs reactive (WebFlux): when would you choose each?",
          a: "Java 21 virtual threads (`spring.threads.virtual.enabled=true`) let you keep simple blocking code while scaling I/O-bound concurrency. WebFlux suits streaming, backpressure and fully non-blocking stacks, but is harder to write/debug. Choose virtual threads for most CRUD/blocking-JDBC services; WebFlux when you need streaming/backpressure end-to-end. Watch for thread pinning (synchronized + blocking) and unbounded concurrency hitting the DB pool." },
        { q: "How do you handle distributed transactions / consistency across microservices?",
          a: "Avoid 2PC. Use sagas (orchestrated or choreographed) with compensating actions, and the transactional outbox pattern so DB write and event publish are atomic (outbox table + relay/CDC such as Debezium). Make consumers idempotent (dedupe keys) and accept eventual consistency. Use optimistic locking (`@Version`) for concurrent updates." },
        { q: "How do you secure a Spring Boot API (Spring Security 6)?",
          a: "Configure a `SecurityFilterChain` bean (no WebSecurityConfigurerAdapter). Run as an OAuth2 resource server validating JWTs from an IdP (`spring-boot-starter-oauth2-resource-server`), stateless sessions, method security (`@PreAuthorize`), CORS configured explicitly, CSRF disabled only for stateless token APIs, secrets in a vault, least-privilege scopes, rate limiting, dependency scanning.",
          code: "@Bean\nSecurityFilterChain chain(HttpSecurity http) throws Exception {\n  return http\n    .csrf(c -> c.disable())\n    .authorizeHttpRequests(a -> a\n       .requestMatchers(\"/actuator/health\").permitAll()\n       .anyRequest().authenticated())\n    .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))\n    .build();\n}" },
        { q: "How do you diagnose and fix slow startup or memory/performance issues in production?",
          a: "Startup: check conditions report, lazy initialization, trim starters, AOT/CDS or GraalVM native image. Runtime: take heap dumps / JFR, inspect GC logs, look for leaks (unbounded caches, ThreadLocals), tune pools, check slow queries (`EXPLAIN`), add caching (Caffeine/Redis) with explicit TTL/invalidation, and load-test before and after changes." }
      ]
    }
  },

  graphql: {
    title: "GraphQL",
    levels: {
      basic: [
        { q: "What is GraphQL and how does it differ from REST?",
          a: "GraphQL is a query language and runtime with a typed schema and typically a single endpoint. Clients ask for exactly the fields they need, which avoids over/under-fetching and multiple round-trips. REST exposes many resource endpoints with fixed response shapes." },
        { q: "What are queries, mutations and subscriptions?",
          a: "Query: read data. Mutation: write/change data (top-level fields run serially). Subscription: long-lived stream of events, usually over WebSocket/SSE." },
        { q: "What is a schema and what are the scalar types?",
          a: "The schema (SDL) defines types and operations. Built-in scalars: `Int`, `Float`, `String`, `Boolean`, `ID`. `!` marks non-null, `[T]` a list. You can also define enums, input types, interfaces, unions and custom scalars (e.g. `DateTime`).",
          code: "type User {\n  id: ID!\n  name: String!\n  posts: [Post!]!\n}\ntype Query { user(id: ID!): User }" },
        { q: "What is a resolver?",
          a: "A function that produces the value for a field. Each field in the schema can have a resolver receiving `(parent, args, context, info)`. Fields without a resolver use the default property lookup on the parent object." }
      ],
      medium: [
        { q: "What is the N+1 problem in GraphQL and how does DataLoader solve it?",
          a: "Resolving `posts { author }` calls the author resolver once per post → N extra DB calls. DataLoader batches all keys requested in one tick into a single call (`WHERE id IN (...)`) and caches per request. In Spring GraphQL use `@BatchMapping` or a `DataLoader` registration.",
          code: "const userLoader = new DataLoader(async (ids) => {\n  const rows = await db.users.findMany({ where: { id: { in: ids } } });\n  return ids.map(id => rows.find(r => r.id === id));\n});" },
        { q: "Input types, variables, fragments and directives — what are they for?",
          a: "Variables parameterise operations safely (no string concatenation). Input types are structured arguments for mutations. Fragments reuse field selections (and, on the client, colocate data needs with components). Directives like `@include(if:)`/`@skip(if:)` conditionally include fields; custom directives add behaviour such as `@auth`." },
        { q: "How do you handle errors in GraphQL?",
          a: "Responses are HTTP 200 with `data` and an `errors` array (partial data is possible). Use non-null wisely: a null in a non-null field bubbles up to the nearest nullable parent. For expected business failures, model them in the schema (union result types like `CreateUserResult = User | ValidationError`); reserve `errors` for unexpected ones, with `extensions.code`." },
        { q: "How do you implement pagination?",
          a: "Offset/limit is simple but unstable on changing data. The Relay cursor connection spec (`edges { cursor node } pageInfo { hasNextPage endCursor }`) with `first/after` is stable and efficient when backed by keyset pagination.",
          code: "type Query {\n  posts(first: Int = 20, after: String): PostConnection!\n}" }
      ],
      senior: [
        { q: "How do you secure and protect a GraphQL API from abusive queries?",
          a: "Authentication in context; authorization at the resolver/domain layer (not only the edge). Limit query depth and complexity/cost, cap page sizes, timeouts, rate-limit by cost, disable introspection in public prod (or restrict), use persisted/allow-listed queries, validate inputs, and avoid leaking internals in error messages." },
        { q: "Explain schema evolution and versioning strategy.",
          a: "GraphQL favours continuous evolution over versions: add fields freely, mark old ones `@deprecated(reason:)`, track field usage via operation metrics/schema registry, and remove only after usage drops to zero. Avoid breaking changes (making a field non-null, removing enum values, changing arg types). Use schema checks in CI (e.g. GraphQL Inspector / Apollo schema checks)." },
        { q: "What is GraphQL Federation and when would you use it?",
          a: "Federation composes multiple subgraphs (owned by different teams/services) into one supergraph behind a router. Entities with `@key` can be extended across subgraphs. Use it when many teams own distinct domains and need a unified graph. Trade-offs: operational complexity, query-planning latency, cross-subgraph N+1 (solve with batched `_entities` resolvers). A simple BFF/monolith schema is better for small teams." },
        { q: "How do you cache in GraphQL and design for performance?",
          a: "HTTP caching is harder since queries are POSTs. Options: persisted queries with GET + CDN for public data, response caching with `@cacheControl` hints, per-request DataLoader caches, normalised client caches (Apollo/urql/Relay), and Redis for expensive resolvers. Use `@defer`/`@stream` for slow fields, and monitor resolver-level tracing to find hot spots." }
      ]
    }
  },

  react: {
    title: "React & TypeScript",
    levels: {
      basic: [
        { q: "What are components, props and state?",
          a: "Components are functions returning UI. Props are read-only inputs passed from a parent. State is data owned by a component that triggers a re-render when updated via `useState`.",
          code: "function Counter({ start }: { start: number }) {\n  const [n, setN] = useState(start);\n  return <button onClick={() => setN(n + 1)}>{n}</button>;\n}" },
        { q: "Why do lists need a `key` prop?",
          a: "Keys let React identify which items changed, were added or removed between renders. Use a stable unique id, not the array index when the list can reorder or change, otherwise state can be attached to the wrong item." },
        { q: "What's the difference between `interface` and `type` in TypeScript?",
          a: "Both describe object shapes. `interface` supports declaration merging and `extends`; `type` can also alias unions, intersections, tuples and mapped/conditional types. Prefer `interface` for public object contracts, `type` for unions and compositions; consistency matters most." },
        { q: "What does useEffect do and what is the dependency array?",
          a: "`useEffect` runs side effects (fetching, subscriptions, timers) after render. The dependency array controls when it re-runs: `[]` once on mount, `[a,b]` when a or b change, omitted every render. Return a cleanup function to unsubscribe/cancel." }
      ],
      medium: [
        { q: "When do you use useMemo, useCallback and React.memo?",
          a: "They're performance optimisations to preserve referential equality / skip work: `React.memo` skips re-rendering a component if props are shallow-equal; `useCallback` keeps a function identity stable (useful when passed to memoised children or effect deps); `useMemo` caches expensive computations. Don't add them by default — measure first (React Profiler)." },
        { q: "How do you type props, events and generic components in TypeScript?",
          a: "Declare a props type, use React's event types, and generics for reusable components.",
          code: "type ListProps<T> = {\n  items: T[];\n  render: (item: T) => React.ReactNode;\n};\nfunction List<T>({ items, render }: ListProps<T>) {\n  return <ul>{items.map((i, idx) => <li key={idx}>{render(i)}</li>)}</ul>;\n}\n\nconst onChange = (e: React.ChangeEvent<HTMLInputElement>) => e.target.value;" },
        { q: "Controlled vs uncontrolled components; how do you handle forms?",
          a: "Controlled: the input value lives in React state (`value` + `onChange`). Uncontrolled: the DOM holds the value, read via refs/FormData. For complex forms use React Hook Form with a schema validator (Zod) for performance and typed validation." },
        { q: "How do you manage server state vs client state?",
          a: "Server state (remote, cached, async) is best handled by TanStack Query / SWR / Apollo: caching, deduping, retries, invalidation. Client/UI state uses local state, Context, or small stores (Zustand/Redux Toolkit). Avoid copying server data into global stores." }
      ],
      senior: [
        { q: "Explain how React rendering works (reconciliation, concurrent features).",
          a: "State change → React re-renders the component tree into a virtual tree, diffs it with the previous (same type + key = update, else remount), then commits DOM changes. React 18 concurrent rendering can interrupt low-priority work: `startTransition`/`useDeferredValue` keep input responsive, Suspense coordinates loading states, and automatic batching groups updates. Render must be pure since it may run multiple times (StrictMode)." },
        { q: "How would you structure and scale a large React + TypeScript codebase?",
          a: "Feature-based folders with clear public APIs, strict TS (`strict`, `noUncheckedIndexedAccess`), shared design system, lint rules for import boundaries, API types generated from OpenAPI/GraphQL schema (graphql-codegen), colocated tests, error boundaries, route-level code splitting, CI checks (typecheck, lint, tests, bundle size budgets), and a monorepo (Nx/Turborepo) when multiple apps share code." },
        { q: "How do you optimise performance of a slow React app?",
          a: "Profile first (React DevTools Profiler, Lighthouse, Web Vitals). Then: code-split with `lazy`/Suspense, virtualise long lists (react-window), avoid unnecessary re-renders (state colocation, memoisation, stable context values / split contexts), debounce inputs, optimise images, use SSR/streaming or RSC (Next.js) for LCP, and reduce bundle size by analysing dependencies." },
        { q: "Advanced TypeScript: discriminated unions, generics constraints, utility types.",
          a: "Discriminated unions give exhaustive, safe state modelling; use `never` for exhaustiveness checks. Constraints (`T extends keyof U`) keep generics precise. Utility types: `Partial`, `Pick`, `Omit`, `Record`, `ReturnType`, `Awaited`. Prefer `unknown` over `any`, and validate external data at runtime (Zod) since types are erased.",
          code: "type State =\n  | { status: 'loading' }\n  | { status: 'error'; error: Error }\n  | { status: 'ok'; data: string[] };\n\nfunction view(s: State) {\n  switch (s.status) {\n    case 'loading': return 'Loading';\n    case 'error': return s.error.message;\n    case 'ok': return s.data.join();\n    default: { const _x: never = s; return _x; }\n  }\n}" }
      ]
    }
  },

  node: {
    title: "Node.js",
    levels: {
      basic: [
        { q: "What is Node.js and why is it fast for I/O?",
          a: "Node.js is a JavaScript runtime built on V8 with libuv. It uses a single-threaded event loop with non-blocking I/O, so one thread can handle many concurrent connections while I/O waits happen in the OS/thread pool. It's less suited to long CPU-bound work on the main thread." },
        { q: "CommonJS vs ES Modules?",
          a: "CommonJS uses `require`/`module.exports`, loaded synchronously. ESM uses `import`/`export`, is statically analysable, supports top-level await, and is enabled via `\"type\": \"module\"` or `.mjs`. ESM is the standard going forward." },
        { q: "Callbacks, Promises and async/await?",
          a: "They're three styles for async code. Callbacks can lead to nesting; Promises chain with `.then/.catch`; `async/await` gives synchronous-looking code with `try/catch`. Use `Promise.all` to run independent tasks concurrently.",
          code: "async function load() {\n  try {\n    const [a, b] = await Promise.all([fetch(u1), fetch(u2)]);\n    return [await a.json(), await b.json()];\n  } catch (e) { console.error(e); }\n}" },
        { q: "What is npm and package.json?",
          a: "npm is the package manager/registry. `package.json` holds metadata, scripts, and `dependencies` / `devDependencies`. The lockfile (`package-lock.json`) pins exact versions for reproducible installs (`npm ci`). Semver ranges: `^1.2.3` allows minor/patch updates." }
      ],
      medium: [
        { q: "Explain the event loop phases and microtasks.",
          a: "Phases: timers → pending callbacks → idle/prepare → poll (I/O) → check (`setImmediate`) → close callbacks. Between each callback, Node drains `process.nextTick` first, then promise microtasks. So `nextTick` and promise callbacks run before the next timer/I-O callback. Starving the loop with endless microtasks blocks I/O." },
        { q: "How do you build a REST API with Express and handle errors/middleware?",
          a: "Middleware are functions `(req,res,next)` run in order. Error middleware has four args and is registered last. In Express 5 async handler rejections are forwarded automatically; in Express 4 wrap them.",
          code: "const app = express();\napp.use(express.json());\napp.get('/users/:id', async (req, res) => {\n  const user = await repo.find(req.params.id);\n  if (!user) return res.status(404).json({ error: 'Not found' });\n  res.json(user);\n});\napp.use((err, req, res, next) => {\n  console.error(err);\n  res.status(500).json({ error: 'Internal error' });\n});" },
        { q: "What are streams and why use them?",
          a: "Streams process data in chunks (Readable, Writable, Duplex, Transform) with backpressure, so you can handle large files/responses without loading them in memory. Use `pipeline()` from `stream/promises` to wire them with proper error handling.",
          code: "import { pipeline } from 'node:stream/promises';\nawait pipeline(fs.createReadStream('in.log'), zlib.createGzip(), fs.createWriteStream('in.log.gz'));" },
        { q: "How do you manage configuration and secrets?",
          a: "Twelve-factor: config via environment variables (`process.env`, `--env-file`), validated at startup (Zod/envalid) so the app fails fast. Never commit secrets; use a secret manager (Vault, AWS Secrets Manager) and different values per environment." }
      ],
      senior: [
        { q: "How do you scale a Node.js application?",
          a: "Vertically via the `cluster` module / PM2 or, in containers, one process per container with horizontal scaling (Kubernetes HPA). Offload CPU-heavy work to `worker_threads` or job queues (BullMQ). Keep services stateless (sessions/cache in Redis), use a load balancer, tune keep-alive, and use caching and message brokers to decouple." },
        { q: "How do you find and fix memory leaks and event-loop blocking?",
          a: "Monitor heap and event-loop lag (`perf_hooks.monitorEventLoopDelay`). Capture heap snapshots (`--inspect`, Chrome DevTools, `v8.writeHeapSnapshot`) and compare them. Typical leaks: unbounded caches/maps, listeners never removed, closures holding large objects, unresolved promises. Blocking: heavy JSON parsing, sync fs/crypto, regex backtracking — use clinic.js / `--cpu-prof` and move work to workers." },
        { q: "How do you make a Node service resilient and production-ready?",
          a: "Graceful shutdown (handle SIGTERM: stop accepting, drain in-flight, close DB), health/readiness endpoints, structured logging (pino) with correlation IDs, timeouts + retries with backoff + circuit breakers, rate limiting, input validation, `unhandledRejection` handling that logs and exits so the orchestrator restarts, Helmet/security headers, dependency audits, OpenTelemetry tracing and metrics." },
        { q: "How do you design for security in a Node API?",
          a: "Validate and sanitise all input (schema validation), parameterised queries/ORM to prevent injection, hash passwords with argon2/bcrypt, short-lived JWTs + refresh rotation or server sessions with secure/HttpOnly/SameSite cookies, strict CORS, rate limiting, avoid prototype pollution and ReDoS, pin and audit dependencies (`npm audit`, lockfile, provenance), run as non-root with minimal container images." }
      ]
    }
  }
};
