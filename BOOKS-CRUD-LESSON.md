# Books CRUD API: build it yourself

This is a hands-on lesson, not an implementation. Add the pieces in order, run the app, and test every step from Postman. The existing project already has Spring MVC, Spring Data JPA, PostgreSQL, and Spring Security dependencies. Since authentication is a later topic, first configure the development security setup so it does not block your API experiments.

## Learning goals

By the end, you should be able to explain:

- How an HTTP request becomes a controller call, service operation, repository call, and database change.
- Why the API should use request/response DTOs rather than expose JPA entities.
- How validation and exception handling produce useful HTTP responses.
- Where logging belongs, and what should not be logged.

## Before you start

1. Start Docker Desktop and the PostgreSQL container from the project folder:

   ```powershell
   docker compose up -d
   ```

2. Confirm the `learning` database is reachable from DBeaver.
3. Start the app once with `.\gradlew.bat bootRun`; note the startup result, then stop it.
4. Read the existing `application.properties` and note the JDBC URL and environment-variable overrides.
5. Make sure you understand that Spring Security is on the classpath. Before you test unauthenticated Postman requests, configure a temporary development security policy that permits your API routes. Do not remove security from the project as a shortcut; you will revisit authentication and authorization later.

## Design the feature

Build a `Book` resource with:

- `id`: generated numeric identifier
- `title`: required, up to 200 characters
- `author`: required, up to 120 characters

Use the package root `com.java.learning.book` and organize the code into `entity`, `dto`, `repository`, `service`, and `web` (or `controller`) packages.

Before coding, write down the responsibility of each layer:

| Layer | Responsibility | Should not do |
|---|---|---|
| Entity | JPA mapping and domain state | Define the public HTTP response shape |
| Request DTO | Validate client input | Access the database |
| Response DTO | Describe data returned by the API | Be a JPA entity |
| Repository | Persist and query entities | Contain HTTP concerns |
| Service | Perform CRUD use cases and transactions | Build HTTP response headers |
| Controller | Map routes and delegate | Contain persistence logic |
| Controller helper | Share repeated HTTP response construction, if useful | Become a second service layer |
| Exception advice | Translate known failures into HTTP error responses | Hide unexpected failures |

Keep this small: implement only what the Books flow needs. You may decide whether a `ControllerHelper` is useful after you have at least one response pattern to share; do not add abstraction just for its own sake.

## Build in small steps

### Step 1: Entity and schema

Create the JPA entity and map it to a `books` table. Decide which fields are non-null and set database column limits consistently with your API validation.

- Configure schema creation/update only for local learning if your project requires it.
- Start the application and verify the table appears in DBeaver.
- Consider how you would use migrations (such as Flyway) for a real deployment instead of automatic schema updates.

**Checkpoint:** Can you explain how JPA maps each Java field to a database column?

### Step 2: Repository

Create a Spring Data repository for the entity using its ID type. Use built-in CRUD methods first; avoid writing custom queries until you need them.

**Checkpoint:** The app starts with the repository bean wired successfully.

### Step 3: DTOs and validation

Create:

- A request DTO for `title` and `author`.
- A response DTO for `id`, `title`, and `author`.

Add validation for blank strings and maximum lengths. Add Spring's validation starter to `build.gradle` if needed, then refresh Gradle. Remember that declaring constraints alone is not enough: make sure request-body validation is triggered in the controller.

**Checkpoint:** Decide what status and response body invalid input should produce before implementing error handling.

### Step 4: Service

Implement these use cases in the service:

- Create a book.
- List all books.
- Find one book by ID.
- Update an existing book.
- Delete an existing book.

Keep entity-to-response conversion out of the controller. Decide what happens when an ID does not exist (recommended: a dedicated not-found exception). Put transaction boundaries around service operations; make read-only operations read-only transactions where appropriate.

**Checkpoint:** Test the service with repository mocks before connecting HTTP routes.

### Step 5: Controller and response helper

Expose these routes under `/api/books`:

| Method | Route | Expected success |
|---|---|---|
| POST | `/api/books` | `201 Created`, response DTO, `Location` header |
| GET | `/api/books` | `200 OK`, list (empty list if there are no books) |
| GET | `/api/books/{id}` | `200 OK`, one book |
| PUT | `/api/books/{id}` | `200 OK`, updated book |
| DELETE | `/api/books/{id}` | `204 No Content` |

