package com.taskflow.backend;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.TimeZone;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    private static final String SUFFIX = String.valueOf(System.currentTimeMillis());

    @BeforeAll
    static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @BeforeEach
    void init() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @Order(1)
    void register_success() {
        webTestClient.post().uri("/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue("""
                    {"name":"Test User","email":"green_success_%s@example.com","password":"123456"}
                    """.formatted(SUFFIX))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.token").exists()
                .jsonPath("$.email").isEqualTo("green_success_%s@example.com".formatted(SUFFIX));
    }

    @Test
    @Order(2)
    void register_duplicate_email_returns_409() {
        String email = "green_dup_%s@example.com".formatted(SUFFIX);
        String body = """
                {"name":"Test User","email":"%s","password":"123456"}
                """.formatted(email);

        webTestClient.post().uri("/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue(body).exchange().expectStatus().isCreated();

        webTestClient.post().uri("/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue(body).exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.error").isEqualTo("email already exists");
    }

    @Test
    @Order(3)
    void register_missing_name_returns_400() {
        webTestClient.post().uri("/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue("""
                    {"email":"green_missing_%s@example.com","password":"123456"}
                    """.formatted(SUFFIX))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("validation failed")
                .jsonPath("$.fields.name").isEqualTo("is required");
    }

    @Test
    @Order(4)
    void register_missing_password_returns_400() {
        webTestClient.post().uri("/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue("""
                    {"name":"Test","email":"nopw_%s@example.com"}
                    """.formatted(SUFFIX))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("validation failed")
                .jsonPath("$.fields.password").exists();
    }

    @Test
    @Order(5)
    void login_success() {
        String email = "green_login_%s@example.com".formatted(SUFFIX);

        webTestClient.post().uri("/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue("""
                    {"name":"Login User","email":"%s","password":"123456"}
                    """.formatted(email))
                .exchange().expectStatus().isCreated();

        webTestClient.post().uri("/auth/login")
                .header("Content-Type", "application/json")
                .bodyValue("""
                    {"email":"%s","password":"123456"}
                    """.formatted(email))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").exists()
                .jsonPath("$.email").isEqualTo(email);
    }

    @Test
    @Order(6)
    void login_wrong_password_returns_401() {
        String email = "wrongpass_%s@example.com".formatted(SUFFIX);

        webTestClient.post().uri("/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue("""
                    {"name":"User","email":"%s","password":"123456"}
                    """.formatted(email))
                .exchange().expectStatus().isCreated();

        webTestClient.post().uri("/auth/login")
                .header("Content-Type", "application/json")
                .bodyValue("""
                    {"email":"%s","password":"wrongpassword"}
                    """.formatted(email))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("invalid credentials");
    }

    @Test
    @Order(7)
    void login_user_not_found_returns_401() {
        webTestClient.post().uri("/auth/login")
                .header("Content-Type", "application/json")
                .bodyValue("""
                    {"email":"notfound_%s@example.com","password":"123456"}
                    """.formatted(SUFFIX))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("invalid credentials");
    }

    @Test
    @Order(8)
    void login_empty_body_returns_400() {
        webTestClient.post().uri("/auth/login")
                .header("Content-Type", "application/json")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("validation failed");
    }
}