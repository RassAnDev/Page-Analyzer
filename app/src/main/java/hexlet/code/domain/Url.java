package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;

@Entity
public final class Url extends Model {
    @Id
    private long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    private List<UrlCheck> urlChecks;

    @WhenCreated
    private Instant createdAt;

    public Url() {

    }

    public Url(String name) {
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public List<UrlCheck> getUrlChecks() {
        return this.urlChecks;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }
}
