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
public class ProjectControllerTest {

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
                        {"name":"Project User","email":"%s","password":"123456"}
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

    private String createProject(String token, String name) {
        return client.post().uri("/projects")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("""
                        {"name":"%s"}
                        """.formatted(name))
                .exchange()
                .returnResult(Map.class)
                .getResponseBody()
                .blockFirst()
                .get("id")
                .toString();
    }

    // ✅ GREEN — create project returns 201
    @Test
    @Order(1)
    void create_project_success() {
        String token = getToken("proj_create");

        client.post().uri("/projects")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("""
                        {"name":"Test Project","description":"A test project"}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Test Project");
    }

    // ✅ GREEN — missing name returns 400
    @Test
    @Order(2)
    void create_project_missing_name_returns_400() {
        String token = getToken("proj_noname");

        client.post().uri("/projects")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("""
                        {"description":"No name here"}
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("validation failed")
                .jsonPath("$.fields.name").isEqualTo("is required");
    }

    // ✅ GREEN — list projects returns 200
    @Test
    @Order(3)
    void list_projects_success() {
        String token = getToken("proj_list");
        createProject(token, "Listed Project");

        client.get().uri("/projects")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }

    // ✅ GREEN — get project by id returns project + tasks
    @Test
    @Order(4)
    void get_project_by_id_success() {
        String token = getToken("proj_getbyid");
        String id = createProject(token, "Detail Project");

        client.get().uri("/projects/" + id)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.tasks").isArray();
    }

    // ✅ GREEN — project not found returns 404
    @Test
    @Order(5)
    void get_project_not_found_returns_404() {
        String token = getToken("proj_notfound");

        client.get().uri("/projects/00000000-0000-0000-0000-000000000000")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("not found");
    }

    // ✅ GREEN — update project success
    @Test
    @Order(6)
    void update_project_success() {
        String token = getToken("proj_update");
        String id = createProject(token, "Old Name");

        client.patch().uri("/projects/" + id)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("""
                        {"name":"New Name","description":"Updated"}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("New Name");
    }

    // ✅ GREEN — update by non-owner returns 403
    @Test
    @Order(7)
    void update_project_by_non_owner_returns_403() {
        String ownerToken = getToken("proj_owner1");
        String otherToken = getToken("proj_other1");
        String id = createProject(ownerToken, "Owner Project");

        client.patch().uri("/projects/" + id)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + otherToken)
                .bodyValue("""
                        {"name":"Hacked"}
                        """)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ✅ GREEN — delete project returns 204
    @Test
    @Order(8)
    void delete_project_success() {
        String token = getToken("proj_delete");
        String id = createProject(token, "To Delete");

        client.delete().uri("/projects/" + id)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();
    }

    // ✅ GREEN — delete by non-owner returns 403
    @Test
    @Order(9)
    void delete_project_by_non_owner_returns_403() {
        String ownerToken = getToken("proj_owner2");
        String otherToken = getToken("proj_other2");
        String id = createProject(ownerToken, "Protected Project");

        client.delete().uri("/projects/" + id)
                .header("Authorization", "Bearer " + otherToken)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ✅ GREEN — no token returns 401
    @Test
    @Order(10)
    void list_projects_no_token_returns_401() {
        client.get().uri("/projects")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}