# Creo OTEL Spring Boot Starter

[![CI](https://github.com/kakavi/creo-otel-spring-boot-starter/actions/workflows/ci.yml/badge.svg)](https://github.com/kakavi/creo-otel-spring-boot-starter/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-25%2B-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen)](https://spring.io/projects/spring-boot)

A lightweight Spring Boot starter that auto-configures OpenTelemetry + Micrometer conveniences and servlet filters to improve observability and log correlation.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
  - [Starter properties](#starter-properties)
  - [Logback configuration](#logback-configuration)
  - [OTLP endpoint configuration](#otlp-endpoint-configuration)
- [Auto-Configuration Details](#auto-configuration-details)
- [Security Notes](#security-notes)
- [Troubleshooting](#troubleshooting)
- [Building from Source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Context Propagation** — Async task context propagation using `ContextPropagatingTaskDecorator` (enabled by default).
- **OpenTelemetry Integration**:
    - Automatic installation of `opentelemetry-logback-appender` at runtime (when an `OpenTelemetry` bean is present).
    - JVM/system metrics published with OpenTelemetry semantic conventions.
    - `OpenTelemetryServerRequestObservationConvention` registration.
- **Servlet Filters** (servlet web apps only, **opt-in**):
    - `HeaderLoggerFilter` — Logs incoming HTTP request headers at DEBUG level, **with sensitive headers redacted**.
    - `AddTraceIdFilter` — Adds a configurable trace-id response header (default `X-Trace-Id`) using Micrometer Tracing.

> **Secure by default:** request-header logging and the trace-id response header are **disabled by default** because both can disclose sensitive information. Enable them explicitly via [configuration](#starter-properties).

## Requirements

| Requirement | Version |
|-------------|---------|
| Java | 25+ |
| Spring Boot | 4.x |
| Logging | Logback (via `spring-boot-starter-logging`) or any SLF4J implementation |

## Installation

### Maven Coordinates

```
Group:    com.creotech
Artifact: creo-otel-spring-boot-starter
Version:  0.0.1-SNAPSHOT
```

### Gradle (Groovy)

```groovy
repositories {
    mavenCentral()
    // Repositories hosting this starter
    maven { url "https://repo.myxeno.com/m2/releases" }
    maven { url "https://repo.myxeno.com/m2/snapshots" }
}

dependencies {
    implementation 'com.creotech:creo-otel-spring-boot-starter:0.0.1-SNAPSHOT'
}
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven(url = "https://repo.myxeno.com/m2/releases")
    maven(url = "https://repo.myxeno.com/m2/snapshots")
}

dependencies {
    implementation("com.creotech:creo-otel-spring-boot-starter:0.0.1-SNAPSHOT")
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>myxeno-releases</id>
        <url>https://repo.myxeno.com/m2/releases</url>
    </repository>
    <repository>
        <id>myxeno-snapshots</id>
        <url>https://repo.myxeno.com/m2/snapshots</url>
    </repository>
</repositories>

<!-- Then add the dependency -->
<dependency>
    <groupId>com.creotech</groupId>
    <artifactId>creo-otel-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Quick Start

1. **Add the dependency** (see [Installation](#installation)).

2. **Ensure logging is present.** Spring Boot applications typically include `spring-boot-starter-logging` (Logback) by default.

3. **Run your application:**

   ```bash
   ./gradlew bootRun
   ```

4. **(Optional) Expose the trace ID** by enabling the filter:

   ```properties
   creo.otel.trace-id.enabled=true
   ```

   Then make a request and observe the trace ID header:

   ```bash
   curl -i http://localhost:8080/your-endpoint
   ```

   ```
   X-Trace-Id: 0dbe0809731e35081d6db16c2ca0ef91
   ```

## Configuration

### Starter properties

All starter behavior is controlled under the `creo.otel.*` prefix. The
`spring-boot-configuration-processor` ships metadata with the jar, so these keys
auto-complete in IDEs.

| Property | Default | Description |
|----------|---------|-------------|
| `creo.otel.context-propagation.enabled` | `true` | Register a `ContextPropagatingTaskDecorator`. Backs off if you define your own. |
| `creo.otel.metrics.enabled` | `true` | Register JVM/system metrics binders that use OpenTelemetry semantic conventions. |
| `creo.otel.header-logging.enabled` | `false` | Register the request-header logging filter (logs at DEBUG). |
| `creo.otel.header-logging.redacted-headers` | `authorization, proxy-authorization, cookie, set-cookie, x-api-key, api-key, x-auth-token, x-csrf-token` | Header names (case-insensitive) whose values are replaced with `***` before logging. |
| `creo.otel.trace-id.enabled` | `false` | Add the trace-id response header. |
| `creo.otel.trace-id.header-name` | `X-Trace-Id` | Name of the trace-id response header. |

Example (`application.yml`):

```yaml
creo:
  otel:
    trace-id:
      enabled: true
      header-name: X-Trace-Id
    header-logging:
      enabled: false        # keep off in production unless you need it
      redacted-headers:
        - authorization
        - cookie
        - x-api-key
```

To enable header logging you must also raise the log level for the filter:

```properties
creo.otel.header-logging.enabled=true
logging.level.com.creotech.starter.autoconfigure.HeaderLoggerFilter=DEBUG
```

You can also disable an entire auto-configuration with Spring Boot's standard property:

```properties
spring.autoconfigure.exclude=\
  com.creotech.starter.autoconfigure.FilterConfiguration,\
  com.creotech.starter.autoconfigure.OpenTelemetryConfiguration
```

### Logback configuration

Create `src/main/resources/logback-spring.xml` to enable OpenTelemetry log export:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <include resource="org/springframework/boot/logging/logback/base.xml"/>

    <appender name="OpenTelemetry"
              class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender">
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="OpenTelemetry"/>
    </root>

</configuration>
```

The OpenTelemetry appender is installed programmatically when both an `OpenTelemetry`
bean and `opentelemetry-logback-appender-1.0` are on the classpath.

### OTLP endpoint configuration

**Option A: Docker Compose (Development).** If you use `spring-boot-docker-compose`
with Grafana LGTM, endpoints are auto-configured:

```groovy
dependencies {
    developmentOnly 'org.springframework.boot:spring-boot-docker-compose'
}
```

```yaml
# compose.yaml
services:
  lgtm:
    image: grafana/otel-lgtm
    ports:
      - "3000:3000"   # Grafana UI
      - "4317:4317"   # OTLP gRPC
      - "4318:4318"   # OTLP HTTP
```

> No endpoint configuration needed — Spring Boot auto-detects the container.

**Option B: Manual Configuration (Production).** Configure endpoints explicitly:

```properties
# Application name (used as service.name in telemetry)
spring.application.name=your-service-name

# OTLP exporters
management.otlp.metrics.export.url=http://localhost:4318/v1/metrics
management.opentelemetry.tracing.export.otlp.endpoint=http://localhost:4318/v1/traces
management.opentelemetry.logging.export.otlp.endpoint=http://localhost:4318/v1/logs

# Sampling — sample less than 100% in production (e.g. 0.1)
management.tracing.sampling.probability=0.1
# Metrics export interval — 1m or higher in production
management.otlp.metrics.export.step=60s
```

> Adjust the OTLP endpoints to match your observability backend (Grafana LGTM, Jaeger, or an OpenTelemetry Collector).

## Auto-Configuration Details

| Configuration Class | Description |
|---------------------|-------------|
| `ContextPropagationConfiguration` | Registers `ContextPropagatingTaskDecorator` for async context propagation. |
| `FilterConfiguration` | Registers `HeaderLoggerFilter` and `AddTraceIdFilter` (servlet web apps only, opt-in). |
| `OpenTelemetryConfiguration` | Installs the Logback OTel appender, registers JVM/system metrics with OTel conventions, and the server-request observation convention. |

**Compatibility notes:**

- Requires Micrometer Core and Micrometer Tracing APIs.
- The consuming application typically provides the OpenTelemetry SDK / OTLP exporter and the `OpenTelemetry` bean.
- Servlet filters only activate in servlet-based applications (not reactive/WebFlux).
- `OpenTelemetryConfiguration` is ordered before Actuator's JVM/system metrics auto-configurations and uses `@ConditionalOnMissingBean`, so application-defined binders always win.

## Security Notes

- **Header logging is off by default** and redacts credential-bearing headers when enabled. Because this starter also installs the OTel log appender, anything logged can be exported to your backend — keep the redaction list current and avoid enabling header logging in production unless necessary.
- **The trace-id response header is off by default.** Exposing internal trace IDs to arbitrary clients is an information-disclosure decision; enable it only for internal services or behind a trusted gateway.

## Troubleshooting

The starter is quiet by default. It logs an INFO line when the OpenTelemetry Logback
appender is installed (and a WARN — without failing startup — if installation fails),
and emits DEBUG lines describing what it wired up. To see the latter:

```properties
logging.level.com.creotech.starter.autoconfigure=DEBUG
```

**`X-Trace-Id` header not appearing in responses**

- Set `creo.otel.trace-id.enabled=true`.
- Verify Micrometer Tracing is on the classpath and a `Tracer` bean exists.
- Ensure you're using servlet-based Spring MVC (not WebFlux).
- Check that `FilterConfiguration` is not excluded.
- To diagnose filter ordering, enable per-request TRACE logging — it reports when no trace context was available: `logging.level.com.creotech.starter.autoconfigure.AddTraceIdFilter=TRACE`.

**Request headers not being logged**

- Set `creo.otel.header-logging.enabled=true`.
- Raise the log level: `logging.level.com.creotech.starter.autoconfigure.HeaderLoggerFilter=DEBUG`.
- Verify your application uses Logback or another SLF4J implementation.

**Duplicate JVM/system metrics**

- The starter replaces Actuator's default binders with OTel-convention ones. If you define your own `ProcessorMetrics`/`JvmMemoryMetrics`/etc. beans, those win and the starter backs off. Set `creo.otel.metrics.enabled=false` to opt out entirely.

## Building from Source

```bash
# Clone the repository
git clone https://github.com/kakavi/creo-otel-spring-boot-starter.git
cd creo-otel-spring-boot-starter

# Build and test
./gradlew build

# Run tests only
./gradlew test

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

> Publishing to the remote SFTP repository requires `mavenU` and `mavenP` Gradle properties (e.g. in `~/.gradle/gradle.properties`). When they are absent the remote repository is simply not registered, so local builds and `publishToMavenLocal` work without credentials.

## Contributing

Contributions are welcome! 

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/amazing-feature`).
3. Commit your changes (`git commit -m 'feat: add amazing feature'`).
4. Push to the branch (`git push origin feature/amazing-feature`).
5. Open a Pull Request.

### Reporting Issues

Please include:
- Java and Spring Boot versions
- Steps to reproduce
- Expected vs actual behavior
- Relevant logs or stack traces

### Security

Please do not file security issues publicly. Contact the maintainers privately if you suspect a vulnerability.

## Versioning

This project uses [Semantic Versioning](https://semver.org/). SNAPSHOT versions are subject to change.

## License

This project is licensed under the Apache License, Version 2.0 — see the [LICENSE](LICENSE) file for details.

---

Made with ❤️ by [Creotech](https://github.com/kakavi)
