# Testing Guide: Unit, Component & Black Box Tests

## 1. Setup & Dependencies

### Add to `build.gradle`:
```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.mockito:mockito-core:5.2.0'
testImplementation 'org.mockito:mockito-junit-jupiter:5.2.0'
testImplementation 'io.jsonwebtoken:jjwt-api:0.11.5'
testImplementation 'io.jsonwebtoken:jjwt-impl:0.11.5'
testImplementation 'io.jsonwebtoken:jjwt-jackson:0.11.5'
```

### Test Annotations:
```java
@SpringBootTest  // For integration/component tests
@ExtendWith(MockitoExtension.class)  // For unit tests with mocks
@WebMvcTest(YourController.class)  // For controller/component tests
```

---

## 2. UNIT TESTS - Service Layer

### Example: CategoryService Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CategoryService categoryService;

  private User testUser;
  private Category testCategory;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
      .id(1L)
      .email("test@example.com")
      .firstName("John")
      .password("password123")
      .build();

    testCategory = Category.builder()
      .id(1L)
      .name("Groceries")
      .type(Category.CategoryType.EXPENSE)
      .color("#FF6B6B")
      .user(testUser)
      .build();
  }

  @Test
  @DisplayName("Should create category successfully")
  void testCreateCategory() {
    // Arrange
    CategoryRequestDTO request = new CategoryRequestDTO();
    request.setName("Groceries");
    request.setType("EXPENSE");
    request.setColor("#FF6B6B");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

    // Act
    Category result = categoryService.createCategory(1L, request);

    // Assert
    assertNotNull(result);
    assertEquals("Groceries", result.getName());
    assertEquals(Category.CategoryType.EXPENSE, result.getType());
    
    // Verify interactions
    verify(categoryRepository, times(1)).save(any(Category.class));
    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Should throw exception when user not found")
  void testCreateCategoryUserNotFound() {
    // Arrange
    CategoryRequestDTO request = new CategoryRequestDTO();
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
      categoryService.createCategory(999L, request);
    });
    
    verify(categoryRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should update category successfully")
  void testUpdateCategory() {
    // Arrange
    long categoryId = 1L;
    CategoryRequestDTO updateRequest = new CategoryRequestDTO();
    updateRequest.setName("Updated Groceries");
    updateRequest.setColor("#00FF00");

    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
    when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

    // Act
    Category result = categoryService.updateCategory(categoryId, updateRequest);

    // Assert
    verify(categoryRepository).save(any(Category.class));
  }
}
```

---

## 3. COMPONENT TESTS - Controller Layer

### Example: CategoryController Component Tests

```java
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CategoryService categoryService;

  private Category testCategory;

  @BeforeEach
  void setUp() {
    testCategory = Category.builder()
      .id(1L)
      .name("Groceries")
      .type(Category.CategoryType.EXPENSE)
      .color("#FF6B6B")
      .build();
  }

  @Test
  @DisplayName("Should get all categories successfully")
  @WithMockUser(username = "test@example.com")
  void testGetUserCategories() throws Exception {
    // Arrange
    List<Category> categories = Arrays.asList(testCategory);
    when(categoryService.getUserCategories(any())).thenReturn(categories);

    // Act & Assert
    mockMvc.perform(get("/categories")
      .header("X-User-Id", "1")
      .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].name").value("Groceries"));

    verify(categoryService, times(1)).getUserCategories(any());
  }

  @Test
  @DisplayName("Should create category successfully")
  @WithMockUser(username = "test@example.com")
  void testCreateCategory() throws Exception {
    // Arrange
    CategoryRequestDTO request = new CategoryRequestDTO();
    request.setName("Groceries");
    request.setType("EXPENSE");
    request.setColor("#FF6B6B");

    when(categoryService.createCategory(any(), any())).thenReturn(testCategory);

    // Act & Assert
    mockMvc.perform(post("/categories")
      .header("X-User-Id", "1")
      .contentType(MediaType.APPLICATION_JSON)
      .content(new ObjectMapper().writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.name").value("Groceries"));

    verify(categoryService, times(1)).createCategory(any(), any());
  }
}
```

---

## 4. Common Mocking Patterns

### Pattern: Argument Captor
```java
@Test
void testWithArgumentCaptor() {
  ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
  
  when(categoryRepository.save(any())).thenReturn(testCategory);
  categoryService.createCategory(1L, request);
  
  verify(categoryRepository).save(captor.capture());
  Category saved = captor.getValue();
  assertEquals("Groceries", saved.getName());
}
```

### Pattern: Exception Throwing
```java
@Test
void testExceptionHandling() {
  when(categoryRepository.findById(any())).thenThrow(new DataAccessException("DB Error"));
  
  assertThrows(DataAccessException.class, () -> {
    categoryService.getCategory(1L);
  });
}
```

---

## 5. Running Tests

```bash
./gradlew test
./gradlew test --tests CategoryServiceTest
./gradlew test --tests CategoryServiceTest.testCreateCategory
```


---

## 6. Test File Naming Conventions

### File Naming Pattern

**Format:** `{ClassName}Test.java` or `{ClassName}Tests.java`

### Examples:

```
Service Tests:
├── CategoryServiceTest.java
├── TransactionServiceTest.java
├── UserServiceTest.java
└── SecurityServiceTest.java

Controller Tests:
├── CategoryControllerTest.java
├── TransactionControllerTest.java
├── AuthControllerTest.java
└── UserControllerTest.java

Repository Tests:
├── CategoryRepositoryTest.java
├── TransactionRepositoryTest.java
└── UserRepositoryTest.java

Integration Tests:
├── AuthenticationIntegrationTest.java
├── CategoryIntegrationTest.java
└── TransactionIntegrationTest.java

Utility/Helper Tests:
├── JwtTokenProviderTest.java
├── PasswordEncoderUtilTest.java
└── DateUtilTest.java
```

### Directory Structure:

```
src/
├── main/
│   └── java/com/finance/dashboard/
│       ├── controller/
│       │   ├── CategoryController.java
│       │   └── TransactionController.java
│       ├── service/
│       │   ├── CategoryService.java
│       │   └── TransactionService.java
│       └── repository/
│           ├── CategoryRepository.java
│           └── TransactionRepository.java
│
└── test/
    └── java/com/finance/dashboard/
        ├── controller/
        │   ├── CategoryControllerTest.java
        │   └── TransactionControllerTest.java
        ├── service/
        │   ├── CategoryServiceTest.java
        │   └── TransactionServiceTest.java
        └── repository/
            ├── CategoryRepositoryTest.java
            └── TransactionRepositoryTest.java
```

### Test Method Naming Convention

**Format:** `test[MethodName][Condition][ExpectedResult]` or `[MethodName]_When[Condition]_Then[ExpectedResult]`

#### Examples:

```java
// Pattern 1: test + method + condition + result
testCreateCategoryWithValidInput_ShouldReturnCategory()
testCreateCategoryWithNullName_ShouldThrowException()
testGetUserCategories_ShouldReturnEmptyList_WhenNoCategories()

// Pattern 2: given-when-then style
givenValidUser_whenCreatingCategory_thenReturnSavedCategory()
givenNonExistentUser_whenCreatingCategory_thenThrowException()

// Pattern 3: descriptive with @DisplayName
@DisplayName("Should create category successfully")
void testCreateCategory() { ... }

@DisplayName("Should throw exception when user not found")
void testCreateCategoryUserNotFound() { ... }

@DisplayName("Should return empty list when no transactions exist")
void testGetTransactionsEmptyResult() { ... }
```

### Test Class Structure Template

```java
/**
 * Unit tests for CategoryService
 * 
 * Tests cover:
 * - Category creation
 * - Category retrieval
 * - Category updates
 * - Category deletion
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  // ============= Setup =============
  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CategoryService categoryService;

  private User testUser;
  private Category testCategory;

  @BeforeEach
  void setUp() {
    // Initialize test data
  }

  // ============= Happy Path Tests =============
  @Test
  @DisplayName("Should create category successfully")
  void testCreateCategorySuccess() { ... }

  @Test
  @DisplayName("Should retrieve category by id")
  void testGetCategorySuccess() { ... }

  // ============= Error Cases =============
  @Test
  @DisplayName("Should throw exception when user not found")
  void testCreateCategoryUserNotFound() { ... }

  // ============= Edge Cases =============
  @Test
  @DisplayName("Should handle null input gracefully")
  void testCreateCategoryWithNullInput() { ... }

  // ============= Verification Tests =============
  @Test
  @DisplayName("Should call repository save method")
  void testCreateCategoryVerifySave() { ... }
}
```

### File Organization Best Practices

```
✅ DO:
- Keep test file in same package as class being tested
- Use consistent naming across all tests
- Group related tests in same test class
- Use @DisplayName for clarity
- One main assertion per test

❌ DON'T:
- Mix unit and integration tests in same file
- Use vague names like testMethod() or test123()
- Have tests depend on each other
- Test multiple concerns in one test
- Ignore test organization
```

---

## 7. Running Tests by File Pattern

```bash
# Run all tests
./gradlew test

# Run all service tests
./gradlew test --tests "*ServiceTest"

# Run all controller tests
./gradlew test --tests "*ControllerTest"

# Run all integration tests
./gradlew test --tests "*IntegrationTest"

# Run specific test file
./gradlew test --tests CategoryServiceTest

# Run specific test method
./gradlew test --tests CategoryServiceTest.testCreateCategory

# Run tests matching pattern
./gradlew test --tests "*Service*"
```

