package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.Empty;
import kong.unirest.HttpResponse;

import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {

    // Выполняем проверку, что тестовая среда работает корректно
    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static Database database;

    private static MockWebServer server;

    // Выполняем настройку тестовой среды, запускаем приложение, базу данных и MockWebServer
    @BeforeAll
    public static void beforeAll() throws Exception {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();
        server = new MockWebServer();
        MockResponse response = new MockResponse()
                .setBody(Files.readString(Paths.get("./src/test/resources/test-page.html")));
        server.enqueue(response);
        server.start();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        server.shutdown();
        app.stop();
    }

    // Перед каждым тестом очищаем содержимое базы данных и заполняем тестовыми записями
    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }

    // Проверяем, что в базу данных добавились тестовые записи
    @Test
    void testInitDataBase() {
        String testUrlName = "https://javalin.io";

        Url testUrl = new QUrl()
                .name.equalTo(testUrlName)
                .findOne();

        assertThat(testUrl).isNotNull();
        assertThat(testUrl.getName()).isEqualTo(testUrlName);
    }

    @Nested
    class RootTest {

        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Бесплатно проверяйте сайты на SEO пригодность");
            assertThat(response.getBody()).contains("https://www.example.com");
            assertThat(response.getHeaders().getFirst("Content-Type")).isEqualTo("text/html");
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
            HttpResponse<String> response;
            String body;

            // Проверяем, что страница отображается корректно
            response = Unirest.get(baseUrl + "/urls/1").asString();
            body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("https://javalin.io");
            assertThat(body).contains("08/08/2023 15:59");

            response = Unirest.get(baseUrl + "/urls/2").asString();
            body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("https://www.thymeleaf.org");
            assertThat(body).contains("08/08/2023 16:00");

            // Проверяем наличие ошибки при запросе к несуществующей в списке странице
            response = Unirest.get(baseUrl + "/urls/5").asString();
            assertThat(response.getStatus()).isEqualTo(404);
        }

        @Test
        void testCreate() {
            String inputUrl = "https://ebean.io/docs/query/query-beans";
            String normalizedUrl = "https://ebean.io";
            String incorrectUrl = "avada kedavra";
            HttpResponse<Empty> responsePost;
            HttpResponse<String> responseGet;
            String body;

            //Проверяем, что страница добавляется корректно
            responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            responseGet = Unirest.get(baseUrl + "/urls").asString();
            body = responseGet.getBody();

            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(body).contains(normalizedUrl);
            assertThat(body).contains("Страница успешно добавлена");

            // Проверяем, что выводится ошибка при попытке добавления существующей страницы
            responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", normalizedUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            responseGet = Unirest.get(baseUrl + "/urls").asString();
            body = responseGet.getBody();

            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(body).contains(normalizedUrl);
            assertThat(body).contains("Страница уже существует");

            // Проверяем, что выводится ошибка при попытке добавления некорректного url
            responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", incorrectUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

            responseGet = Unirest.get(baseUrl).asString();
            body = responseGet.getBody();

            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(body).contains("Некорректный URL");

            responseGet = Unirest.get(baseUrl + "/urls").asString();
            body = responseGet.getBody();

            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(body).doesNotContain(incorrectUrl);

            // Проверяем, что корректный url был добавлен в БД
            Url actualUrl = new QUrl()
                    .name.equalTo(normalizedUrl)
                    .findOne();

            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(normalizedUrl);

            // Проверяем, что некорректный url не был добавлен в БД
            Url invalidUrl = new QUrl()
                    .name.equalTo(incorrectUrl)
                    .findOne();

            assertThat(invalidUrl).isNull();
        }

        @Test
        void testUrlCheck() {
            // Получаем нормализованный url у MockWebServer для добавления
            String url = "%s://%s".formatted(server.url("/").url()
                    .getProtocol(), server.url("/").url().getAuthority());

            HttpResponse<Empty> responsePost;
            HttpResponse<String> responseGet;
            HttpResponse<Empty> responseCheckPost;
            HttpResponse<String> responseCheckGet;
            String checkBody;

            responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", url)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            responseGet = Unirest.get(baseUrl + "/urls").asString();
            String body = responseGet.getBody();

            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(body).contains(url);
            assertThat(body).contains("Страница успешно добавлена");

            Url actualUrl = new QUrl()
                    .name.equalTo(url)
                    .findOne();

            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(url);

            //Проверяем, что проверки для url добавляются корректно и отображается необходимое содержимое url
            responseCheckPost = Unirest
                    .post(baseUrl + "/urls/" + actualUrl.getId() + "/checks")
                    .asEmpty();

            assertThat(responseCheckPost.getStatus()).isEqualTo(302);
            assertThat(responseCheckPost.getHeaders().getFirst("Location"))
                    .isEqualTo("/urls/" + actualUrl.getId());

            responseCheckGet = Unirest.get(baseUrl + "/urls/" + actualUrl.getId()).asString();
            checkBody = responseCheckGet.getBody();

            assertThat(responseCheckGet.getStatus()).isEqualTo(200);
            assertThat(checkBody).contains("200");
            assertThat(checkBody).contains("Название страницы");
            assertThat(checkBody).contains("Заголовок Н1");
            assertThat(checkBody).contains("Описание страницы");
            assertThat(checkBody).contains("Страница успешно проверена");

            UrlCheck lastUrlCheck = new QUrlCheck()
                    .orderBy()
                    .id.desc()
                    .findOne();

            assertThat(lastUrlCheck).isNotNull();
            assertThat(lastUrlCheck.getStatusCode()).isEqualTo(200);

            // Проверяем, что выводится ошибка при попытке добавления проверки для несуществующего url
            responseCheckPost = Unirest
                    .post(baseUrl + "/urls/5/checks")
                    .asEmpty();
            assertThat(responseCheckPost.getStatus()).isEqualTo(404);
        }
    }
}
