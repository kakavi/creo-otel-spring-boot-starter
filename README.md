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