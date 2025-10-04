
---

# Spring PetClinic — Security Milestone 2 (IAS2)

This document summarizes the security hardening we implemented for Spring PetClinic and how to **run**, **log in**, and **verify** the controls locally.

---

## Implemented Security Controls (5)

1. **Spring Security Core (Authentication, CSRF, Headers)**

   * Added `spring-boot-starter-security` and enabled the standard Spring Security filter chain.
   * Default secure headers and CSRF protection are now active.

2. **Role-Based Access Control (RBAC) + Secure UI**

   * Configured in-memory users for demo/testing (`admin` and `user`).
   * Integrated Thymeleaf Security Extras to show/hide menu items and action links based on role.

3. **CSRF Tokens on Forms**

   * All forms using `th:action` automatically include CSRF tokens.
   * Any plain forms were updated to include hidden CSRF token fields.

4. **H2 Console Restriction**

   * **Production**: H2 console is disabled.
   * **Development profile**: H2 console enabled at `/h2-console` for local use only.

5. **Global Exception Handling**

   * Implemented `@ControllerAdvice` to log full stack traces internally while showing a friendly error page to end users.

---

## Prerequisites

* **Java 17+** (check with `java -version`)
* **Maven Wrapper** included (`mvnw` / `./mvnw`) — no need to install Maven separately
* Port **8080** must be free

> ⚠️ If your project folder is inside **OneDrive**, Windows may lock `target/`. If a build fails to delete `target/`, close running apps or manually remove the folder:
>
> * Windows: `rmdir /s /q target`
> * macOS/Linux: `rm -rf target`

---

## Build the Application

### Windows (CMD/PowerShell)

```bat
.\mvnw clean package -DskipTests
```

### macOS/Linux

```bash
./mvnw clean package -DskipTests
```

If you encounter a **formatting** failure from `spring-javaformat`, auto-fix with:

```bash
./mvnw spring-javaformat:apply
```

Then rebuild.

---

## Run the Application

### Option A — Run with Maven (Default Profile, Production-like)

```bat
.\mvnw spring-boot:run
```

Open: **[http://localhost:8080](http://localhost:8080)**

---

### Option B — Run with Maven (Development Profile, H2 Enabled)

```bat
.\mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Open:

* App → **[http://localhost:8080](http://localhost:8080)**
* H2 Console → **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**

**H2 Console Settings (Dev):**

* JDBC URL: `jdbc:h2:mem:testdb`
* User: `sa`
* Password: *(leave blank)*

---

### Option C — Run with Packaged JAR (Default Profile)

After building (`mvnw clean package`), run:

**Windows:**

```bat
java -jar target/spring-petclinic-*.jar
```

**macOS/Linux:**

```bash
java -jar target/spring-petclinic-*.jar
```

Open: **[http://localhost:8080](http://localhost:8080)**

---

### Option D — Run with Packaged JAR (Development Profile)

```bat
java -jar target/spring-petclinic-*.jar --spring.profiles.active=dev
```

---

## Sign In (Demo Credentials)

We configured **in-memory users** in `SecurityConfig` for testing:

* **Admin**

  * Username: `admin`
  * Password: `admin123`
  * Role: `ADMIN`

* **Standard User**

  * Username: `user`
  * Password: `user`
  * Role: `USER`

**Role Access (UI examples):**

* **Both roles** → Home and Vets pages
* **Authenticated (USER or ADMIN)** → Owners CRUD features
* **Admin-only** → Error demo page, H2 Console link

---

## Logging Out

Click **Logout** in the navbar.
If you remain logged in, clear cookies, refresh with **Ctrl+Shift+R**, or use a private window.

---

## Key Changes in the Codebase

* **`pom.xml`**

  * Added Spring Security and Thymeleaf Security Extras dependencies.

* **`SecurityConfig.java`**

  * Defined the security filter chain, login, logout, CSRF, headers, and in-memory users.

* **`layout.html`** (UI fragment)

  * Added `xmlns:sec` namespace.
  * Wrapped menu items with `sec:authorize`.
  * Added Login/Logout links and “Signed in as …” display.

* **Forms** (Owners, Pets, Visits)

  * Ensured CSRF tokens are included in all forms.

* **`application.properties`** (production settings)

  * Disabled H2 console, added session timeout, and hardened cookies.

* **`application-dev.properties`**

  * Enabled H2 console locally.

* **`GlobalExceptionHandler.java`**

  * Logs full exceptions while showing a generic error page to users.

---

## Quick Test Checklist

* [ ] Visit `/owners/find` logged out → redirected to login
* [ ] Login works (`admin/admin123` or `user/user`)
* [ ] RBAC works → `user` sees fewer links, `admin` sees all
* [ ] CSRF works → submitting without token fails
* [ ] H2 Console → works only in **dev** profile
* [ ] `/oups` → shows friendly error page, no stack trace in browser

---

## Why These Changes

These address audit findings (OWASP ZAP + manual tests):

* Missing/weak headers → fixed by Spring Security defaults
* No CSRF protection → enforced CSRF tokens
* Overexposed dev endpoints → restricted H2 console
* Error stack traces → hidden from users, logged internally
* Weak UI controls → enforced RBAC and conditional rendering

---

## Troubleshooting

* **Build fails to delete `target/` (Windows)**

```bat
rmdir /s /q target
.\mvnw clean package -DskipTests
```

* **Login loop** → Clear cookies, confirm credentials match `SecurityConfig`.

* **H2 Console shows error** → Run with the **dev profile**:

```bat
.\mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Then open **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**

---


