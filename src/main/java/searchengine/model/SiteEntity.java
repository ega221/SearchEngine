package searchengine.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import searchengine.model.enums.IndexingStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "site")
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
public class SiteEntity {
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
    private Set<PageEntity> pageSet = new HashSet<>();
}
