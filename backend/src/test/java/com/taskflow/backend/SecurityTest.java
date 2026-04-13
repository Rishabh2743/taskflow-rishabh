package com.taskflow.backend;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.TimeZone;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityTest {

    @LocalServerPort
    private int port;

    private WebTestClient client;

    private static final String SUFFIX = String.valueOf(System.currentTimeMillis());

    @BeforeAll
    static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @BeforeEach
    void init() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private String getToken(String prefix) {
        String email = prefix + "_" + SUFFIX + "@example.com";

        client.post().uri("/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue("""
                        {"name":"Security User","email":"%s","password":"123456"}
                        """.formatted(email))
                .exchange();

        return client.post().uri("/auth/login")
                .header("Content-Type", "application/json")
                .bodyValue("""
                        {"email":"%s","password":"123456"}
                        """.formatted(email))
                .exchange()
                .returnResult(Map.class)
                .getResponseBody()
                .blockFirst()
                .get("token")
                .toString();
    }

    // ✅ GREEN — /auth/register is public (no token needed)
    @Test
    @Order(1)
    void register_is_public() {
        client.post().uri("/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue("""
                        {"name":"Public User","email":"public_%s@example.com","password":"123456"}
                        """.formatted(SUFFIX))
                .exchange()
                .expectStatus().isCreated(); // ✅ 201 no token needed
    }

    // ✅ GREEN — /auth/login is public (no token needed)
    @Test
    @Order(2)
    void login_is_public() {
        String email = "login_public_%s@example.com".formatted(SUFFIX);

        client.post().uri("/auth/register")
                .header("Content-Type", "application/json")
                .bodyValue("""
                        {"name":"Login Public","email":"%s","password":"123456"}
                        """.formatted(email))
                .exchange();

        client.post().uri("/auth/login")
                .header("Content-Type", "application/json")
                .bodyValue("""
                        {"email":"%s","password":"123456"}
                        """.formatted(email))
                .exchange()
                .expectStatus().isOk(); // ✅ 200 no token needed
    }

    // ✅ GREEN — /projects requires token → 401 without token
    @Test
    @Order(3)
    void projects_requires_token() {
        client.get().uri("/projects")
                .exchange()
                .expectStatus().isUnauthorized() // ✅ 401
                .expectBody()
                .jsonPath("$.error").isEqualTo("unauthorized");
    }

    // ✅ GREEN — /projects/:id requires token → 401 without token
    @Test
    @Order(4)
    void project_by_id_requires_token() {
        client.get().uri("/projects/00000000-0000-0000-0000-000000000000")
                .exchange()
                .expectStatus().isUnauthorized(); // ✅ 401
    }

    // ✅ GREEN — POST /projects requires token → 401 without token
    @Test
    @Order(5)
    void create_project_requires_token() {
        client.post().uri("/projects")
                .header("Content-Type", "application/json")
                .bodyValue("""
                        {"name":"Hacked Project"}
                        """)
                .exchange()
                .expectStatus().isUnauthorized(); // ✅ 401
    }

    // ✅ GREEN — /projects/:id/tasks requires token → 401
    @Test
    @Order(6)
    void tasks_requires_token() {
        client.get().uri("/projects/00000000-0000-0000-0000-000000000000/tasks")
                .exchange()
                .expectStatus().isUnauthorized(); // ✅ 401
    }

    // ✅ GREEN — PATCH /tasks/:id requires token → 401
    @Test
    @Order(7)
    void update_task_requires_token() {
        client.patch().uri("/tasks/00000000-0000-0000-0000-000000000000")
                .header("Content-Type", "application/json")
                .bodyValue("""
                        {"title":"Hacked"}
                        """)
                .exchange()
                .expectStatus().isUnauthorized(); // ✅ 401
    }

    // ✅ GREEN — DELETE /tasks/:id requires token → 401
    @Test
    @Order(8)
    void delete_task_requires_token() {
        client.delete().uri("/tasks/00000000-0000-0000-0000-000000000000")
                .exchange()
                .expectStatus().isUnauthorized(); // ✅ 401
    }

    // ✅ GREEN — expired/invalid token returns 401
    @Test
    @Order(9)
    void invalid_token_returns_401() {
        client.get().uri("/projects")
                .header("Authorization", "Bearer invalidtoken123")
                .exchange()
                .expectStatus().isUnauthorized() // ✅ 401
                .expectBody()
                .jsonPath("$.error").isEqualTo("unauthorized");
    }

    // ✅ GREEN — malformed Bearer header returns 401
    @Test
    @Order(10)
    void malformed_auth_header_returns_401() {
        client.get().uri("/projects")
                .header("Authorization", "NotBearer sometoken")
                .exchange()
                .expectStatus().isUnauthorized(); // ✅ 401
    }

    // ✅ GREEN — valid token can access protected endpoint
    @Test
    @Order(11)
    void valid_token_accesses_protected_endpoint() {
        String token = getToken("sec_valid");

        client.get().uri("/projects")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk(); // ✅ 200
    }

    // ✅ GREEN — valid token can create project
    @Test
    @Order(12)
    void valid_token_creates_project() {
        String token = getToken("sec_create");

        client.post().uri("/projects")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("""
                        {"name":"Secure Project"}
                        """)
                .exchange()
                .expectStatus().isCreated(); // ✅ 201
    }

    // ✅ GREEN — user cannot update another user's project → 403
    @Test
    @Order(13)
    void cannot_update_another_users_project() {
        String ownerToken = getToken("sec_owner1");
        String attackerToken = getToken("sec_attacker1");

        String projectId = client.post().uri("/projects")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ownerToken)
                .bodyValue("""
                        {"name":"Owner Only Project"}
                        """)
                .exchange()
                .returnResult(Map.class)
                .getResponseBody()
                .blockFirst()
                .get("id")
                .toString();

        client.patch().uri("/projects/" + projectId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + attackerToken)
                .bodyValue("""
                        {"name":"Hacked Name"}
                        """)
                .exchange()
                .expectStatus().isForbidden(); // ✅ 403
    }

    // ✅ GREEN — user cannot delete another user's project → 403
    @Test
    @Order(14)
    void cannot_delete_another_users_project() {
        String ownerToken = getToken("sec_owner2");
        String attackerToken = getToken("sec_attacker2");

        String projectId = client.post().uri("/projects")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ownerToken)
                .bodyValue("""
                        {"name":"Protected Project"}
                        """)
                .exchange()
                .returnResult(Map.class)
                .getResponseBody()
                .blockFirst()
                .get("id")
                .toString();

        client.delete().uri("/projects/" + projectId)
                .header("Authorization", "Bearer " + attackerToken)
                .exchange()
                .expectStatus().isForbidden(); // ✅ 403
    }

    // ✅ GREEN — user cannot delete another user's task → 403
    @Test
    @Order(15)
    void cannot_delete_another_users_task() {
        String ownerToken = getToken("sec_owner3");
        String attackerToken = getToken("sec_attacker3");

        String projectId = client.post().uri("/projects")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ownerToken)
                .bodyValue("""
                        {"name":"Task Security Project"}
                        """)
                .exchange()
                .returnResult(Map.class)
                .getResponseBody()
                .blockFirst()
                .get("id")
                .toString();

        String taskId = client.post().uri("/projects/" + projectId + "/tasks")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ownerToken)
                .bodyValue("""
                        {"title":"Protected Task","status":"todo"}
                        """)
                .exchange()
                .returnResult(Map.class)
                .getResponseBody()
                .blockFirst()
                .get("id")
                .toString();

        client.delete().uri("/tasks/" + taskId)
                .header("Authorization", "Bearer " + attackerToken)
                .exchange()
                .expectStatus().isForbidden(); // ✅ 403
    }
}