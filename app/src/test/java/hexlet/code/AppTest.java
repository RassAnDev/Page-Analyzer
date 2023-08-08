package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;

import kong.unirest.Unirest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {

    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static Database database;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Nested
    class RootTest {

        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Бесплатно проверяйте сайты на SEO пригодность");
        }
    }

    @Nested
    class UrlTest {

        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("https://javalin.io");
            assertThat(body).contains("https://www.thymeleaf.org");
        }

        @Test
        void testShow() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls/1").asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("https://javalin.io");
            assertThat(body).contains("08/08/2023 15:59");

            response = Unirest.get(baseUrl + "/urls/2").asString();
            body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("https://www.thymeleaf.org");
            assertThat(body).contains("08/08/2023 16:00");
        }

        @Test
        void testCreate() {
            String inputUrl = "https://ebean.io/docs/query/query-beans";
            String normalizedUrl = "https://ebean.io";

            HttpResponse responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(normalizedUrl);
            assertThat(body).contains("Страница успешно добавлена");

            responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", normalizedUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            response = Unirest.get(baseUrl + "/urls").asString();
            body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(normalizedUrl);
            assertThat(body).contains("Страница уже существует");

            Url actualUrl = new QUrl()
                    .name.equalTo(normalizedUrl)
                    .findOne();

            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(normalizedUrl);
        }
    }
}
