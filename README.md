# Creo OTEL Spring Boot Starter

[![CI](https://github.com/kakavi/creo-otel-spring-boot-starter/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/creo-otel-spring-boot-starter/actions/workflows/ci.yml)
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
- [Auto-Configuration Details](#auto-configuration-details)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Context Propagation** — Async task context propagation using `ContextPropagatingTaskDecorator`
- **Servlet Filters** (servlet web apps only):
    - `HeaderLoggerFilter` — Logs incoming HTTP request headers at DEBUG level
    - `AddTraceIdFilter` — Adds `X-Trace-Id` header to responses using Micrometer Tracing
- **OpenTelemetry Integration**:
    - Automatic installation of `opentelemetry-logback-appender` at runtime
    - JVM/system metrics with OpenTelemetry conventions
    - `OpenTelemetryServerRequestObservationConvention` registration

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
    mavenLocal() // Optional: useful when working with local SNAPSHOTs
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
    mavenLocal() // Optional: useful when working with local SNAPSHOTs
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

1. **Add the dependency** (see [Installation](#installation))

2. **Ensure logging is present**

   Spring Boot applications typically include `spring-boot-starter-logging` (Logback) by default.

3. **Run your application**

   ```bash
   ./gradlew bootRun
   ```

4. **Make a request and observe the trace ID**

   ```bash
   curl -i http://localhost:8080/your-endpoint
   ```

   Response headers will include:
   ```
   X-Trace-Id: 0dbe0809731e35081d6db16c2ca0ef91
   ```

## Configuration
### Required Setup

#### 1. Logback Configuration

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

#### 2. OTLP Endpoint Configuration

**Option A: Using Docker Compose (Development)**

If you use `spring-boot-docker-compose` with Grafana LGTM, endpoints are auto-configured:
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

> No endpoint configuration needed — Spring Boot auto-detects the container!


**Option B: Manual Configuration (Production)**

For production or external collectors, configure endpoints explicitly:
Add the following to your `application.properties` or `application.yml`:

**application.properties**
```properties
# Application name (used as service.name in telemetry)
spring.application.name=your-service-name

# OTLP Metrics export
management.otlp.metrics.export.url=http://localhost:4318/v1/metrics

# OTLP Traces export
management.opentelemetry.tracing.export.otlp.endpoint=http://localhost:4318/v1/traces

# OTLP Logs export
management.opentelemetry.logging.export.otlp.endpoint=http://localhost:4318/v1/logs

# Sample 100% of traces (adjust for production e.g to 0.1)
management.tracing.sampling.probability=1.0
# Metrics export interval in production set to 1m or higher
management.otlp.metrics.export.step=10s
```

**application.yml**
```yaml
spring:
  application:
    name: your-service-name

management:
  otlp:
    metrics:
      export:
        url: http://localhost:4318/v1/metrics
        step: 60s
  opentelemetry:
    tracing:
      export:
        otlp:
          endpoint: http://localhost:4318/v1/traces
    logging:
      export:
        otlp:
          endpoint: http://localhost:4318/v1/logs
  tracing:
    sampling:
      probability: 0.1
```

> **Note:** Adjust the OTLP endpoints to match your observability backend (e.g., Grafana LGTM, Jaeger, or OpenTelemetry Collector).

---

### Optional Configuration

#### Enable/Disable Auto-Configurations

Exclude specific auto-configuration classes using Spring Boot's standard property:

```properties
spring.autoconfigure.exclude=\
  com.creotech.starter.autoconfigure.FilterConfiguration,\
  com.creotech.starter.autoconfigure.OpenTelemetryConfiguration
```

#### Enable Header Logging

The `HeaderLoggerFilter` logs at DEBUG level. Enable it in your `application.properties`:

```properties
logging.level.com.creotech.starter.autoconfigure.HeaderLoggerFilter=DEBUG
```

#### Custom Logback Configuration

The OpenTelemetry appender is installed programmatically if `opentelemetry-logback-appender-1.0` is present on the classpath. You can extend the base configuration in your `logback-spring.xml` as needed.

## Auto-Configuration Details

| Configuration Class | Description |
|---------------------|-------------|
| `ContextPropagationConfiguration` | Registers `ContextPropagatingTaskDecorator` for async context propagation |
| `FilterConfiguration` | Registers `HeaderLoggerFilter` and `AddTraceIdFilter` (servlet web apps only) |
| `OpenTelemetryConfiguration` | Wires OpenTelemetry, installs Logback appender, registers JVM/system metrics and observation conventions |

### Compatibility Notes

- Requires Micrometer Core and Micrometer Tracing APIs
- The consuming application typically provides OpenTelemetry SDK/OTLP exporter
- Servlet filters only activate in servlet-based applications (not reactive/WebFlux)

## Troubleshooting

### X-Trace-Id header not appearing in responses

- Verify Micrometer Tracing is on the classpath
- Ensure you're using servlet-based Spring MVC (not WebFlux)
- Check that `FilterConfiguration` is not excluded

### Request headers not being logged

- Set DEBUG level for the filter:
  ```properties
  logging.level.com.creotech.starter.autoconfigure.HeaderLoggerFilter=DEBUG
  ```
- Verify your application uses Logback or another SLF4J implementation

### Enable/Disable Auto-Configurations

Exclude specific auto-configuration classes using Spring Boot's standard property:

```properties
spring.autoconfigure.exclude=\
  com.creotech.starter.autoconfigure.FilterConfiguration,\
  com.creotech.starter.autoconfigure.OpenTelemetryConfiguration
```

### Enable Header Logging

The `HeaderLoggerFilter` logs at DEBUG level. Enable it in your `application.properties`:

```properties
logging.level.com.creotech.starter.autoconfigure.HeaderLoggerFilter=DEBUG
```

### Custom Logback Configuration

The OpenTelemetry appender is installed programmatically if `opentelemetry-logback-appender-1.0` is present on the classpath. You can still customize Logback via `logback.xml` or `logback-spring.xml`.

## Auto-Configuration Details

| Configuration Class | Description |
|---------------------|-------------|
| `ContextPropagationConfiguration` | Registers `ContextPropagatingTaskDecorator` for async context propagation |
| `FilterConfiguration` | Registers `HeaderLoggerFilter` and `AddTraceIdFilter` (servlet web apps only) |
| `OpenTelemetryConfiguration` | Wires OpenTelemetry, installs Logback appender, registers JVM/system metrics and observation conventions |

### Compatibility Notes

- Requires Micrometer Core and Micrometer Tracing APIs
- The consuming application typically provides OpenTelemetry SDK/OTLP exporter
- Servlet filters only activate in servlet-based applications (not reactive/WebFlux)

## Troubleshooting

### X-Trace-Id header not appearing in responses

- Verify Micrometer Tracing is on the classpath
- Ensure you're using servlet-based Spring MVC (not WebFlux)
- Check that `FilterConfiguration` is not excluded

### Request headers not being logged

- Set DEBUG level for the filter:
  ```properties
  logging.level.com.creotech.starter.autoconfigure.HeaderLoggerFilter=DEBUG
  ```
- Verify your application uses Logback or another SLF4J implementation

## Building from Source

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/creo-otel-spring-boot-starter.git
cd creo-otel-spring-boot-starter

# Build
./gradlew build

# Run tests
./gradlew test

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

## Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

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