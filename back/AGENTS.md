# AGENTS.md — Backend

> Supplements the root `AGENTS.md`; root rules apply.

## Context

- **Stack**: Java 17, Spring Boot 3.2, MyBatis-Plus, MySQL 8, springdoc.
- **Entry point**: `com.dongruan.environment.DemoApplication`.
- **Port**: `8080`; OpenAPI UI: `/swagger-ui.html`.
- **Source**: `src/main/java/com/dongruan/environment/`; tests: `src/test/java/`.

## Commands

- Run: `./mvnw spring-boot:run`
- Test: `./mvnw test`

## Conventions

Keep controllers limited to HTTP concerns; use services for business logic and mappers for persistence. Use `ResultVO` for the existing response envelope. Add JUnit tests under the matching package, named `*Tests`.

Read root `docs/2-designs/api_contract.md` before API changes and `db_schema.md` before entity or SQL changes.
