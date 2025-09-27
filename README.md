
---

# Spring PetClinic — Security Milestone 2 (IAS2)

This document summarizes the security hardening we implemented for Spring PetClinic and how to **run**, **log in**, and **verify** the controls locally.

---

## What we implemented (5 controls)

1. **Spring Security core (auth, CSRF, headers)**

* Added `spring-boot-starter-security` and enabled standard security filter chain.
* Default secure headers + CSRF enabled.

2. **Role-Based Access Control (RBAC) + secure UI**

* In-memory users for demo/testing (`admin` and `user`).
* Thymeleaf Security Extras to show/hide menu items and action links based on role.

3. **CSRF tokens on forms**

* Forms that use `th:action` get tokens automatically.
* Any plain forms include hidden CSRF token.

4. **H2 Console restriction**

* **Production**: H2 console disabled.
* **Dev profile**: H2 console enabled at `/h2-console`, local only.

5. **Global exception handling (no stack traces to users)**

* `@ControllerAdvice` returns a friendly error page and logs full details server-side.

---

## Prerequisites

* **Java 17+** (check with `java -version`)
* **Maven Wrapper** included (`mvnw`/`./mvnw`) — no need to install Maven separately
* Port **8080** must be free

> If your folder is inside **OneDrive**, Windows can lock `target/`. If a build fails to delete `target/`, close running apps, or run:
> `rmdir /s /q target` (Windows) / `rm -rf target` (macOS/Linux)

---

## Build the app

### Windows (CMD/PowerShell)

```bat
.\mvnw clean package -DskipTests
```

### macOS/Linux

```bash
./mvnw clean package -DskipTests
```

If you see a **formatting** failure from `spring-javaformat`, you can auto-fix:

```bash
./mvnw spring-javaformat:apply
```

(then build again)

---

## Run the app

### Option A — “Production-like” (default profile, H2 console disabled)

Windows:

```bat
.\mvnw spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

Open: **[http://localhost:8080](http://localhost:8080)**

### Option B — Development profile (H2 console enabled)

We added `src/main/resources/application-dev.properties`:

```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.h2.console.settings.web-allow-others=false
management.endpoints.web.exposure.include=health,info,env,beans
```

Run with the **dev** profile:

Windows:

```bat
.\mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

macOS/Linux:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Open:

* App: **[http://localhost:8080](http://localhost:8080)**
* H2 Console (dev only): **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**

**H2 Console settings (dev):**

* **JDBC URL:** `jdbc:h2:mem:testdb`
* **User:** `sa`
* **Password:** *(leave blank)*

> In **default** mode (no profile), the H2 console is **disabled** for security.

---

## Sign in (demo credentials)

We configured **in-memory users** in `SecurityConfig` for testing:

* **Admin**

  * **Username:** `admin`
  * **Password:** `admin`
  * **Role:** `ADMIN`

* **Standard user**

  * **Username:** `user`
  * **Password:** `user`
  * **Role:** `USER`

**What each role can see/do (UI examples):**

* **Both** can access Home and Vets pages.
* **Authenticated (`USER` or `ADMIN`)** can use Owners → Find Owners and typical CRUD workflows.
* **Admin-only** items (e.g., dev links like Error demo, H2 Console menu item) are shown only to `ADMIN`.

> If you changed these in your `SecurityConfig`, use your own values.
> Also, when you later enable HTTPS, set the session cookie `secure` flag.

---

## How to log out

Click **Logout** in the navbar.
If your browser seems to “stay logged in,” do a hard refresh (`Ctrl+Shift+R`) or open a new private window. With CSRF enabled, Spring Security hits `/logout` via POST under the hood—Thymeleaf/Security handles this for the link.

---

## Where we changed things (high level)

* **`pom.xml`**

  * Added:

    ```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
      <groupId>org.thymeleaf.extras</groupId>
      <artifactId>thymeleaf-extras-springsecurity6</artifactId>
    </dependency>
    ```

* **`src/main/java/.../config/SecurityConfig.java`**

  * Defines the security filter chain, login, logout, CSRF (enabled), headers, and **in-memory users** (`admin/admin`, `user/user`).

* **`src/main/resources/templates/fragments/layout.html`**

  * Added `xmlns:sec="https://www.thymeleaf.org/extras/spring-security"`
  * Wrapped menu items with `sec:authorize="isAuthenticated()"` or `hasRole('ADMIN')`
  * Added Login/Logout conditional links and “Signed in as …” display.

* **Forms** (e.g., Owners/Pets/Visits)

  * Most PetClinic forms already use `th:action` (CSRF auto-included).
  * Any plain `<form>` (without `th:action`) includes:

    ```html
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    ```

* **`application.properties`**

  * Production stance:

    ```properties
    spring.h2.console.enabled=false
    server.servlet.session.timeout=15m
    server.servlet.session.cookie.http-only=true
    # server.servlet.session.cookie.secure=true   # enable when using HTTPS
    ```

* **`application-dev.properties`** (dev only)

  * Enables H2 console locally (see above).

* **`GlobalExceptionHandler.java`**

  * `@ControllerAdvice` to log full stack traces internally and show a **generic** error page to users.

---

## Quick test checklist

* **Auth required**: Visit `/owners/find` while logged out → redirected to login.
* **Login works**: Use `admin/admin` or `user/user`.
* **RBAC works**: As `user`, admin-only links are hidden; as `admin`, they appear.
* **CSRF works**: Submitting a form without a token should fail (dev test).
* **H2 Console**: Available only in **dev** profile; otherwise disabled.
* **Error page**: Visit `/oups` → friendly message, no stack trace in browser (but logged in console).

---

## Why these changes

These directly address audit results (OWASP ZAP + manual tests):

* Missing/weak headers → fixed by Spring Security defaults.
* No CSRF protection → CSRF enabled and verified on forms.
* Overexposed dev endpoints → H2 console disabled by default, dev-only when needed.
* Error stack traces → hidden from users, logged server-side.
* Unauthenticated access/weak UI controls → RBAC and conditional UI rendering.

---

## Troubleshooting

* **Windows build fails to delete `target/`**
  Close the app and run:

  ```bat
  rmdir /s /q target
  ```

  Then build again:

  ```bat
  .\mvnw clean package -DskipTests
  ```

* **Login loop**
  Clear cookies or try a private window; ensure `SecurityConfig` actually defines the in-memory users you’re using.

* **H2 console 404/“error page”**
  Use the **dev profile** to enable it:

  ```bat
  .\mvnw spring-boot:run -Dspring-boot.run.profiles=dev
  ```

  Then open **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**.

---

