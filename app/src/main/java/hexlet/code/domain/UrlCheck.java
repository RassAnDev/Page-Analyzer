package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Entity
public final class UrlCheck extends Model {
    @Id
    private long id;

    private int statusCode;

    private String title;

    private String h1;

    @Lob
    private String description;

    @ManyToOne
    private Url url;

    @WhenCreated
    private Instant createdAt;

    public UrlCheck(int statusCode, String title, String h1, String description) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
    }

    public long getId() {
        return this.id;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getTitle() {
        return this.title;
    }

    public String getH1() {
        return this.h1;
    }

    public String getDescription() {
        return this.description;
    }

    public Url getUrl() {
        return this.url;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }
}
