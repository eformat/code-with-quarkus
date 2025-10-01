package rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;

@QuarkusTest
public class TodosResourceTest {

    @Test
    public void testRenardeIndexAndTodosFlow() {
        // GET /renarde
        given()
            .when().get("/renarde")
            .then()
            .statusCode(200)
            .body(containsString("Welcome to your Quarkus Renarde"));

        // GET /Todos/todos to obtain CSRF token and cookie
        Response todosResponse = given()
            .when().get("/Todos/todos")
            .then()
            .statusCode(200)
            .extract().response();

        String html = todosResponse.asString();
        String csrfToken = html.replaceAll("(?s).*name=\\\"csrf-token\\\" value=\\\"([^\\\"]*)\\\".*", "$1");
        String csrfCookie = todosResponse.getCookie("csrf-token");

        // POST a new todo (form-urlencoded) with CSRF
        String task = "Added from test";
        given()
            .contentType("application/x-www-form-urlencoded")
            .cookie("csrf-token", csrfCookie)
            .formParam("csrf-token", csrfToken)
            .formParam("task", task)
            .when().post("/Todos/add")
            .then()
            .statusCode(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.is(303), org.hamcrest.Matchers.is(200)));

        // Verify the item appears on the page (template capitalises each word)
        String expected = capitaliseWords(task);
        given()
            .when().get("/Todos/todos")
            .then()
            .statusCode(200)
            .body(containsString(expected));
    }

    private static String capitaliseWords(String input) {
        String[] parts = input.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1));
        }
        return sb.toString();
    }
}


