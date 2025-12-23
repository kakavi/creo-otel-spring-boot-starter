# Creo OTEL Spring Boot Starter

A lightweight Spring Boot starter that auto-configures OpenTelemetry + Micrometer conveniences and a couple of servlet filters to improve log correlation:
- Logs incoming HTTP request headers at DEBUG.
- Adds the current trace id to responses via the `X-Trace-Id` header.
- Installs the OpenTelemetry Logback appender at runtime so your logs are trace-aware when a compatible logging setup is present.

Coordinates
- Group: `com.creotech`
- Artifact: `creo-otel-spring-boot-starter`
- Version: `0.0.1-SNAPSHOT`

Requirements
- Java 25+
- Spring Boot 4.x (the exact version is taken from your BOM; default is 4.0.0 via gradle.properties)
- A logging implementation on the classpath (e.g., Spring Boot’s starter-logging which brings Logback)

Features
- Context propagation for async tasks using `ContextPropagatingTaskDecorator`.
- Servlet filters (only in servlet web apps):
  - HeaderLoggerFilter – logs request headers at DEBUG.
  - AddTraceIdFilter – adds `X-Trace-Id` using Micrometer Tracing’s current trace context.
- OpenTelemetry integration:
  - Installs `opentelemetry-logback-appender` at runtime if available.
  - Registers JVM/system metrics with OTEL conventions.
  - Registers `OpenTelemetryServerRequestObservationConvention`.

Quick start
1) Add the dependency
- Gradle (Groovy)
  repositories {
      mavenCentral()
      mavenLocal() // optional if consuming a local SNAPSHOT
  }
  dependencies {
      implementation 'com.creotech:creo-otel-spring-boot-starter:0.0.1-SNAPSHOT'
  }

- Gradle (Kotlin)
  repositories {
      mavenCentral()
      mavenLocal()
  }
  dependencies {
      implementation("com.creotech:creo-otel-spring-boot-starter:0.0.1-SNAPSHOT")
  }

- Maven
  <dependency>
    <groupId>com.creotech</groupId>
    <artifactId>creo-otel-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  </dependency>

2) Ensure logging is present
Spring Boot applications typically include `spring-boot-starter-logging` (Logback). This starter does not force a logging implementation; it only needs the APIs to compile.

3) Run your app
- Set your application log level to DEBUG to see header logs from `HeaderLoggerFilter`.
- Make a request and observe `X-Trace-Id` in the response headers.

Configuration and customization
- Enable/disable auto-configurations
  You can exclude any auto-configuration class using Spring Boot’s standard property:
  spring.autoconfigure.exclude=com.creotech.starter.autoconfigure.FilterConfiguration,com.creotech.starter.autoconfigure.OpenTelemetryConfiguration

- Control header logging verbosity
  This filter only logs at DEBUG. To enable it:
  logging.level.com.creotech.starter.autoconfigure.HeaderLoggerFilter=DEBUG

- Using a custom Logback config
  The OTEL appender is installed programmatically if `io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0` is present. You can still fully customize Logback via your `logback.xml`/`logback-spring.xml`.

What gets auto-configured
- ContextPropagationConfiguration – registers `ContextPropagatingTaskDecorator` (for async context propagation).
- FilterConfiguration – registers `HeaderLoggerFilter` and `AddTraceIdFilter` when running as a servlet web app and Micrometer Tracing is on the classpath.
- OpenTelemetryConfiguration – wires `OpenTelemetry` and installs the Logback appender; also registers JVM/system metrics and the default server observation convention.

Compatibility notes
- This starter relies on Micrometer Core and Micrometer Tracing APIs and on the OpenTelemetry Logback appender. The consuming application typically brings OpenTelemetry SDK/OTLP exporter as part of your observability setup.
- The servlet filters only activate in servlet-based apps (not reactive).

Local build and publish (for testing)
- Build and publish to your local Maven cache:
  ./gradlew clean publishToMavenLocal

Troubleshooting
- I don’t see X-Trace-Id in responses
  Ensure Micrometer Tracing is on the classpath and that you’re using servlet-based Spring MVC.

- I don’t see header logs
  Set DEBUG level for `com.creotech.starter.autoconfigure.HeaderLoggerFilter` (or package) and ensure your app uses Logback or another SLF4J implementation.

Contributing
- Issues and pull requests are welcome. Please include reproduction steps and versions.
- Follow conventional commit messages if possible and include tests where applicable.

Versioning
- Uses Semantic Versioning. SNAPSHOTs are subject to change.

Security
- Please do not file security issues publicly. Contact the maintainers privately if you suspect a vulnerability.

License
- Licensed under the Apache License, Version 2.0.
