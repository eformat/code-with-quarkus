package rest;

import static io.restassured.RestAssured.given;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;

@QuarkusTest
public class TodosResourceTestFailed {

    @Test
    public void testFailedTodos() {
        // Missing CSRF token and cookie should be rejected with 400
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("task", "Should fail")
            .when().post("/Todos/add")
            .then()
            .statusCode(400);
    }

    @Test
    public void testBlankTaskValidation() {
        // Acquire CSRF token and cookie from the form page
        Response resp = given()
            .when().get("/Todos/todos")
            .then()
            .statusCode(200)
            .extract().response();

        String html = resp.asString();
        int beforeCount = countTodos(html);
        String csrfToken = html.replaceAll("(?s).*name=\\\"csrf-token\\\" value=\\\"([^\\\"]*)\\\".*", "$1");
        String csrfCookie = resp.getCookie("csrf-token");

        // Post a blank task; validation should not add a todo item
        Response post = given()
            .contentType("application/x-www-form-urlencoded")
            .cookie("csrf-token", csrfCookie)
            .formParam("csrf-token", csrfToken)
            .formParam("task", "")
            .when().post("/Todos/add")
            .then()
            .statusCode(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.is(200), org.hamcrest.Matchers.is(303)))
            .extract().response();

        // Always follow up to the todos page with cookies and ensure count unchanged
        String afterHtml = given()
            .cookies(post.getCookies())
            .when().get("/Todos/todos")
            .then()
            .statusCode(200)
            .extract().asString();

        int afterCount = countTodos(afterHtml);
        org.junit.jupiter.api.Assertions.assertEquals(beforeCount, afterCount, "Blank submission must not add a todo");
    }

    private static int countTodos(String html) {
        int count = 0;
        int idx = 0;
        String marker = "<div class=\"todo\">";
        while ((idx = html.indexOf(marker, idx)) != -1) {
            count++;
            idx += marker.length();
        }
        return count;
    }
}
