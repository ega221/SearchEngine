package searchengine.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import searchengine.model.enums.IndexingStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "site")
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IndexingStatus status;
    @Column(name = "status_time", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime statusTime;
    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;
    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String url;
    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;
    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY)
    private Set<Page> pageSet = new HashSet<>();
}