Use constructor injection. Keep each controller method thin: accept/validate input, call the service, and return the appropriate HTTP response. A small controller helper can construct the create response and `Location` header if that pattern is genuinely reusable.

### Step 6: Error responses

Add centralized exception handling for:

- Missing book ID → `404 Not Found`
- Invalid request DTO → `400 Bad Request`

Return a consistent error shape, for example timestamp, status, error, message, and request path. Do not return stack traces or internal details. Let unexpected server errors remain server errors and make them visible in logs.

### Step 7: Logging

Use SLF4J with parameterized log messages in the service layer for successful create, update, and delete operations. Use the record ID as context.

- Do not log passwords, tokens, complete request bodies, or other sensitive data.
- Do not log the same successful operation in every layer.
- Use appropriate log levels; routine successful CRUD operations are usually `INFO`, while detailed troubleshooting belongs at `DEBUG`.
- Check that failed operations are diagnosable without exposing sensitive information.

## Postman practice

Create a Postman collection named **Books CRUD**. Set a collection variable `baseUrl` to `http://localhost:8080`.

For requests with JSON bodies, use `Content-Type: application/json`.

1. **Create** — `POST {{baseUrl}}/api/books`

   ```json
   {
     "title": "Dune",
     "author": "Frank Herbert"
   }
   ```

   Check for `201`, a generated `id`, and a `Location` header. Save the returned ID as a collection variable such as `bookId`.

2. **List** — `GET {{baseUrl}}/api/books`

   Check that the created book is in the array. Also try this before creating anything; an empty collection should be a successful `200` response.

3. **Read one** — `GET {{baseUrl}}/api/books/{{bookId}}`

4. **Update** — `PUT {{baseUrl}}/api/books/{{bookId}}`

   ```json
   {
     "title": "Dune Messiah",
     "author": "Frank Herbert"
   }
   ```

   Read the book again and confirm the saved values changed.

5. **Delete** — `DELETE {{baseUrl}}/api/books/{{bookId}}`

   Check for `204`, then read the same ID and confirm it returns `404`.

6. **Validation** — try a blank title, missing author, and overlong values; expect `400`.
7. **Not found** — request a numeric ID that does not exist; expect `404`.

Inspect `books` in DBeaver between requests to connect the API behavior to persisted rows.

## Tests to write

Start with focused tests and add integration coverage as you learn:

- Service unit tests: create, list, get, update, delete, missing ID.
- Controller tests: route/method mapping, request validation, status codes, response shape.
- Persistence or integration test: verify an entity is stored and read from PostgreSQL (use an isolated test database or Testcontainers rather than relying on your personal learning database).

Run tests using:

```powershell
.\gradlew.bat test
```

## Suggested learning sequence after CRUD

1. Finish and test the unauthenticated CRUD flow locally.
2. Add authentication and authorization as a separate step; start with the Spring Security concepts and decide which routes require which roles.
3. Add structured request correlation and review what is safe to log.
4. Replace development schema auto-update with database migrations before treating the app as deployable.

## Completion checklist

- [ ] All five CRUD routes work from Postman.
- [ ] PostgreSQL rows match the API results in DBeaver.
- [ ] Invalid input returns `400`; unknown IDs return `404`.
- [ ] The controller delegates; the service owns use cases; the repository owns persistence.
- [ ] Entities are not returned directly from API routes.
- [ ] Logs record useful CRUD events without leaking request secrets.
- [ ] Tests pass with `.\gradlew.bat test`.

## Reference snippets (peek when stuck)

These are examples to consult while implementing the steps above, not files already added to your project. Try to write each layer yourself first, then compare your approach. Imports and package names are shown so you can adapt each snippet to your chosen package structure.

### Temporary local security setup

Because Spring Security is already included, unauthenticated requests may be blocked until you configure it. For local learning only, you can permit the API routes and ignore CSRF for those API routes. Do not use this policy in a deployed application; replace it with authentication and authorization in the later security lesson.

```java
package com.java.learning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class LocalApiSecurityConfig {

    @Bean
    SecurityFilterChain localApiSecurity(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .anyRequest().denyAll());
        return http.build();
    }
}
```

