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
public class TaskControllerTest {

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
                        {"name":"Task User","email":"%s","password":"123456"}
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

    private String createTask(String token, String projectId, String title, String status) {
        return client.post().uri("/projects/" + projectId + "/tasks")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("""
                        {"title":"%s","status":"%s"}
                        """.formatted(title, status))
                .exchange()
                .returnResult(Map.class)
                .getResponseBody()
                .blockFirst()
                .get("id")
                .toString();
    }

    // ✅ GREEN — create task success returns 201
    @Test
    @Order(1)
    void create_task_success() {
        String token = getToken("task_create");
        String projectId = createProject(token, "Task Project 1");

        client.post().uri("/projects/" + projectId + "/tasks")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("""
                        {"title":"First Task","status":"todo","priority":"high"}
                        """)
                .exchange()
                .expectStatus().isCreated()                           // ✅ 201
                .expectBody()
                .jsonPath("$.id").exists()                            // ✅ id
                .jsonPath("$.title").isEqualTo("First Task")          // ✅ title
                .jsonPath("$.status").isEqualTo("todo")               // ✅ status
                .jsonPath("$.priority").isEqualTo("high");            // ✅ priority
    }

    // ✅ GREEN — missing title returns 400
    @Test
    @Order(2)
    void create_task_missing_title_returns_400() {
        String token = getToken("task_notitle");
        String projectId = createProject(token, "Task Project 2");

        client.post().uri("/projects/" + projectId + "/tasks")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("""
                        {"status":"todo","priority":"high"}
                        """)
                .exchange()
                .expectStatus().isBadRequest()                        // ✅ 400
                .expectBody()
                .jsonPath("$.error").isEqualTo("validation failed")   // ✅
                .jsonPath("$.fields.title").isEqualTo("is required"); // ✅
    }

    // ✅ GREEN — invalid status returns 400
    @Test
    @Order(3)
    void create_task_invalid_status_returns_400() {
        String token = getToken("task_badstatus");
        String projectId = createProject(token, "Task Project 3");

        client.post().uri("/projects/" + projectId + "/tasks")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("""
                        {"title":"Bad Task","status":"invalid"}
                        """)
                .exchange()
                .expectStatus().isBadRequest()                        // ✅ 400
                .expectBody()
                .jsonPath("$.error").isEqualTo("validation failed");  // ✅
    }

    // ✅ GREEN — list tasks returns 200
    @Test
    @Order(4)
    void list_tasks_success() {
        String token = getToken("task_list");
        String projectId = createProject(token, "Task Project 4");
        createTask(token, projectId, "Listed Task", "todo");

        client.get().uri("/projects/" + projectId + "/tasks")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()                                // ✅ 200
                .expectBody()
                .jsonPath("$").isArray()                              // ✅ array
                .jsonPath("$[0].title").isEqualTo("Listed Task");     // ✅ task present
    }

    // ✅ GREEN — filter tasks by status
    @Test
    @Order(5)
    void list_tasks_filter_by_status() {
        String token = getToken("task_filter");
        String projectId = createProject(token, "Task Project 5");
        createTask(token, projectId, "Todo Task", "todo");
        createTask(token, projectId, "Done Task", "done");

        client.get().uri("/projects/" + projectId + "/tasks?status=todo")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()                                // ✅ 200
                .expectBody()
                .jsonPath("$[0].status").isEqualTo("todo")            // ✅ filtered
                .jsonPath("$[1]").doesNotExist();                     // ✅ only 1 result
    }

    // ✅ GREEN — update task success
    @Test
    @Order(6)
    void update_task_success() {
        String token = getToken("task_update");
        String projectId = createProject(token, "Task Project 6");
        String taskId = createTask(token, projectId, "Old Title", "todo");

        client.patch().uri("/tasks/" + taskId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("""
                        {"title":"New Title","status":"in_progress","priority":"low"}
                        """)
                .exchange()
                .expectStatus().isOk()                                // ✅ 200
                .expectBody()
                .jsonPath("$.title").isEqualTo("New Title")           // ✅ updated
                .jsonPath("$.status").isEqualTo("in_progress");       // ✅ status updated
    }

    // ✅ GREEN — invalid priority returns 400
    @Test
    @Order(7)
    void update_task_invalid_priority_returns_400() {
        String token = getToken("task_badprio");
        String projectId = createProject(token, "Task Project 7");
        String taskId = createTask(token, projectId, "Valid Task", "todo");

        client.patch().uri("/tasks/" + taskId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("""
                        {"priority":"invalid"}
                        """)
                .exchange()
                .expectStatus().isBadRequest()                        // ✅ 400
                .expectBody()
                .jsonPath("$.error").isEqualTo("validation failed");  // ✅
    }

    // ✅ GREEN — delete task by owner returns 204
    @Test
    @Order(8)
    void delete_task_by_owner_success() {
        String token = getToken("task_delowner");
        String projectId = createProject(token, "Task Project 8");
        String taskId = createTask(token, projectId, "Delete Me", "todo");

        client.delete().uri("/tasks/" + taskId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();                        // ✅ 204
    }

    // ✅ GREEN — delete task by non-owner returns 403
    @Test
    @Order(9)
    void delete_task_by_non_owner_returns_403() {
        String ownerToken = getToken("task_owner1");
        String otherToken = getToken("task_other1");
        String projectId = createProject(ownerToken, "Task Project 9");
        String taskId = createTask(ownerToken, projectId, "Protected Task", "todo");

        client.delete().uri("/tasks/" + taskId)
                .header("Authorization", "Bearer " + otherToken)
                .exchange()
                .expectStatus().isForbidden();                        // ✅ 403
    }

    // ✅ GREEN — task not found returns 404
    @Test
    @Order(10)
    void update_task_not_found_returns_404() {
        String token = getToken("task_ghost");

        client.patch().uri("/tasks/00000000-0000-0000-0000-000000000000")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .bodyValue("""
                        {"title":"Ghost Task"}
                        """)
                .exchange()
                .expectStatus().isNotFound()                          // ✅ 404
                .expectBody()
                .jsonPath("$.error").isEqualTo("not found");          // ✅
    }
}