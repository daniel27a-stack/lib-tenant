# lib-tenant

Spring Boot auto-configuration library generated with `java-skeleton` (Gradle + `java-library`
plugin, Spring Boot BOM imported as a `platform()`; it is a plain jar, not an executable one).

## What it is

`lib-tenant` is a library for servlet-based Spring Boot applications. A **tenant** is the customer
(organization) that owns the data of a request; it is not the user or the session. The library always
knows which tenant the current request belongs to and carries it between services, so services never
handle it by hand.

It does not write logs or touch the MDC (that belongs to `lib-logging`, which reads `TenantContext`),
it does not validate JWT or API keys (the `edge` does) and it does no roles or authorization. Its only
pirca dependency is `lib-errors`.

Adding the dependency to a servlet web application is enough to activate it: no `@Import` or
`@ComponentScan` is needed. `TenantAutoConfiguration` is listed in
`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
and is active only in servlet web applications. Every piece backs off when the application defines its
own bean of the same type.

## The tenant header

The tenant travels in the `X-Tenant-Id` header. A blank header counts as no tenant; a value is trimmed.

The header is **trusted, not validated** here. It is protected by the closed network: only the `edge`
is reachable from outside, and the `edge` is the one that authenticates and sets the header. Never expose
a service that uses this library directly.

## Reading the tenant

```java
Optional<String> tenant = TenantContext.current();   // empty when no tenant is set

TenantContext.runAs("acme", () -> {                  // outside a request: a plain thread, a scheduled task
    // TenantContext.current() returns "acme" here
});                                                  // and is no longer set after the block
```

`runAs` rejects a null or blank tenant with `IllegalArgumentException`.

## Inbound requests

- `TenantFilter` sets the tenant from `X-Tenant-Id` for the whole request. It never rejects: a request
  without the header proceeds with no tenant.
- Its order is `TenantFilter.ORDER`, which is `Ordered.HIGHEST_PRECEDENCE + 10`. A filter that needs the
  tenant, such as the one of `lib-logging`, must have a later (higher) order.
- `TenantRequiredInterceptor` is registered on `/api/**` only. A request to an `/api/**` route without a
  tenant fails with lib-errors' `BadRequestException` and code `TENANT_REQUIRED`, answered with a 400
  `application/problem+json` by `ErrorsControllerAdvice`; this library builds no response itself. Routes
  outside `/api/**` (health, Actuator) are answered without a tenant.
- The tenant follows the Spring MVC asynchronous dispatch of `Callable`, `CompletableFuture` and
  `DeferredResult` under `/api/**`: a valid request sees the tenant in the code that runs after the
  async work, as in a synchronous controller. Work that application code starts on its own threads is
  not covered; see Asynchronous work.

## Outbound calls

Every `RestClient.Builder` injected from Spring gets an interceptor that adds `X-Tenant-Id` with the current
tenant to each call, when one is set; with no tenant no header is added. This covers a `RestClient` and an
HTTP interface (`@HttpExchange`) built on it. It does **not** cover `RestTemplate`, Feign or reactive
(WebFlux) applications. Build the `RestClient` from the injected builder, not with `RestClient.create()`.

## Asynchronous work

`TenantTaskDecorator` copies the tenant to the thread that runs `@Async` methods and tasks of Spring Boot's
default executor (`applicationTaskExecutor`). Spring Boot applies a `TaskDecorator` only when exactly one
exists: an application that defines its own `TaskDecorator` replaces this one and must propagate the tenant
itself, and so must an executor you create by hand.

## Build and test

```bash
./gradlew build
```

## Test suites

| Suite      | Directory      | Run                  | Purpose                       |
|------------|----------------|----------------------|-------------------------------|
| `test`     | `src/test`     | `./gradlew test`     | Unit tests                    |
| `intTest`  | `src/intTest`  | `./gradlew intTest`  | Integration tests             |
| `archTest` | `src/archTest` | `./gradlew archTest` | Architecture tests (ArchUnit) |

`./gradlew check` runs the three suites, `test` first. Each suite can also run alone.

### Quality

`./gradlew build` also runs SpotBugs with FindSecBugs on the main code and checks JaCoCo line coverage
(minimum set by `minimumLineCoverage` in `gradle/quality.gradle`). Mutation testing is slow and runs only by hand:
`./gradlew pitest` (report in `build/reports/pitest/index.html`).

### Architecture tests

`ArchitectureTest` enforces the layered design `controller -> service -> repository | client`
(naming in both directions: a class in a layer package has that layer's suffix, and a
class with a layer's suffix lives in that layer's package; no upward calls, no `business`
package) and forbids package cycles.

Every layer rule is tagged `layer-<layer>` (`layer-controller`, `layer-service`,
`layer-repository`, `layer-client`). A layer this artifact does not have is excluded in
`gradle.properties`:

```properties
archTest.excludeLayers=controller,service,repository,client
```

Remove a layer from the list as soon as the artifact gets it. Unknown layer names fail the
build. A rule of a layer that is not excluded fails if the layer has no classes.

## Makefile shortcuts

The `Makefile` holds local shortcuts for the Gradle commands in this README. They are conveniences
for your machine: CI does not use them. Run `make` with no arguments to list them.

| Shortcut | Equivalent command |
|----------|--------------------|
| `make test`     | `./gradlew test`        |
| `make int`      | `./gradlew intTest`     |
| `make arch`     | `./gradlew archTest`    |
| `make build`    | `./gradlew build`       |
| `make me-happy` | `./gradlew clean build` |
| `make publish`       | `./gradlew publish`              |
| `make publish-local` | `./gradlew publishToMavenLocal`  |

## Publish

Local publishing (to consume it from another project on the same machine):

```bash
./gradlew publishToMavenLocal
```

Publish to GitHub Packages (repository `GitHubPackages` in `gradle/publish.gradle`, target URL set by
`pirca.registryUrl` in `gradle.properties`) with a token
that has the `write:packages` scope, supplied at build time and never stored in the project:

```bash
export GITHUB_ACTOR=<github-user>
export GITHUB_TOKEN=<token>
./gradlew publish
```

or set `gpr.user` and `gpr.token` in `~/.gradle/gradle.properties` (not the project one).

### Using other pirca libs

The `lib-*` libraries (`cl.pirca.lib.*`) are published to GitHub Packages, which asks for a token
even to read. `gradle/repositories.gradle` reads it at build time; it is never stored in the
project. A build that uses no pirca lib needs no token. The registry base URL is the single
`pirca.registryUrl` line in `gradle.properties`.

Locally, set a token with the `read:packages` scope in the environment:

```bash
export GITHUB_ACTOR=<github-user>
export GITHUB_TOKEN=<token>
```

or in `~/.gradle/gradle.properties` (not the project one): `gpr.user=<github-user>` and
`gpr.token=<token>`.

In a Docker build, supply the token as a BuildKit secret:

```bash
docker build --secret id=github_actor,env=GITHUB_ACTOR --secret id=github_token,env=GITHUB_TOKEN .
```

This project has no Dockerfile. A service generated by java-skeleton already reads both secrets in
its `ci/docker/Dockerfile`.

Never pass the token as `ARG` or `ENV`: it would stay in the image layers and history. A secret
mount is available only during that `RUN` and is not stored in the image.