Keep the application bound to localhost while using this temporary open API. Remove or replace this configuration before exposing the app beyond your machine.

### Entity

```java
package com.java.learning.book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 120)
    private String author;

    protected Book() {
    }

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }

    public void updateDetails(String title, String author) {
        this.title = title;
        this.author = author;
    }
}
```

The protected no-argument constructor is needed by JPA. No public setter for `id` is needed because the database generates it.

### DTOs and validation

If validation classes cannot be resolved, add the validation starter to `dependencies` in `build.gradle` and refresh Gradle:

```groovy
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

Request DTO:

```java
package com.java.learning.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 120) String author) {
}
```

Response DTO:

```java
package com.java.learning.book.dto;

public record BookResponse(Long id, String title, String author) {
}
```

### Repository

```java
package com.java.learning.book.repository;

import com.java.learning.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
```

`JpaRepository` already provides methods such as `save`, `findAll`, `findById`, and `delete`; add custom queries only when the use case requires them.

### Service: CRUD and entity-to-DTO conversion

This shows the basic shape. Add methods one at a time and think about what to do when `findById` is empty.

```java
package com.java.learning.book.service;

import com.java.learning.book.dto.BookRequest;
import com.java.learning.book.dto.BookResponse;
import com.java.learning.book.entity.Book;
import com.java.learning.book.repository.BookRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookResponse create(BookRequest request) {
        Book saved = bookRepository.save(
            new Book(request.title().trim(), request.author().trim()));
        logger.info("Created book with id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return toResponse(findBook(id));
    }

    public BookResponse update(Long id, BookRequest request) {
        Book book = findBook(id);
        book.updateDetails(request.title().trim(), request.author().trim());
        Book saved = bookRepository.save(book);
        logger.info("Updated book with id={}", id);
        return toResponse(saved);
    }

    public void delete(Long id) {
        Book book = findBook(id);
        bookRepository.delete(book);
        logger.info("Deleted book with id={}", id);
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor());
    }
}
```

### Not-found exception

```java
package com.java.learning.book.service;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("Book with id " + id + " was not found");
    }
}
```

### Controller

`@Valid` makes the request DTO constraints run. Notice that the controller delegates work to the service rather than using the repository directly.

```java
package com.java.learning.book.web;

import com.java.learning.book.dto.BookRequest;
import com.java.learning.book.dto.BookResponse;
import com.java.learning.book.service.BookService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final ControllerHelper controllerHelper;

    public BookController(BookService bookService, ControllerHelper controllerHelper) {
        this.bookService = bookService;
        this.controllerHelper = controllerHelper;
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        BookResponse created = bookService.create(request);
        return controllerHelper.created(created.id(), created);
    }

    @GetMapping
    public List<BookResponse> findAll() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    public BookResponse findById(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @PutMapping("/{id}")
    public BookResponse update(
            @PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return bookService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Controller helper for `201 Created`

The helper is optional; it illustrates one place to build the create response's `Location` header.

```java
package com.java.learning.book.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class ControllerHelper {

    public <T> ResponseEntity<T> created(Long id, T body) {
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(id)
            .toUri();
        return ResponseEntity.created(location).body(body);
    }
}
```

### Central error response

The example handles expected errors; it deliberately does not catch every exception and turn it into a success-shaped response.

```java
package com.java.learning.book.web;

import java.time.Instant;

public record ApiError(
    Instant timestamp, int status, String error, String message, String path) {
}
```

```java
package com.java.learning.book.web;

import com.java.learning.book.service.BookNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    ResponseEntity<ApiError> notFound(
            BookNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalidRequest(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + " "
                + fieldError.getDefaultMessage())
            .findFirst()
            .orElse("Request validation failed");
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status, String message, HttpServletRequest request) {
        ApiError body = new ApiError(
            Instant.now(), status.value(), status.getReasonPhrase(),
            message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
```

### Local schema and logging settings

For a disposable local learning database, these settings let Hibernate create/update tables and show your application logs. Add them to `application.properties` only after you understand the trade-off; use database migrations rather than automatic updates for a deployed application.

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
logging.level.com.java.learning=INFO
```
